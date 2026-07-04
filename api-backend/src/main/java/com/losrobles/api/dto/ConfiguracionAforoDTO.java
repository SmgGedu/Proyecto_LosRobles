package com.losrobles.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfiguracionAforoDTO {
    private int aforoMaximo;
    private int tiempoMaximoVisitaMinutos;
}
