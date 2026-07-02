package com.losrobles.api.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ObjetoRequestDTO {
    private String descripcion;

    @JsonProperty("marca")
    private String marcaModelo;

    @JsonProperty("serie")
    private String numeroSerie;
}