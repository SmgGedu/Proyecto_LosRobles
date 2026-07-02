package com.losrobles.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioResponseDTO {
    private Integer id;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String username;
    private String email;
    private Boolean estado;
    private Integer rolId;
    private String rolNombre;
    private Integer departamentoId;
    private String departamentoNumero;
    private String departamentoTorre;
}
