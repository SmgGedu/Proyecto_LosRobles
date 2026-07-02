package com.losrobles.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AccesoResponseDTO {
    private Integer id;
    private String nombreVisitante;
    private String dniVisitante;
    private String nombreAnfitrion;
    private String departamento;
    private String nombreConserje;
    private String placaVehiculo;
    private String tipoIngreso;
    private String tipoVisita;
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
    private String estadoAcceso;
    private String observaciones;
    private List<ObjetoResponseDTO> objetos;
}