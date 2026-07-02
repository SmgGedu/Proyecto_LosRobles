package com.losrobles.api.dto;

import java.util.List;

public class RegistroCompletoRequest {
    // Visitante
    private String dni_visitante;
    private String nombres;
    private String apellidos;

    // Acceso
    private Integer id_departamento_destino;
    private Integer id_residente_que_autoriza; // NUEVO: ID del amigo/anfitrión
    private String placa_vehiculo;
    private String observaciones;
    private String tipo_ingreso;

    // Tipo de visita: NORMAL, FRECUENTE, DELIVERY
    private String tipo_visita;
    private String empresa_delivery;

    // Objetos (NUEVO: Ahora es una lista múltiple)
    private List<ObjetoRequestDTO> objetos;

    // --- GETTERS Y SETTERS ---

    public String getDni_visitante() {
        return dni_visitante;
    }

    public void setDni_visitante(String dni_visitante) {
        this.dni_visitante = dni_visitante;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Integer getId_departamento_destino() {
        return id_departamento_destino;
    }

    public void setId_departamento_destino(Integer id_departamento_destino) {
        this.id_departamento_destino = id_departamento_destino;
    }

    public Integer getId_residente_que_autoriza() {
        return id_residente_que_autoriza;
    }

    public void setId_residente_que_autoriza(Integer id_residente_que_autoriza) {
        this.id_residente_que_autoriza = id_residente_que_autoriza;
    }

    public String getPlaca_vehiculo() {
        return placa_vehiculo;
    }

    public void setPlaca_vehiculo(String placa_vehiculo) {
        this.placa_vehiculo = placa_vehiculo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getTipo_ingreso() {
        return tipo_ingreso;
    }

    public void setTipo_ingreso(String tipo_ingreso) {
        this.tipo_ingreso = tipo_ingreso;
    }

    public String getTipo_visita() {
        return tipo_visita;
    }

    public void setTipo_visita(String tipo_visita) {
        this.tipo_visita = tipo_visita;
    }

    public String getEmpresa_delivery() {
        return empresa_delivery;
    }

    public void setEmpresa_delivery(String empresa_delivery) {
        this.empresa_delivery = empresa_delivery;
    }

    public List<ObjetoRequestDTO> getObjetos() {
        return objetos;
    }

    public void setObjetos(List<ObjetoRequestDTO> objetos) {
        this.objetos = objetos;
    }
}