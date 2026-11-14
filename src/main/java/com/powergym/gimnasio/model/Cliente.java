package com.powergym.gimnasio.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "clientes")
public class Cliente {
    @Id
    private String idCliente;

    private String cedula;

    @Field("nombre_completo")
    private String nombreCompleto;

    private Integer edad;
    private String telefono;
    private String barrio = "Ternera";
    private String sede = "Cartagena - Barrio Ternera";

    private MembresiaEmb membresia = new MembresiaEmb();

    @Field("historial_pagos")
    private List<Pago> historialPagos = new ArrayList<>();

    @Field("rutina_activa")
    private Rutina rutinaActiva;

    public Cliente() {}

    public static class MembresiaEmb {
        private String tipo = "Mensual";
        @Field("valor_mensual_ref")
        private Double valorMensualRef = 75000.0;
        @Field("fecha_inicio")
        private LocalDate fechaInicio;
        @Field("fecha_vencimiento")
        private LocalDate fechaVencimiento;
        private Boolean activa = true;

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Double getValorMensualRef() { return valorMensualRef; }
        public void setValorMensualRef(Double valorMensualRef) { this.valorMensualRef = valorMensualRef; }
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        public LocalDate getFechaVencimiento() { return fechaVencimiento; }
        public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
        public Boolean getActiva() { return activa; }
        public void setActiva(Boolean activa) { this.activa = activa; }
    }

    // Métodos adaptadores compatibles con las vistas Thymeleaf y el controlador
    public String getEstadoMembresia() {
        if (getFechaVencimiento() == null) return "VENCIDA";
        return !getFechaVencimiento().isBefore(LocalDate.now()) ? "ACTIVA" : "VENCIDA";
    }

    public String getTipoMembresia() {
        return membresia != null ? membresia.getTipo() : "Mensual";
    }

    public void setTipoMembresia(String tipoMembresia) {
        if (this.membresia == null) this.membresia = new MembresiaEmb();
        this.membresia.setTipo(tipoMembresia);
    }

    public LocalDate getFechaRegistro() {
        return membresia != null ? membresia.getFechaInicio() : null;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        if (this.membresia == null) this.membresia = new MembresiaEmb();
        this.membresia.setFechaInicio(fechaRegistro);
    }

    public LocalDate getFechaVencimiento() {
        return membresia != null ? membresia.getFechaVencimiento() : null;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        if (this.membresia == null) this.membresia = new MembresiaEmb();
        this.membresia.setFechaVencimiento(fechaVencimiento);
        this.membresia.setActiva(fechaVencimiento != null && !fechaVencimiento.isBefore(LocalDate.now()));
    }

    public Boolean getActivo() {
        return membresia != null && membresia.getActiva() != null ? membresia.getActiva() : true;
    }

    public void setActivo(Boolean activo) {
        if (this.membresia == null) this.membresia = new MembresiaEmb();
        this.membresia.setActiva(activo);
    }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public MembresiaEmb getMembresia() { return membresia; }
    public void setMembresia(MembresiaEmb membresia) { this.membresia = membresia; }
    public List<Pago> getHistorialPagos() { return historialPagos; }
    public void setHistorialPagos(List<Pago> historialPagos) { this.historialPagos = historialPagos; }
    public Rutina getRutinaActiva() { return rutinaActiva; }
    public void setRutinaActiva(Rutina rutinaActiva) { this.rutinaActiva = rutinaActiva; }
}