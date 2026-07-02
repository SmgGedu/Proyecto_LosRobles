package com.losrobles.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class InvitacionResponseDTO {
    private Integer id;
    private String codigoQrHash;
    private String nombreAnfitrion;
    private String nombreVisitante;
    private String dniVisitante;
    private String nombreDepartamento;
    private LocalDate fechaProgramada;
    private String estado;
}