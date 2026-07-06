package com.losrobles.api.services;

import com.losrobles.api.dto.ResidenteDTO;
import com.losrobles.api.dto.UsuarioResponseDTO;
import com.losrobles.api.models.Departamento;
import com.losrobles.api.models.Rol;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.repositories.DepartamentoRepository;
import com.losrobles.api.repositories.RolRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final DepartamentoRepository departamentoRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    /**
     * Lista los usuarios para el panel de administración. Se mapea a un DTO
     * dentro de la transacción para evitar errores de serialización al
     * acceder al departamento (relación LAZY) fuera de la sesión de Hibernate.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> findAllConDetalle() {
        return usuarioRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Perfil del usuario autenticado (nombres, rol, departamento propio),
     * usado por la app móvil para prellenar el residente/conserje logueado.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPerfilPropio(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));
        return mapToDTO(usuario);
    }

    private UsuarioResponseDTO mapToDTO(Usuario u) {
        UsuarioResponseDTO.UsuarioResponseDTOBuilder dto = UsuarioResponseDTO.builder()
                .id(u.getId())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .dni(u.getDni())
                .telefono(u.getTelefono())
                .username(u.getUsername())
                .email(u.getEmail())
                .estado(u.getEstado());

        if (u.getRol() != null) {
            dto.rolId(u.getRol().getId()).rolNombre(u.getRol().getNombreRol());
        }

        Departamento depto = u.getDepartamento();
        if (depto != null) {
            dto.departamentoId(depto.getId())
                    .departamentoNumero(depto.getNumeroDepa())
                    .departamentoTorre(depto.getBloqueTorre());
        }

        return dto.build();
    }

    /**
     * Crea un usuario validando unicidad de username/DNI, asignando el rol y
     * departamento (si aplica) y cifrando la contraseña con BCrypt.
     */
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Error: El username ya está en uso");
        }
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new IllegalArgumentException("Error: El DNI ya está registrado");
        }
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("Error: Es necesario asignar un rol válido.");
        }
        Rol rol = rolRepository.findById(usuario.getRol().getId())
                .orElseThrow(() -> new IllegalArgumentException("Error: Rol no encontrado en el sistema."));
        usuario.setRol(rol);

        if (usuario.getDepartamento() != null && usuario.getDepartamento().getId() != null) {
            Departamento depto = departamentoRepository.findById(usuario.getDepartamento().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Error: Departamento no encontrado."));
            usuario.setDepartamento(depto);
        } else {
            usuario.setDepartamento(null);
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Activa o da de baja a un usuario (login bloqueado cuando estado = false).
     * Se mapea a DTO dentro de la transacción por la misma razón que
     * findAllConDetalle: evitar LazyInitializationException al serializar el
     * departamento (relación LAZY) fuera de la sesión de Hibernate.
     */
    @Transactional
    public UsuarioResponseDTO cambiarEstado(Integer id, boolean estado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Usuario no encontrado."));
        usuario.setEstado(estado);
        if (!estado) {
            // Al dar de baja se libera también su departamento, para que quede
            // disponible de inmediato para otro residente.
            usuario.setDepartamento(null);
        }
        return mapToDTO(usuarioRepository.save(usuario));
    }

    /**
     * Elimina definitivamente a un usuario. Solo procede si ya fue dado de baja,
     * para evitar borrar cuentas activas por error.
     */
    public void eliminar(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Usuario no encontrado."));
        if (Boolean.TRUE.equals(usuario.getEstado())) {
            throw new IllegalArgumentException("Error: Solo se pueden eliminar usuarios dados de baja.");
        }
        try {
            usuarioRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Error: No se puede eliminar el usuario porque tiene registros asociados " +
                            "(accesos, dispositivos, invitaciones u otros). Mantenlo dado de baja en su lugar.");
        }
    }

    /**
     * Lista residentes activos que aún no tienen un departamento asignado,
     * para ofrecerlos como candidatos al asignar un departamento libre.
     */
    @Transactional(readOnly = true)
    public List<ResidenteDTO> listarResidentesDisponibles() {
        return usuarioRepository.buscarResidentesSinDepartamento().stream()
                .map(u -> new ResidenteDTO(
                        u.getId(),
                        u.getNombres() + " " + u.getApellidos(),
                        "Sin departamento asignado",
                        null))
                .collect(Collectors.toList());
    }

    public List<ResidenteDTO> buscarResidente(String nombre) {
        List<Usuario> residentes = usuarioRepository.buscarResidentePorNombre(nombre);
        return residentes.stream().map(u -> {
            String deptoInfo = "Sin departamento asignado";
            Integer deptoId = null;
            if (u.getDepartamento() != null) {
                deptoInfo = u.getDepartamento().getBloqueTorre() + " - " + u.getDepartamento().getNumeroDepa();
                deptoId = u.getDepartamento().getId();
            }
            return new ResidenteDTO(
                    u.getId(),
                    u.getNombres() + " " + u.getApellidos(),
                    deptoInfo,
                    deptoId);
        }).collect(Collectors.toList());
    }
}
