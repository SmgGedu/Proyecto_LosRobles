package com.losrobles.api.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id") // <--- Este es el nombre real en tu Azure
    private Integer id;

    // Aquí está la clave: coincidir con el nombre de tu tabla
    @Column(name = "nombre_rol", nullable = false, length = 50)
    private String nombreRol; 
}