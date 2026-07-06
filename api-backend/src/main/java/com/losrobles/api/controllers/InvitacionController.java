package com.losrobles.api.controllers;

import com.losrobles.api.dto.InvitacionResponseDTO; // Importamos el DTO
import com.losrobles.api.dto.RegistroCompletoRequest;
import com.losrobles.api.services.InvitacionService;
import com.losrobles.api.services.RegistroAccesoService;
import com.losrobles.api.models.Invitacion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitaciones")
@RequiredArgsConstructor
public class InvitacionController {

    private final InvitacionService invitacionService;
    private final RegistroAccesoService registroAccesoService;

    /**
     * Listado global de invitaciones en formato DTO.
     * Protege los datos sensibles de los residentes.
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<List<InvitacionResponseDTO>> listarTodas() {
        return ResponseEntity.ok(invitacionService.listarTodas());
    }

    /**
     * Creación de invitación.
     * Recibe la entidad (o podrías crear un InvitacionCreateDTO después)
     * y devuelve la respuesta limpia en DTO.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_Residente')")
    public ResponseEntity<InvitacionResponseDTO> crearInvitacion(@RequestBody Invitacion nuevaInvi,
            Authentication authentication) {
        String username = authentication.getName();
        InvitacionResponseDTO creada = invitacionService.guardarInvitacion(nuevaInvi, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Invitaciones propias del residente autenticado, para poder reabrir
     * un código QR ya generado (por ejemplo si el visitante aún no llegó).
     */
    @GetMapping("/mias")
    @PreAuthorize("hasAuthority('ROLE_Residente')")
    public ResponseEntity<List<InvitacionResponseDTO>> misInvitaciones(Authentication authentication) {
        return ResponseEntity.ok(invitacionService.listarMisInvitaciones(authentication.getName()));
    }

    /**
     * Búsqueda por Hash optimizada para el escáner del conserje.
     * Devuelve solo los datos necesarios para validar el ingreso.
     */
    @GetMapping("/buscar/{hash}")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<InvitacionResponseDTO> buscarPorHash(@PathVariable String hash) {
        // Llamamos al nuevo método del service que devuelve el DTO
        InvitacionResponseDTO inviDto = invitacionService.buscarPorHashDTO(hash);
        return ResponseEntity.ok(inviDto);
    }

    @PostMapping("/registro-manual")
    @PreAuthorize("hasAnyAuthority('ROLE_Administrador', 'ROLE_Conserje')")
    public ResponseEntity<?> registrarEntradaManual(
            @RequestBody RegistroCompletoRequest request,
            Authentication auth) {

        // Pasamos el nombre del usuario logueado (conserje) para la auditoría
        String usernameConserje = auth.getName();

        return ResponseEntity.ok(registroAccesoService.registrarEntradaManual(request, usernameConserje));
    }
}