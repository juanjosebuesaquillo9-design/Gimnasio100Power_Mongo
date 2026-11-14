package com.powergym.gimnasio.model;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;

public class Pago {
    @Field("pago_id")
    private String idPago;

    @Field("fecha_pago")
    private LocalDate fechaPago;

    private Double monto;
    private String metodo;

    @Field("plan_pagado")
    private String planPagado;

    @Transient
    private Cliente cliente;

    public Pago() {}

    public String getIdPago() { return idPago; }
    public void setIdPago(String idPago) { this.idPago = idPago; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getPlanPagado() { return planPagado; }
    public void setPlanPagado(String planPagado) { this.planPagado = planPagado; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
}