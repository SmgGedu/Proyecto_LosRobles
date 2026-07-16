package com.losrobles.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_visitantes")
public class Visitante {
    @Id
    @NotBlank(message = "El DNI del visitante es obligatorio")
    @Size(max = 15, message = "El DNI no puede superar los 15 caracteres")
    @Column(name = "dni_visitante", length = 15)
    private String dni;

    @NotBlank(message = "El nombre del visitante es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "Los apellidos del visitante son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    private String apellidos;

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
    private String telefono;

    @Size(max = 100, message = "La empresa de delivery no puede superar los 100 caracteres")
    private String empresaDelivery;
    private Boolean esFrecuente = false;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean bloqueado = false;

    @Column(name = "motivo_bloqueo", length = 255)
    private String motivoBloqueo;
}