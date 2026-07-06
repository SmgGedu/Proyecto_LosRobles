package com.losrobles.api.services;

import com.google.common.base.Preconditions;
import com.losrobles.api.dto.InvitacionResponseDTO;
import com.losrobles.api.models.Departamento;
import com.losrobles.api.models.Invitacion;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.models.Visitante;
import com.losrobles.api.repositories.DepartamentoRepository;
import com.losrobles.api.repositories.InvitacionRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import com.losrobles.api.repositories.VisitanteRepository;
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

    private final VisitanteRepository visitanteRepo;

    private final DepartamentoRepository departamentoRepo;

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

        // El cliente solo envía el DNI/id de referencia; se resuelven las entidades
        // completas para que la respuesta (nombre del visitante, torre/número del
        // departamento) no salga en null.
        Preconditions.checkArgument(nuevaInvi.getVisitante() != null && nuevaInvi.getVisitante().getDni() != null,
                "El DNI del visitante es obligatorio.");
        Visitante visitante = visitanteRepo.findById(nuevaInvi.getVisitante().getDni())
                .orElseThrow(() -> new RuntimeException("Error: El visitante no existe. Regístralo primero."));
        nuevaInvi.setVisitante(visitante);

        Preconditions.checkArgument(nuevaInvi.getDepartamentoDestino() != null
                        && nuevaInvi.getDepartamentoDestino().getId() != null,
                "El departamento destino es obligatorio.");
        Departamento departamento = departamentoRepo.findById(nuevaInvi.getDepartamentoDestino().getId())
                .orElseThrow(() -> new RuntimeException("Error: El departamento destino no existe."));
        nuevaInvi.setDepartamentoDestino(departamento);

        if (nuevaInvi.getCodigoQrHash() == null || nuevaInvi.getCodigoQrHash().isEmpty()) {
            nuevaInvi.setCodigoQrHash(UUID.randomUUID().toString());
        }

        Invitacion guardada = invitacionRepo.save(nuevaInvi);
        log.info("Invitación creada: id={}, anfitrion='{}', dniVisitante='{}', fechaProgramada={}",
                guardada.getId(), username, guardada.getVisitante().getDni(), guardada.getFechaProgramada());
        return mapToDTO(guardada);
    }

    public List<InvitacionResponseDTO> listarMisInvitaciones(String username) {
        return invitacionRepo.findByAnfitrion_UsernameOrderByFechaCreacionDesc(username).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
                .nombreVisitante(invi.getVisitante().getNombre() + " " + invi.getVisitante().getApellidos())
                .dniVisitante(invi.getVisitante().getDni())
                .nombreDepartamento(invi.getDepartamentoDestino().getBloqueTorre() + " - "
                        + invi.getDepartamentoDestino().getNumeroDepa())
                .fechaProgramada(invi.getFechaProgramada())
                .estado(invi.getEstado())
                .build();
    }
}