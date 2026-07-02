package com.losrobles.api.models;

import com.losrobles.api.util.FechaUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "tbl_objetos_registrados")
public class ObjetoRegistrado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_objeto")
    private Integer id;

    // Usamos LAZY para no sobrecargar la memoria al listar registros de acceso
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_registro", nullable = false)
    private RegistroAcceso registroAcceso;

    @NotBlank(message = "La descripción del objeto es obligatoria")
    @Column(nullable = false)
    private String descripcion;

    @Size(max = 100)
    @Column(name = "marca_modelo", length = 100)
    private String marcaModelo;

    @Size(max = 100)
    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = FechaUtils.ahora();
    }
}