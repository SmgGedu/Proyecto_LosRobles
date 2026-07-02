package com.losrobles.api.controllers;

import com.losrobles.api.dto.AccesoResponseDTO; // IMPORTANTE: Usar el DTO
import com.losrobles.api.services.RegistroAccesoService;
import com.losrobles.api.services.ReporteService;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAuthority('ROLE_Administrador')")
@RequiredArgsConstructor
public class ReporteController {

    private final RegistroAccesoService registroService;

    private final ReporteService reporteService;

    /**
     * Reporte de Aforo en Tiempo Real.
     * Devuelve información procesada (DTO), no datos crudos de la BD.
     */
    @GetMapping("/aforo-actual")
    public ResponseEntity<Map<String, Object>> obtenerAforoRealTime() {
        // CAMBIO CLAVE: El service devuelve AccesoResponseDTO, no RegistroAcceso
        List<AccesoResponseDTO> activos = registroService.obtenerVisitantesActivos();
        long total = registroService.contarVisitantesEnEdificio();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("totalEnEdificio", total);
        respuesta.put("visitantes", activos);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Exporta el registro de accesos en un rango de fechas (y categoría
     * opcional) a un libro de Excel (.xlsx).
     */
    @GetMapping("/registros/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String categoria) {
        try {
            byte[] excel = reporteService.generarExcel(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX), categoria);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_accesos.xlsx")
                    .body(excel);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exporta el registro de accesos en un rango de fechas (y categoría
     * opcional) a un documento PDF.
     */
    @GetMapping("/registros/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String categoria) {
        try {
            byte[] pdf = reporteService.generarPdf(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX), categoria);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte_accesos.pdf")
                    .body(pdf);
        } catch (DocumentException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}