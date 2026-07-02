package com.losrobles.api.services;

import com.losrobles.api.models.Usuario;
import com.losrobles.api.repositories.UsuarioRepository;
import com.losrobles.api.util.RoleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Servicio fundamental para la seguridad de la aplicación.
 * Implementa la interfaz UserDetailsService de Spring Security.
 * Su responsabilidad exclusiva es buscar en la base de datos (Azure SQL) 
 * al usuario que intenta iniciar sesión y "traducirlo" al formato que Spring entiende.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga los datos del usuario basándose en su nombre de usuario.
     * Este método es llamado automáticamente por el AuthenticationManager durante el Login.
     * 
     * @param username El nombre de usuario enviado desde el cliente (ej. "admin_fer").
     * @return UserDetails Un objeto interno de Spring con credenciales y permisos.
     * @throws UsernameNotFoundException Si el usuario no existe en la base de datos.
     */
    @Override
    @Transactional // Mantiene la sesión de la base de datos abierta para cargar el Rol asociado correctamente
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // 1. Búsqueda del usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Error: El usuario [" + username + "] no fue encontrado."));

        // 2. Limpieza de datos (Prevención de errores por Azure SQL)
        // Azure a veces rellena los campos CHAR/VARCHAR con espacios en blanco invisibles al final.
        // El uso de .trim() es VITAL para asegurar que el hash coincida perfectamente con BCrypt.
        String hashDB = usuario.getPassword().trim(); 
        String usernameDB = usuario.getUsername().trim();

        // 3. Configuración de Roles y Permisos
        // Se normaliza a "Capitalizado" (ej. "conserje" -> "Conserje") para que coincida
        // con la convención usada en @PreAuthorize, sin importar cómo esté guardado en la BD.
        String nombreRol = RoleUtils.normalize(usuario.getRol().getNombreRol());

        // Spring Security requiere por convención que todos los roles comiencen con el prefijo "ROLE_"
        // Si en la base de datos dice "Administrador", lo convertimos dinámicamente a "ROLE_Administrador"
        if (!nombreRol.startsWith("ROLE_")) {
            nombreRol = "ROLE_" + nombreRol;
        }

        // 4. Construcción y retorno del "User" (Clase interna de Spring Security)
        // Spring tomará este objeto, comparará la contraseña enviada con este 'hashDB',
        // y si coinciden, emitirá el Token JWT otorgando los permisos definidos en 'nombreRol'.
        return new User(
                usernameDB,
                hashDB,
                Collections.singletonList(new SimpleGrantedAuthority(nombreRol))
        );
    }
}