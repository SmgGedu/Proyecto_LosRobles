package com.losrobles.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "El bloque/torre es obligatorio")
    @Size(max = 50, message = "El bloque/torre no puede superar los 50 caracteres")
    @Column(name = "bloque_torre", length = 50)
    private String bloqueTorre;

    @NotBlank(message = "El número de departamento es obligatorio")
    @Size(max = 20, message = "El número de departamento no puede superar los 20 caracteres")
    @Column(name = "numero_depa", length = 20)
    private String numeroDepa;

    // En tu clase Departamento.java
    @OneToMany(mappedBy = "departamento")
    @JsonIgnore
    private List<Usuario> residentes;
}