package com.losrobles.api.models;

import com.losrobles.api.util.FechaUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_registro_accesos")
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer id;

    @NotNull(message = "El visitante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_visitante", nullable = false)
    private Visitante visitante;

    @NotNull(message = "El departamento de destino es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento_destino", nullable = false)
    private Departamento departamentoDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_residente_que_autoriza")
    private Usuario residenteQueAutoriza;

    @NotNull(message = "El conserje en turno es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conserje_en_turno", nullable = false)
    private Usuario conserjeEnTurno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_invitacion")
    private Invitacion invitacion;

    @NotNull
    @Size(max = 20)
    @Column(name = "tipo_ingreso", nullable = false, length = 20)
    private String tipoIngreso; // Ejemplos: "QR", "MANUAL", "VEHICULAR"

    @Size(max = 20)
    @Column(name = "tipo_visita", length = 20)
    private String tipoVisita; // Ejemplos: "NORMAL", "FRECUENTE", "DELIVERY"

    @Column(name = "hora_ingreso", updatable = false)
    private LocalDateTime horaIngreso;

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Size(max = 15)
    @Column(name = "placa_vehiculo", length = 15)
    private String placaVehiculo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Builder.Default
    @Column(name = "estado_acceso", length = 20)
    private String estadoAcceso = "ACTIVO"; // Estados: "ACTIVO", "FINALIZADO"

    @PrePersist
    protected void onCreate() {
        if (this.horaIngreso == null) {
            this.horaIngreso = FechaUtils.ahora();
        }
        if (this.estadoAcceso == null) {
            this.estadoAcceso = "ACTIVO";
        }
    }
}