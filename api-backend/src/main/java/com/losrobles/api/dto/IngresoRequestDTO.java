package com.losrobles.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class IngresoRequestDTO {
    private String qrHash;
    private String placaVehiculo;
    private String observaciones;
    private List<ObjetoRequestDTO> objetos; 
}