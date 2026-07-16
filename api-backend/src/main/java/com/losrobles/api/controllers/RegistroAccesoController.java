package com.losrobles.api.controllers;

import com.losrobles.api.dto.AccesoResponseDTO;
import com.losrobles.api.dto.AforoResponseDTO;
import com.losrobles.api.dto.IngresoRequestDTO;
import com.losrobles.api.services.RegistroAccesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.losrobles.api.dto.RegistroCompletoRequest;

import java.util.List;

@RestController
@RequestMapping("/api/accesos")
@RequiredArgsConstructor
public class RegistroAccesoController {

    private final RegistroAccesoService registroService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<List<AccesoResponseDTO>> listarAccesos() {
        return ResponseEntity.ok(registroService.listarTodos());
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<AforoResponseDTO> verAforoActual() {
        return ResponseEntity.ok(registroService.obtenerAforoActual());
    }

    @PostMapping("/ingreso-qr")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<AccesoResponseDTO> ingresoPorQR(
            @Valid @RequestBody IngresoRequestDTO request,
            Authentication auth) {
        return ResponseEntity.ok(registroService.registrarEntradaQR(request, auth.getName()));
    }

    @PostMapping("/registro-manual")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<AccesoResponseDTO> registroManual(
            @Valid @RequestBody RegistroCompletoRequest request,
            Authentication auth) {
        try {
            AccesoResponseDTO resultado = registroService.registrarEntradaManual(request, auth.getName());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/salida/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<AccesoResponseDTO> registrarSalida(@PathVariable Integer id) {
        return ResponseEntity.ok(registroService.registrarSalida(id));
    }
}