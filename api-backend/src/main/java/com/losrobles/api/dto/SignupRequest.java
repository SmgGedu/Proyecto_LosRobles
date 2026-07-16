package com.losrobles.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Cuerpo del registro público (POST /api/auth/signup). A propósito NO tiene
 * campo de rol: el registro público solo puede crear cuentas RESIDENTE. Dar
 * de alta Conserjes o Administradores requiere el endpoint protegido
 * POST /api/usuarios (ROLE_Administrador).
 */
@Data
public class SignupRequest {
    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 15, message = "El DNI no puede superar los 15 caracteres")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    private String apellidos;

    @Size(max = 15, message = "El teléfono no puede superar los 15 caracteres")
    private String telefono;

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no puede superar los 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;

    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    private String email;
}
