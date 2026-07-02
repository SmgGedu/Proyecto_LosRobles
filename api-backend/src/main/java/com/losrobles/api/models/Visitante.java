package com.losrobles.api.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_visitantes")
public class Visitante {
    @Id
    @Column(name = "dni_visitante", length = 15)
    private String dni;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String empresaDelivery;
    private Boolean esFrecuente = false;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean bloqueado = false;

    @Column(name = "motivo_bloqueo", length = 255)
    private String motivoBloqueo;
}