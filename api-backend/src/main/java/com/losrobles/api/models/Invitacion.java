package com.losrobles.api.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class) // Para auditoría automática
@Table(name = "tbl_invitaciones")
public class Invitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_invitacion")
    private Integer id;

    @Column(unique = true, nullable = false)
    private String codigoQrHash;

    @NotNull(message = "El anfitrión es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY) // Mejor performance al escalar
    @JoinColumn(name = "id_residente_anfitrion", nullable = false)
    private Usuario anfitrion;

    @NotNull(message = "El visitante es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_visitante", nullable = false)
    private Visitante visitante;

    @NotNull(message = "El departamento es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento_destino", nullable = false)
    private Departamento departamentoDestino;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDate fechaProgramada;

    @Column(name = "hora_expiracion")
    private LocalDateTime horaExpiracion;

    @Column(length = 20)
    @Builder.Default
    private String estado = "PENDIENTE";

    @CreatedDate // Azure grabará la fecha de creación automáticamente
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;
}