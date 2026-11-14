package com.powergym.gimnasio.model;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Rutina {
    @Transient
    private String idRutina;

    @Transient
    private Cliente cliente;

    @Field("entrenador")
    private Map<String, String> entrenadorDoc = new HashMap<>();

    private String nivel;
    private String objetivo;
    private String ejercicios;

    @Field("fecha_asignacion")
    private LocalDate fechaCreacion;

    public Rutina() {}

    public Entrenador getEntrenador() {
        if (entrenadorDoc == null) return new Entrenador("", "Sin asignar", "General", "");
        return new Entrenador(
            entrenadorDoc.getOrDefault("id", ""),
            entrenadorDoc.getOrDefault("nombre", "Sin asignar"),
            entrenadorDoc.getOrDefault("especialidad", "General"),
            ""
        );
    }

    public void setEntrenador(Entrenador ent) {
        if (this.entrenadorDoc == null) this.entrenadorDoc = new HashMap<>();
        if (ent != null) {
            this.entrenadorDoc.put("id", ent.getIdEntrenador());
            this.entrenadorDoc.put("nombre", ent.getNombreCompleto());
            this.entrenadorDoc.put("especialidad", ent.getEspecialidad());
        }
    }

    public String getIdRutina() { return idRutina; }
    public void setIdRutina(String idRutina) { this.idRutina = idRutina; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Map<String, String> getEntrenadorDoc() { return entrenadorDoc; }
    public void setEntrenadorDoc(Map<String, String> entrenadorDoc) { this.entrenadorDoc = entrenadorDoc; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public String getEjercicios() { return ejercicios; }
    public void setEjercicios(String ejercicios) { this.ejercicios = ejercicios; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}