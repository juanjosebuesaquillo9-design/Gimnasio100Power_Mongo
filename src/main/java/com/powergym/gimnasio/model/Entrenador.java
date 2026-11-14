package com.powergym.gimnasio.model;

public class Entrenador {
    private String idEntrenador;
    private String nombreCompleto;
    private String especialidad;
    private String telefono;

    public Entrenador() {}

    public Entrenador(String idEntrenador, String nombreCompleto, String especialidad, String telefono) {
        this.idEntrenador = idEntrenador;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.telefono = telefono;
    }

    public String getNombre() { return nombreCompleto; }
    public String getIdEntrenador() { return idEntrenador; }
    public void setIdEntrenador(String idEntrenador) { this.idEntrenador = idEntrenador; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}