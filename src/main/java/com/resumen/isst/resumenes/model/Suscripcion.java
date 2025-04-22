package com.resumen.isst.resumenes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;


@Entity
public class Suscripcion {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) private Long id;
    @Positive private double mensualidad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @OneToOne
    @JoinColumn(name = "username")   
    @JsonIgnore 
    private Usuario usuario;


    public Suscripcion() {
    }

    public Suscripcion(double mensualidad, LocalDate fechaInicio, LocalDate fechaFin, Usuario usuario) {
        this.mensualidad = mensualidad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getMensualidad() {
        return mensualidad;
    }

    public void setMensualidad(double mensualidad) {
        this.mensualidad = mensualidad;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        if(fechaFin != null && fechaInicio.isAfter(fechaFin)){
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        if(fechaInicio != null && fechaFin.isBefore(fechaInicio)){
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        this.fechaFin = fechaFin;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if(usuario != null && usuario.getSuscripcion() != this){
            usuario.setSuscripcion(this);
            usuario.setRol(RolUsuario.LECTOR);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Suscripcion other = (Suscripcion) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Suscripcion [id=" + id + ", mensualidad=" + mensualidad + ", fechaInicio=" + fechaInicio + ", fechaFin="
                + fechaFin + ", usuario=" + (usuario != null ? usuario.getUsername() : "null")  + "]";
    }

    public boolean estaActiva(){
        LocalDate hoy = LocalDate.now();
        return fechaInicio != null && fechaFin != null && hoy.isAfter(fechaInicio) && hoy.isBefore(fechaFin);
    }
}
