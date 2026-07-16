package com.losrobles.api.controllers;

import com.losrobles.api.models.Visitante;
import com.losrobles.api.services.VisitanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitantes")
@RequiredArgsConstructor
public class VisitanteController {

    private final VisitanteService visitanteService;

    /**
     * Listado completo de visitantes (datos de todos los residentes/torres).
     * Restringido a personal de portería/administración: un Residente no
     * debe poder consultar la base de visitantes de todo el condominio.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public List<Visitante> listar() {
        return visitanteService.listarTodos();
    }

    // Cualquier rol autenticado (incluido Residente) puede registrar un
    // visitante nuevo al crear una invitación; solo se exige autenticación.
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Visitante crear(@Valid @RequestBody Visitante visitante) {
        return visitanteService.guardar(visitante);
    }

    @GetMapping("/por-dni/{dni}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Visitante> buscarPorDni(@PathVariable String dni) {
        return visitanteService.buscarPorDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}