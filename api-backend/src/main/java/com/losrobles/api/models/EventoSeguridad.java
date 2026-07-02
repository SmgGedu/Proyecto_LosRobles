package com.losrobles.api.models;

import com.losrobles.api.util.FechaUtils;
import jakarta.persistence.*;
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
@Table(name = "tbl_eventos_seguridad")
public class EventoSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Integer id;

    @Column(name = "tipo_evento", nullable = false, length = 30)
    private String tipoEvento; // QR_INVALIDO, QR_EXPIRADO, QR_YA_USADO, VISITANTE_BLOQUEADO, RESIDENTE_NO_VINCULADO

    @Column(name = "codigo_qr_intentado")
    private String codigoQrIntentado;

    @Column(name = "dni_visitante", length = 15)
    private String dniVisitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conserje")
    private Usuario conserje;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "fecha_hora", updatable = false)
    private LocalDateTime fechaHora;

    @PrePersist
    protected void onCreate() {
        if (this.fechaHora == null) {
            this.fechaHora = FechaUtils.ahora();
        }
    }
}
