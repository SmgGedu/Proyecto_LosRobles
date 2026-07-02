package com.losrobles.api.dto;

public class DashboardStatsDTO {
    private long visitantesHoy;
    private long activosAhora;
    private long totalResidentes;
    private String ultimoIngreso;

    // Constructor
    public DashboardStatsDTO(long visitantesHoy, long activosAhora, long totalResidentes, String ultimoIngreso) {
        this.visitantesHoy = visitantesHoy;
        this.activosAhora = activosAhora;
        this.totalResidentes = totalResidentes;
        this.ultimoIngreso = ultimoIngreso;
    }

    // Getters y Setters
    public long getVisitantesHoy() {
        return visitantesHoy;
    }

    public long getActivosAhora() {
        return activosAhora;
    }

    public long getTotalResidentes() {
        return totalResidentes;
    }

    public String getUltimoIngreso() {
        return ultimoIngreso;
    }
}
