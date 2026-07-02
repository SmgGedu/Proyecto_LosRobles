package com.losrobles.api.services;

import com.losrobles.api.config.JwtUtils;
import com.losrobles.api.dto.JwtResponse;
import com.losrobles.api.dto.LoginRequest;
import com.losrobles.api.models.Rol;
import com.losrobles.api.models.Usuario;
import com.losrobles.api.repositories.RolRepository;
import com.losrobles.api.repositories.UsuarioRepository;
import com.losrobles.api.util.RoleUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final UsuarioRepository usuarioRepository;

    private final RolRepository rolRepository;

    private final PasswordEncoder passwordEncoder;

    public JwtResponse login(LoginRequest loginRequest) {
        if (StringUtils.isAnyBlank(loginRequest.getUsername(), loginRequest.getPassword())) {
            throw new IllegalArgumentException("Error: Usuario y contraseña son obligatorios.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
        } catch (BadCredentialsException ex) {
            log.warn("Intento de login fallido para el usuario '{}'", loginRequest.getUsername());
            throw ex;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Usuario usuario = usuarioRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado tras autenticación."));
        String rol = RoleUtils.normalize(usuario.getRol().getNombreRol());

        String jwt = jwtUtils.generateJwtToken(loginRequest.getUsername(), rol);
        log.info("Login exitoso: usuario='{}', rol='{}'", loginRequest.getUsername(), rol);
        return new JwtResponse(jwt, loginRequest.getUsername(), rol);
    }

    public String register(Usuario usuario) {
        if (StringUtils.isAnyBlank(usuario.getUsername(), usuario.getDni(), usuario.getPassword())) {
            throw new IllegalArgumentException("Error: Username, DNI y contraseña son obligatorios.");
        }
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new IllegalArgumentException("Error: El username ya está en uso");
        }
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new IllegalArgumentException("Error: El DNI ya está registrado");
        }
        if (usuario.getRol() == null || usuario.getRol().getId() == null) {
            throw new IllegalArgumentException("Error: Es necesario asignar un ID de rol válido.");
        }
        Rol rol = rolRepository.findById(usuario.getRol().getId())
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado en el sistema."));
        usuario.setRol(rol);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        if (usuario.getEstado() == null) {
            usuario.setEstado(true);
        }
        usuarioRepository.save(usuario);
        log.info("Nuevo usuario registrado: username='{}', rol='{}'", usuario.getUsername(), rol.getNombreRol());
        return "Usuario " + usuario.getUsername() + " registrado con éxito. Perfil asignado: " + rol.getNombreRol();
    }
}
