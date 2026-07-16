package com.losrobles.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.losrobles.api.util.FechaUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tbl_usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(max = 15, message = "El DNI no puede superar los 15 caracteres")
    @Column(nullable = false, unique = true, length = 15)
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @Size(max = 15, message = "El teléfono no puede superar los 15 caracteres")
    @Column(length = 15)
    private String telefono;

    @NotBlank(message = "El username es obligatorio")
    @Size(max = 50, message = "El username no puede superar los 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false)
    private String password;

    @Email(message = "El email no tiene un formato válido")
    @Size(max = 100, message = "El email no puede superar los 100 caracteres")
    @Column(unique = true, length = 100)
    private String email;

    @Column(columnDefinition = "BIT DEFAULT 1")
    private Boolean estado;

    // Marca cuentas "eliminadas": se anonimizan en vez de borrarse de la BD
    // para no romper las claves foráneas de tablas con historial (accesos,
    // invitaciones, eventos de seguridad, dispositivos push, etc.).
    @Column(columnDefinition = "BIT DEFAULT 0")
    private Boolean eliminado;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación con el Rol (Muchos usuarios tienen un Rol)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private Rol rol;

    // NUEVA RELACIÓN: Muchos usuarios pertenecen a un departamento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento")
    private Departamento departamento;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = FechaUtils.ahora();
        if (this.estado == null)
            this.estado = true;
    }
}