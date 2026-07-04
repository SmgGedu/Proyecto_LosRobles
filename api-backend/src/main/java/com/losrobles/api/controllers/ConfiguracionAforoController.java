package com.losrobles.api.controllers;

import com.losrobles.api.dto.ConfiguracionAforoDTO;
import com.losrobles.api.services.ConfiguracionAforoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion/aforo")
@RequiredArgsConstructor
public class ConfiguracionAforoController {

    private final ConfiguracionAforoService configuracionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<ConfiguracionAforoDTO> obtenerConfiguracion() {
        return ResponseEntity.ok(ConfiguracionAforoDTO.builder()
                .aforoMaximo(configuracionService.obtenerAforoMaximo())
                .tiempoMaximoVisitaMinutos(configuracionService.obtenerTiempoMaximoVisita())
                .build());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_Administrador')")
    public ResponseEntity<?> actualizarConfiguracion(@RequestBody ConfiguracionAforoDTO request) {
        if (request.getAforoMaximo() <= 0 || request.getTiempoMaximoVisitaMinutos() <= 0) {
            return ResponseEntity.badRequest().body("Los valores de aforo y tiempo máximo deben ser mayores a 0.");
        }
        configuracionService.actualizarAforoMaximo(request.getAforoMaximo());
        configuracionService.actualizarTiempoMaximoVisita(request.getTiempoMaximoVisitaMinutos());
        return ResponseEntity.ok(request);
    }
}
