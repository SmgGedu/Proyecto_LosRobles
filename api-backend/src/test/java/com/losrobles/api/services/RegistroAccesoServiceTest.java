package com.losrobles.api.services;

import com.losrobles.api.dto.AccesoResponseDTO;
import com.losrobles.api.dto.IngresoRequestDTO;
import com.losrobles.api.models.*;
import com.losrobles.api.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegistroAccesoServiceTest {

    @Mock
    private RegistroAccesoRepository registroRepo;
    @Mock
    private InvitacionRepository invitacionRepo;
    @Mock
    private UsuarioRepository usuarioRepo;
    @Mock
    private ObjetoRegistradoRepository objetoRepo;
    @Mock
    private VisitanteRepository visitanteRepo;
    @Mock
    private DepartamentoRepository departamentoRepo;
    @Mock
    private EventoSeguridadRepository eventoSeguridadRepo;

    @InjectMocks
    private RegistroAccesoService registroAccesoService;

    private Usuario conserje;
    private Usuario anfitrion;
    private Departamento departamento;
    private Visitante visitante;
    private Invitacion invitacion;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        conserje = new Usuario();
        conserje.setId(1);
        conserje.setUsername("conserje1");

        departamento = new Departamento();
        departamento.setId(10);
        departamento.setBloqueTorre("A");
        departamento.setNumeroDepa("101");

        anfitrion = new Usuario();
        anfitrion.setId(2);
        anfitrion.setNombres("Juan");
        anfitrion.setApellidos("Perez");
        anfitrion.setDepartamento(departamento);

        visitante = new Visitante();
        visitante.setDni("12345678");
        visitante.setNombre("Maria");
        visitante.setApellidos("Lopez");
        visitante.setBloqueado(false);

        invitacion = new Invitacion();
        invitacion.setId(5);
        invitacion.setCodigoQrHash("qr-valido");
        invitacion.setEstado("PENDIENTE");
        invitacion.setAnfitrion(anfitrion);
        invitacion.setVisitante(visitante);
        invitacion.setDepartamentoDestino(departamento);
        invitacion.setFechaProgramada(LocalDate.now());

        when(usuarioRepo.findByUsername("conserje1")).thenReturn(Optional.of(conserje));
    }

    @Test
    void registrarEntradaQR_conVisitanteBloqueado_lanzaExcepcionYRegistraEvento() {
        visitante.setBloqueado(true);
        visitante.setMotivoBloqueo("Reporte de robo");

        IngresoRequestDTO request = new IngresoRequestDTO();
        request.setQrHash("qr-valido");

        when(invitacionRepo.findByCodigoQrHash("qr-valido")).thenReturn(Optional.of(invitacion));

        assertThatThrownBy(() -> registroAccesoService.registrarEntradaQR(request, "conserje1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("acceso restringido");

        ArgumentCaptor<EventoSeguridad> eventoCaptor = ArgumentCaptor.forClass(EventoSeguridad.class);
        verify(eventoSeguridadRepo).save(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getTipoEvento()).isEqualTo("VISITANTE_BLOQUEADO");

        verify(registroRepo, never()).save(any(RegistroAcceso.class));
    }

    @Test
    void registrarEntradaQR_conQrInexistente_lanzaExcepcionYRegistraEvento() {
        IngresoRequestDTO request = new IngresoRequestDTO();
        request.setQrHash("qr-invalido");

        when(invitacionRepo.findByCodigoQrHash("qr-invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registroAccesoService.registrarEntradaQR(request, "conserje1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("QR no válido");

        ArgumentCaptor<EventoSeguridad> eventoCaptor = ArgumentCaptor.forClass(EventoSeguridad.class);
        verify(eventoSeguridadRepo).save(eventoCaptor.capture());
        assertThat(eventoCaptor.getValue().getTipoEvento()).isEqualTo("QR_INVALIDO");
    }

    @Test
    void registrarEntradaQR_conDatosValidos_creaRegistroDeAcceso() {
        IngresoRequestDTO request = new IngresoRequestDTO();
        request.setQrHash("qr-valido");

        when(invitacionRepo.findByCodigoQrHash("qr-valido")).thenReturn(Optional.of(invitacion));
        when(registroRepo.save(any(RegistroAcceso.class))).thenAnswer(invocation -> {
            RegistroAcceso ra = invocation.getArgument(0);
            ra.setId(100);
            return ra;
        });
        when(objetoRepo.findByRegistroAcceso(any(RegistroAcceso.class))).thenReturn(Collections.emptyList());

        AccesoResponseDTO resultado = registroAccesoService.registrarEntradaQR(request, "conserje1");

        assertThat(resultado.getId()).isEqualTo(100);
        assertThat(resultado.getDniVisitante()).isEqualTo("12345678");
        assertThat(resultado.getTipoIngreso()).isEqualTo("QR");
        assertThat(invitacion.getEstado()).isEqualTo("UTILIZADA");

        verify(eventoSeguridadRepo, never()).save(any(EventoSeguridad.class));
    }
}
