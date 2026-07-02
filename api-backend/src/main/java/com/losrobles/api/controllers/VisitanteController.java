package com.losrobles.api.controllers;

import com.losrobles.api.models.Visitante;
import com.losrobles.api.services.VisitanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitantes")
@RequiredArgsConstructor
public class VisitanteController {

    private final VisitanteService visitanteService;

    @GetMapping
    public List<Visitante> listar() {
        return visitanteService.listarTodos();
    }

    @PostMapping
    public Visitante crear(@RequestBody Visitante visitante) {
        return visitanteService.guardar(visitante);
    }

    @GetMapping("/por-dni/{dni}")
    public ResponseEntity<Visitante> buscarPorDni(@PathVariable String dni) {
        return visitanteService.buscarPorDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}