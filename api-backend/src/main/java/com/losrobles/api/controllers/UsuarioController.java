package com.losrobles.api.controllers;

import com.losrobles.api.dto.ResidenteDTO;
import com.losrobles.api.dto.UsuarioResponseDTO;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        try {
            return ResponseEntity.ok(usuarioService.crearUsuario(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al registrar el usuario: " + e.getMessage());
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

    @GetMapping("/buscar-residente")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<List<ResidenteDTO>> buscarResidente(@RequestParam String nombre) {
        try {
            return ResponseEntity.ok(usuarioService.buscarResidente(nombre));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
