package com.losrobles.api.models;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "tbl_departamentos")
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departamento")
    private Integer id;

    @Column(name = "bloque_torre", length = 50)
    private String bloqueTorre;

    @Column(name = "numero_depa", length = 20)
    private String numeroDepa;

    // En tu clase Departamento.java
    @OneToMany(mappedBy = "departamento")
    @JsonIgnore
    private List<Usuario> residentes;
}