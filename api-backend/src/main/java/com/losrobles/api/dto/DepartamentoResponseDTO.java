package com.losrobles.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartamentoResponseDTO {
    private Integer id;
    private String bloqueTorre;
    private String numeroDepa;
    private String residente; // Nombre completo del residente o "No tiene"
    private String estado; // "Ocupado" u "Libre"
}
