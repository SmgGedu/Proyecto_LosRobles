package com.losrobles.api.services;

import com.losrobles.api.config.JwtUtils;
import com.losrobles.api.dto.JwtResponse;
import com.losrobles.api.dto.LoginRequest;
import com.losrobles.api.dto.SignupRequest;
import com.losrobles.api.models.Rol;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.repositories.RolRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Rol rolResidente;
    private Rol rolAdministrador;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        rolResidente = new Rol();
        rolResidente.setId(2);
        rolResidente.setNombreRol("Residente");

        rolAdministrador = new Rol();
        rolAdministrador.setId(1);
        rolAdministrador.setNombreRol("Administrador");
    }

    @Test
    void login_conCredencialesValidas_retornaJwtConRolDelUsuario() {
        LoginRequest request = new LoginRequest("jperez", "clave123");
        Usuario usuario = new Usuario();
        usuario.setUsername("jperez");
        usuario.setRol(rolResidente);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(usuario));
        when(jwtUtils.generateJwtToken("jperez", "Residente")).thenReturn("token-falso");

        JwtResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("token-falso");
        assertThat(response.getUsername()).isEqualTo("jperez");
        assertThat(response.getRole()).isEqualTo("Residente");
    }

    @Test
    void login_conCredencialesInvalidas_propagaBadCredentialsException() {
        LoginRequest request = new LoginRequest("jperez", "claveIncorrecta");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    /**
     * Anti-INC-01 (CP-11): el registro público debe ignorar cualquier rol recibido
     * en la petición y asignar SIEMPRE el rol RESIDENTE, incluso si el atacante
     * intenta fijar el rol Administrador vía el DTO o cualquier otro campo.
     */
    @Test
    void register_conIntentoDeAutoAsignarseAdministrador_creaUsuarioComoResidente() {
        SignupRequest request = new SignupRequest();
        request.setUsername("atacante");
        request.setDni("87654321");
        request.setPassword("clave123");
        request.setNombres("Mal");
        request.setApellidos("Actor");

        when(usuarioRepository.existsByUsername("atacante")).thenReturn(false);
        when(usuarioRepository.existsByDni("87654321")).thenReturn(false);
        when(rolRepository.findByNombreRolIgnoreCase("Residente")).thenReturn(Optional.of(rolResidente));
        when(passwordEncoder.encode("clave123")).thenReturn("hash-encriptado");

        authService.register(request);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        org.mockito.Mockito.verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario guardado = usuarioCaptor.getValue();
        assertThat(guardado.getRol().getNombreRol()).isEqualTo("Residente");
        assertThat(guardado.getRol().getId()).isNotEqualTo(rolAdministrador.getId());
        assertThat(guardado.getPassword()).isEqualTo("hash-encriptado");
    }

    @Test
    void register_conUsernameYaUsado_lanzaExcepcion() {
        SignupRequest request = new SignupRequest();
        request.setUsername("existente");
        request.setDni("11111111");
        request.setPassword("clave123");

        when(usuarioRepository.existsByUsername("existente")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("username ya está en uso");
    }
}
