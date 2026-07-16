package com.losrobles.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class IngresoRequestDTO {
    @NotBlank(message = "El código QR es obligatorio")
    private String qrHash;
    private String placaVehiculo;
    private String observaciones;
    private List<ObjetoRequestDTO> objetos;
}