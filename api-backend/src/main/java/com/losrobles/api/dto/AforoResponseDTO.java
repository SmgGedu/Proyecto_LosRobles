package com.losrobles.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AforoResponseDTO {
    private long totalEnEdificio;
    private int aforoMaximo;
    private boolean aforoExcedido;
    private List<ZonaAforoDTO> desglosePorZona;
    private List<AccesoResponseDTO> visitantes;

    @Data
    @Builder
    public static class ZonaAforoDTO {
        private String zona;
        private long cantidad;
    }
}
