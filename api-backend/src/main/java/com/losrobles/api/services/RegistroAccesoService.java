package com.losrobles.api.services;

import com.losrobles.api.dto.*;
import com.losrobles.api.models.*;
import com.losrobles.api.repositories.*;
import com.losrobles.api.util.FechaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio central para la gestión de accesos en Condominio Los Robles.
 * Optimizado para gestión de familias (1:N) y trazabilidad múltiple de objetos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroAccesoService {

    private final RegistroAccesoRepository registroRepo;
    private final InvitacionRepository invitacionRepo;
    private final UsuarioRepository usuarioRepo;
    private final ObjetoRegistradoRepository objetoRepo;
    private final VisitanteRepository visitanteRepo;
    private final DepartamentoRepository departamentoRepo;
    private final EventoSeguridadRepository eventoSeguridadRepo;

    /**
     * Registra entrada mediante QR validando la vinculación actual del residente
     * con el departamento.
     */
    @Transactional
    public AccesoResponseDTO registrarEntradaQR(IngresoRequestDTO request, String usernameConserje) {
        Usuario conserje = usuarioRepo.findByUsername(usernameConserje)
                .orElseThrow(() -> new RuntimeException("Error: Conserje no encontrado en el sistema."));

        Invitacion invi = invitacionRepo.findByCodigoQrHash(request.getQrHash())
                .orElseThrow(() -> {
                    registrarEvento("QR_INVALIDO", request.getQrHash(), null, conserje,
                            "Código QR no encontrado en el sistema.");
                    return new RuntimeException("Error: QR no válido o no encontrado.");
                });

        if (!"PENDIENTE".equals(invi.getEstado())) {
            registrarEvento("QR_YA_USADO", request.getQrHash(), invi.getVisitante().getDni(), conserje,
                    "Intento de reutilización de QR con estado: " + invi.getEstado());
            throw new RuntimeException("Error: Esta invitación ya fue utilizada o está inactiva.");
        }

        if (invi.getHoraExpiracion() != null && FechaUtils.ahora().isAfter(invi.getHoraExpiracion())) {
            invi.setEstado("EXPIRADA");
            invitacionRepo.save(invi);
            registrarEvento("QR_EXPIRADO", request.getQrHash(), invi.getVisitante().getDni(), conserje,
                    "El QR expiró el " + invi.getHoraExpiracion());
            throw new RuntimeException("Error: Este código QR ha expirado.");
        }

        if (Boolean.TRUE.equals(invi.getVisitante().getBloqueado())) {
            registrarEvento("VISITANTE_BLOQUEADO", request.getQrHash(), invi.getVisitante().getDni(), conserje,
                    "Motivo: " + invi.getVisitante().getMotivoBloqueo());
            throw new RuntimeException("Error de Seguridad: Este visitante tiene acceso restringido. Motivo: "
                    + invi.getVisitante().getMotivoBloqueo());
        }

        // VALIDACIÓN DE FAMILIA: Verifica que el residente que invita aún viva en ese
        // departamento
        if (invi.getAnfitrion().getDepartamento() == null ||
                !invi.getAnfitrion().getDepartamento().getId().equals(invi.getDepartamentoDestino().getId())) {
            registrarEvento("RESIDENTE_NO_VINCULADO", request.getQrHash(), invi.getVisitante().getDni(), conserje,
                    "El anfitrión ya no pertenece al departamento de destino.");
            throw new RuntimeException(
                    "Error de Seguridad: El residente anfitrión ya no pertenece a este departamento.");
        }

        RegistroAcceso nuevo = new RegistroAcceso();
        nuevo.setVisitante(invi.getVisitante());
        nuevo.setDepartamentoDestino(invi.getDepartamentoDestino());
        nuevo.setResidenteQueAutoriza(invi.getAnfitrion());
        nuevo.setConserjeEnTurno(conserje);
        nuevo.setInvitacion(invi);
        nuevo.setTipoIngreso("QR");
        nuevo.setTipoVisita(determinarTipoVisita(invi.getVisitante()));
        nuevo.setPlacaVehiculo(request.getPlacaVehiculo());
        nuevo.setObservaciones(request.getObservaciones());
        nuevo.setEstadoAcceso("ACTIVO");
        nuevo.setHoraIngreso(FechaUtils.ahora());

        // "Quemar" la invitación
        invi.setEstado("UTILIZADA");
        invitacionRepo.save(invi);

        RegistroAcceso guardado = registroRepo.save(nuevo);
        log.info("Ingreso QR registrado: idRegistro={}, dniVisitante='{}', departamento='{}', conserje='{}'",
                guardado.getId(), guardado.getVisitante().getDni(),
                guardado.getDepartamentoDestino().getNumeroDepa(), usernameConserje);

        // Registro de múltiples objetos traídos por el visitante
        if (request.getObjetos() != null && !request.getObjetos().isEmpty()) {
            registrarListaDeObjetos(request.getObjetos(), guardado);
        }

        return mapToDTO(guardado);
    }

    /**
     * Registro manual permitiendo múltiples objetos y vinculación directa con el
     * departamento en Ica.
     */
    @Transactional
    public AccesoResponseDTO registrarEntradaManual(RegistroCompletoRequest request, String usernameConserje) {
        Usuario conserje = usuarioRepo.findByUsername(usernameConserje)
                .orElseThrow(() -> new RuntimeException("Error: Conserje no encontrado."));

        // Buscar o crear visitante
        Visitante visitante = visitanteRepo.findByDni(request.getDni_visitante())
                .orElseGet(() -> {
                    Visitante nuevoV = new Visitante();
                    nuevoV.setDni(request.getDni_visitante());
                    nuevoV.setNombre(request.getNombres());
                    nuevoV.setApellidos(request.getApellidos());
                    return nuevoV;
                });

        if (Boolean.TRUE.equals(visitante.getBloqueado())) {
            registrarEvento("VISITANTE_BLOQUEADO", null, visitante.getDni(), conserje,
                    "Intento de registro manual. Motivo: " + visitante.getMotivoBloqueo());
            throw new RuntimeException("Error de Seguridad: Este visitante tiene acceso restringido. Motivo: "
                    + visitante.getMotivoBloqueo());
        }

        // Actualizar flags según tipo de visita
        String tipoVisita = request.getTipo_visita();
        if ("FRECUENTE".equalsIgnoreCase(tipoVisita)) {
            visitante.setEsFrecuente(true);
        }
        if ("DELIVERY".equalsIgnoreCase(tipoVisita)) {
            visitante.setEmpresaDelivery(request.getEmpresa_delivery());
        }
        visitante = visitanteRepo.save(visitante);

        Departamento depto = departamentoRepo.findById(request.getId_departamento_destino())
                .orElseThrow(() -> new RuntimeException("Error: El departamento destino no existe."));

        RegistroAcceso nuevo = new RegistroAcceso();
        nuevo.setVisitante(visitante);
        nuevo.setDepartamentoDestino(depto);
        nuevo.setConserjeEnTurno(conserje);
        nuevo.setTipoIngreso(request.getTipo_ingreso() != null ? request.getTipo_ingreso().toUpperCase() : "MANUAL");
        nuevo.setTipoVisita(tipoVisita != null ? tipoVisita.toUpperCase() : "NORMAL");
        nuevo.setPlacaVehiculo(request.getPlaca_vehiculo());
        nuevo.setObservaciones(request.getObservaciones());
        nuevo.setEstadoAcceso("ACTIVO");
        nuevo.setHoraIngreso(FechaUtils.ahora());

        RegistroAcceso guardado = registroRepo.save(nuevo);

        // Procesar la lista dinámica de objetos desde el frontend
        List<ObjetoRequestDTO> objetos = extractObjetosFromRequest(request);
        if (objetos != null && !objetos.isEmpty()) {
            objetos.forEach(objDto -> {
                ObjetoRegistrado obj = ObjetoRegistrado.builder()
                        .registroAcceso(guardado)
                        .descripcion(objDto.getDescripcion())
                        .marcaModelo(objDto.getMarcaModelo())
                        .numeroSerie(objDto.getNumeroSerie())
                        .build();
                objetoRepo.save(obj);
            });
        }

        return mapToDTO(guardado);
    }

    /**
     * Registra un evento de seguridad (QR inválido/expirado/reutilizado,
     * visitante bloqueado, etc.) para trazabilidad y auditoría.
     */
    private void registrarEvento(String tipoEvento, String codigoQr, String dniVisitante, Usuario conserje,
            String detalle) {
        log.warn("Evento de seguridad '{}' detectado por conserje '{}': {}",
                tipoEvento, conserje != null ? conserje.getUsername() : "desconocido", detalle);
        EventoSeguridad evento = EventoSeguridad.builder()
                .tipoEvento(tipoEvento)
                .codigoQrIntentado(codigoQr)
                .dniVisitante(dniVisitante)
                .conserje(conserje)
                .detalle(detalle)
                .build();
        eventoSeguridadRepo.save(evento);
    }

    /**
     * Determina el tipo de visita vigente para un visitante en base a sus flags
     * actuales. Se usa al ingresar por QR, donde el conserje no elige el tipo.
     */
    private String determinarTipoVisita(Visitante visitante) {
        if (visitante.getEmpresaDelivery() != null && !visitante.getEmpresaDelivery().isBlank()) {
            return "DELIVERY";
        }
        if (Boolean.TRUE.equals(visitante.getEsFrecuente())) {
            return "FRECUENTE";
        }
        return "NORMAL";
    }

    /**
     * Método auxiliar para persistir la lista de objetos vinculados a un acceso.
     */
    private void registrarListaDeObjetos(List<ObjetoRequestDTO> objetos, RegistroAcceso registro) {
        objetos.forEach(objDto -> {
            ObjetoRegistrado obj = ObjetoRegistrado.builder()
                    .registroAcceso(registro)
                    .descripcion(objDto.getDescripcion())
                    .marcaModelo(objDto.getMarcaModelo())
                    .numeroSerie(objDto.getNumeroSerie())
                    .build();
            objetoRepo.save(obj);
        });
    }

    @SuppressWarnings("unchecked")
    private List<ObjetoRequestDTO> extractObjetosFromRequest(RegistroCompletoRequest request) {
        try {
            Method getter = request.getClass().getMethod("getObjetos");
            Object result = getter.invoke(request);
            if (result instanceof List<?>) {
                return (List<ObjetoRequestDTO>) result;
            }
        } catch (NoSuchMethodException ignored) {
            // El request no contiene una lista de objetos con ese nombre.
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error al extraer la lista de objetos del request", e);
        }
        return null;
    }

    public List<AccesoResponseDTO> obtenerVisitantesActivos() {
        return registroRepo.findByEstadoAcceso("ACTIVO").stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public long contarVisitantesEnEdificio() {
        return registroRepo.countByEstadoAcceso("ACTIVO");
    }

    public List<AccesoResponseDTO> listarTodos() {
        return registroRepo.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public AccesoResponseDTO registrarSalida(Integer id) {
        RegistroAcceso ra = registroRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Registro no encontrado"));
        ra.setHoraSalida(FechaUtils.ahora());
        ra.setEstadoAcceso("FINALIZADO");
        return mapToDTO(registroRepo.save(ra));
    }

    /**
     * Mapper que transforma la entidad en un DTO formateado para el Dashboard.
     */
    private AccesoResponseDTO mapToDTO(RegistroAcceso ra) {
        List<ObjetoResponseDTO> objetosDTO = objetoRepo.findByRegistroAcceso(ra).stream()
                .map(o -> ObjetoResponseDTO.builder()
                        .id(o.getId())
                        .descripcion(o.getDescripcion())
                        .marcaModelo(o.getMarcaModelo())
                        .numeroSerie(o.getNumeroSerie())
                        .build())
                .collect(Collectors.toList());

        String nombreVisitante = ra.getVisitante().getNombre() + " " + ra.getVisitante().getApellidos();

        String nombreResidente = ra.getResidenteQueAutoriza() != null
                ? ra.getResidenteQueAutoriza().getNombres() + " " + ra.getResidenteQueAutoriza().getApellidos()
                : "INGRESO MANUAL";

        String infoDepartamento = ra.getDepartamentoDestino() != null
                ? ra.getDepartamentoDestino().getBloqueTorre() + " - " + ra.getDepartamentoDestino().getNumeroDepa()
                : "N/A";

        return AccesoResponseDTO.builder()
                .id(ra.getId())
                .nombreVisitante(nombreVisitante)
                .dniVisitante(ra.getVisitante().getDni())
                .nombreAnfitrion(nombreResidente)
                .departamento(infoDepartamento)
                .nombreConserje(ra.getConserjeEnTurno().getNombres())
                .tipoIngreso(ra.getTipoIngreso())
                .tipoVisita(ra.getTipoVisita())
                .horaEntrada(ra.getHoraIngreso())
                .horaSalida(ra.getHoraSalida())
                .estadoAcceso(ra.getEstadoAcceso())
                .placaVehiculo(ra.getPlacaVehiculo())
                .observaciones(ra.getObservaciones())
                .objetos(objetosDTO)
                .build();
    }
}