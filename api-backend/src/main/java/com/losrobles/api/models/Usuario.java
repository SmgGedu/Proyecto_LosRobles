package com.losrobles.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.losrobles.api.util.FechaUtils;
import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true, length = 15)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 15)
    private String telefono;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false)
    private String password;

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