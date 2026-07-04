package com.losrobles.api.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_configuracion_aforo")
public class ConfiguracionAforo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private Integer id;

    @Column(name = "clave", nullable = false, unique = true, length = 50)
    private String clave;

    @Column(name = "valor", nullable = false, length = 255)
    private String valor;
}
