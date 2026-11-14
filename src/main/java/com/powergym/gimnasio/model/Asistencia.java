package com.powergym.gimnasio.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "asistencias")
public class Asistencia {
    @Id
    private String idAsistencia;

    @Field("cliente_id")
    private ObjectId clienteId;

    private String cedula;

    @Field("nombre_cliente")
    private String nombreCliente;

    @Field("fecha_hora")
    private LocalDateTime fechaHora;

    private String torniquete = "Torniquete Principal #1";

    @Field("estado_acceso")
    private String estadoAcceso;

    @Transient
    private Cliente cliente;

    public Asistencia() {}

    public Cliente getCliente() {
        if (cliente != null) return cliente;
        Cliente c = new Cliente();
        c.setIdCliente(clienteId != null ? clienteId.toHexString() : "");
        c.setCedula(cedula);
        c.setNombreCompleto(nombreCliente != null ? nombreCliente : "Afiliado");
        return c;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            if (cliente.getIdCliente() != null && ObjectId.isValid(cliente.getIdCliente())) {
                this.clienteId = new ObjectId(cliente.getIdCliente());
            }
            this.cedula = cliente.getCedula();
            this.nombreCliente = cliente.getNombreCompleto();
        }
    }

    public String getIdAsistencia() { return idAsistencia; }
    public void setIdAsistencia(String idAsistencia) { this.idAsistencia = idAsistencia; }
    public ObjectId getClienteId() { return clienteId; }
    public void setClienteId(ObjectId clienteId) { this.clienteId = clienteId; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getTorniquete() { return torniquete; }
    public void setTorniquete(String torniquete) { this.torniquete = torniquete; }
    public String getEstadoAcceso() { return estadoAcceso; }
    public void setEstadoAcceso(String estadoAcceso) { this.estadoAcceso = estadoAcceso; }
}