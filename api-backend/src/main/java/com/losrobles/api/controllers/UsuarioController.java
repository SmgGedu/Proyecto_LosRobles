package com.losrobles.api.controllers;

import com.losrobles.api.dto.ResidenteDTO;
import com.losrobles.api.dto.UsuarioResponseDTO;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public List<UsuarioResponseDTO> listar() {
        return usuarioService.findAllConDetalle();
    }

    /**
     * Perfil del usuario autenticado. Usado por la app móvil (residente/conserje)
     * para mostrar sus datos y, en el caso del residente, su departamento propio.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UsuarioResponseDTO> obtenerPerfilPropio(Authentication auth) {
        return ResponseEntity.ok(usuarioService.obtenerPerfilPropio(auth.getName()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<?> crear(@Valid @RequestBody Usuario usuario) {
        try {
            return ResponseEntity.ok(usuarioService.crearUsuario(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al registrar usuario", e);
            return ResponseEntity.internalServerError().body("Ha ocurrido un error inesperado al registrar el usuario.");
        }
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        Boolean estado = body.get("estado");
        if (estado == null) {
            return ResponseEntity.badRequest().body("Error: Debe indicar el campo 'estado'.");
        }
        try {
            return ResponseEntity.ok(usuarioService.cambiarEstado(id, estado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            usuarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/residentes-disponibles")
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<List<ResidenteDTO>> residentesDisponibles() {
        return ResponseEntity.ok(usuarioService.listarResidentesDisponibles());
    }

    @GetMapping("/buscar-residente")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<List<ResidenteDTO>> buscarResidente(@RequestParam String nombre) {
        try {
            return ResponseEntity.ok(usuarioService.buscarResidente(nombre));
        } catch (Exception e) {
            log.error("Error inesperado al buscar residente", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
