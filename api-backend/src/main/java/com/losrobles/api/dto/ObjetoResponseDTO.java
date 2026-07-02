package com.losrobles.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ObjetoResponseDTO {
    private Integer id;
    private String descripcion;
    private String marcaModelo;
    private String numeroSerie;
}