package com.losrobles.api.services;

import com.losrobles.api.dto.DepartamentoResponseDTO;
import com.losrobles.api.models.Departamento;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.repositories.DepartamentoRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<Departamento> findAll() {
        return departamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DepartamentoResponseDTO> findAllConEstado() {
        return departamentoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Departamento save(Departamento departamento) {
        return departamentoRepository.save(departamento);
    }

    public Optional<Departamento> findById(Integer id) {
        return departamentoRepository.findById(id);
    }

    /**
     * "Da de baja" un departamento desvinculando a su(s) residente(s),
     * dejándolo libre para asignar a otra familia.
     */
    @Transactional
    public DepartamentoResponseDTO liberar(Integer id) {
        Departamento depto = departamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Departamento no encontrado."));
        if (depto.getResidentes() != null) {
            depto.getResidentes().forEach(r -> r.setDepartamento(null));
            depto.getResidentes().clear();
        }
        return mapToDTO(depto);
    }

    /**
     * Asigna un residente ya registrado (sin departamento previo) a un
     * departamento libre.
     */
    @Transactional
    public DepartamentoResponseDTO asignar(Integer deptoId, Integer usuarioId) {
        Departamento depto = departamentoRepository.findById(deptoId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Departamento no encontrado."));
        if (depto.getResidentes() != null && !depto.getResidentes().isEmpty()) {
            throw new IllegalArgumentException("Error: El departamento ya tiene un residente asignado.");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Error: Usuario no encontrado."));
        if (usuario.getRol() == null || usuario.getRol().getId() != 3) {
            throw new IllegalArgumentException("Error: Solo se puede asignar como residente a un usuario con rol Residente.");
        }
        if (usuario.getDepartamento() != null) {
            throw new IllegalArgumentException("Error: El usuario ya tiene un departamento asignado. Libéralo primero.");
        }
        usuario.setDepartamento(depto);
        usuarioRepository.save(usuario);
        depto.getResidentes().add(usuario);
        return mapToDTO(depto);
    }

    private DepartamentoResponseDTO mapToDTO(Departamento d) {
        String residente = "No tiene";
        String estado = "Libre";
        Integer residenteId = null;
        String residenteDni = null;
        String residenteTelefono = null;
        String residenteEmail = null;
        if (d.getResidentes() != null && !d.getResidentes().isEmpty()) {
            Usuario r = d.getResidentes().get(0);
            residente = r.getNombres() + " " + r.getApellidos();
            estado = "Ocupado";
            residenteId = r.getId();
            residenteDni = r.getDni();
            residenteTelefono = r.getTelefono();
            residenteEmail = r.getEmail();
        }
        return DepartamentoResponseDTO.builder()
                .id(d.getId())
                .bloqueTorre(d.getBloqueTorre())
                .numeroDepa(d.getNumeroDepa())
                .residente(residente)
                .estado(estado)
                .residenteId(residenteId)
                .residenteDni(residenteDni)
                .residenteTelefono(residenteTelefono)
                .residenteEmail(residenteEmail)
                .build();
    }
}
