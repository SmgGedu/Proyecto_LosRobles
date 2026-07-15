package com.losrobles.api.dto;

import lombok.Data;

/**
 * Cuerpo del registro público (POST /api/auth/signup). A propósito NO tiene
 * campo de rol: el registro público solo puede crear cuentas RESIDENTE. Dar
 * de alta Conserjes o Administradores requiere el endpoint protegido
 * POST /api/usuarios (ROLE_Administrador).
 */
@Data
public class SignupRequest {
    private String dni;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String username;
    private String password;
    private String email;
}
