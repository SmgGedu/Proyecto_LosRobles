package com.losrobles.api.services;

import com.losrobles.api.dto.InvitacionResponseDTO;
import com.losrobles.api.models.Departamento;
import com.losrobles.api.models.Invitacion;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.models.Visitante;
import com.losrobles.api.repositories.InvitacionRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import com.losrobles.api.util.FechaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InvitacionServiceTest {

    @Mock
    private InvitacionRepository invitacionRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @InjectMocks
    private InvitacionService invitacionService;

    private Usuario residente;
    private Visitante visitante;
    private Departamento departamento;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        residente = new Usuario();
        residente.setId(1);
        residente.setUsername("jperez");
        residente.setNombres("Juan");
        residente.setApellidos("Perez");

        visitante = new Visitante();
        visitante.setDni("12345678");
        visitante.setNombre("Maria");
        visitante.setApellidos("Lopez");

        departamento = new Departamento();
        departamento.setId(10);
        departamento.setBloqueTorre("A");
        departamento.setNumeroDepa("101");
    }

    @Test
    void guardarInvitacion_conFechaPasada_lanzaExcepcion() {
        Invitacion invitacion = new Invitacion();
        invitacion.setVisitante(visitante);
        invitacion.setDepartamentoDestino(departamento);
        invitacion.setFechaProgramada(FechaUtils.hoy().minusDays(1));

        assertThatThrownBy(() -> invitacionService.guardarInvitacion(invitacion, "jperez"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha programada");
    }

    @Test
    void guardarInvitacion_conDatosValidos_generaCodigoQrYAsignaAnfitrion() {
        Invitacion invitacion = new Invitacion();
        invitacion.setVisitante(visitante);
        invitacion.setDepartamentoDestino(departamento);
        invitacion.setFechaProgramada(FechaUtils.hoy());

        when(usuarioRepo.findByUsername("jperez")).thenReturn(Optional.of(residente));
        when(invitacionRepo.save(any(Invitacion.class))).thenAnswer(invocation -> {
            Invitacion arg = invocation.getArgument(0);
            arg.setId(99);
            return arg;
        });

        InvitacionResponseDTO resultado = invitacionService.guardarInvitacion(invitacion, "jperez");

        assertThat(resultado.getId()).isEqualTo(99);
        assertThat(resultado.getCodigoQrHash()).isNotBlank();
        assertThat(resultado.getNombreAnfitrion()).isEqualTo("Juan Perez");
        assertThat(resultado.getDniVisitante()).isEqualTo("12345678");
    }
}
