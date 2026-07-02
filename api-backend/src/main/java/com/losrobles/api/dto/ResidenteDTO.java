package com.losrobles.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResidenteDTO {
    private Integer idUsuario;
    private String nombreCompleto;
    private String infoDepartamento; // Ejemplo: "Torre A - 101"
    private Integer idDepartamento; // Para llenar el selector automáticamente
}