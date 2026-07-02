package com.losrobles.api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InvitacionDTO {
    private Integer id;
    private String codigoQrHash;
    private String nombreAnfitrion;
    private String nombreVisitante;
    private String dniVisitante;
    private String departamentoNombre;
    private LocalDate fechaProgramada;
    private String estado;
}