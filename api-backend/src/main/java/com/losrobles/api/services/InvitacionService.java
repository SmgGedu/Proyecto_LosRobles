package com.losrobles.api.services;

import com.google.common.base.Preconditions;
import com.losrobles.api.dto.InvitacionResponseDTO;
import com.losrobles.api.models.Invitacion;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.repositories.InvitacionRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import com.losrobles.api.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvitacionService {

    private final InvitacionRepository invitacionRepo;

    private final UsuarioRepository usuarioRepo;

    public List<InvitacionResponseDTO> listarTodas() {
        return invitacionRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public InvitacionResponseDTO guardarInvitacion(Invitacion nuevaInvi, String username) {
        Preconditions.checkArgument(nuevaInvi.getFechaProgramada() != null,
                "La fecha programada es obligatoria.");
        Preconditions.checkArgument(!nuevaInvi.getFechaProgramada().isBefore(FechaUtils.hoy()),
                "La fecha programada no puede ser anterior a hoy.");

        Usuario residente = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: El residente emisor no existe."));

        nuevaInvi.setAnfitrion(residente);

        if (nuevaInvi.getCodigoQrHash() == null || nuevaInvi.getCodigoQrHash().isEmpty()) {
            nuevaInvi.setCodigoQrHash(UUID.randomUUID().toString());
        }

        Invitacion guardada = invitacionRepo.save(nuevaInvi);
        log.info("Invitación creada: id={}, anfitrion='{}', dniVisitante='{}', fechaProgramada={}",
                guardada.getId(), username, guardada.getVisitante().getDni(), guardada.getFechaProgramada());
        return mapToDTO(guardada);
    }

    public InvitacionResponseDTO buscarPorHashDTO(String hash) {
        Invitacion invi = invitacionRepo.findByCodigoQrHash(hash)
                .orElseThrow(() -> {
                    log.warn("Búsqueda de invitación con hash inexistente: '{}'", hash);
                    return new RuntimeException("¡QR Inválido! No existe o ya fue utilizado.");
                });
        return mapToDTO(invi);
    }

    /**
     * MÉTODO DE MAPEADO (Corregido según tus capturas)
     */
    private InvitacionResponseDTO mapToDTO(Invitacion invi) {
        return InvitacionResponseDTO.builder()
                .id(invi.getId()) // Sincronizado con el modelo
                .codigoQrHash(invi.getCodigoQrHash())
                .nombreAnfitrion(invi.getAnfitrion().getNombres() + " " + invi.getAnfitrion().getApellidos())
                .dniVisitante(invi.getVisitante().getDni())
                .nombreDepartamento(invi.getDepartamentoDestino().getBloqueTorre() + " - "
                        + invi.getDepartamentoDestino().getNumeroDepa())
                .fechaProgramada(invi.getFechaProgramada())
                .estado(invi.getEstado())
                .build();
    }
}