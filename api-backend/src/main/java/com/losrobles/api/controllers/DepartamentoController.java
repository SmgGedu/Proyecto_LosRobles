package com.losrobles.api.controllers;

import com.losrobles.api.dto.DepartamentoResponseDTO;
import com.losrobles.api.models.Departamento;
import com.losrobles.api.services.DepartamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @GetMapping
    public ResponseEntity<List<DepartamentoResponseDTO>> listar() {
        List<DepartamentoResponseDTO> departamentos = departamentoService.findAllConEstado();
        if (departamentos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(departamentos);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<Departamento> crear(@RequestBody Departamento depa) {
        try {
            return ResponseEntity.ok(departamentoService.save(depa));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departamento> obtenerPorId(@PathVariable Integer id) {
        return departamentoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/liberar")
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<?> liberar(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(departamentoService.liberar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<?> asignar(@PathVariable Integer id, @RequestBody Map<String, Integer> body) {
        Integer usuarioId = body.get("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.badRequest().body("Error: Debe indicar el campo 'usuarioId'.");
        }
        try {
            return ResponseEntity.ok(departamentoService.asignar(id, usuarioId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
