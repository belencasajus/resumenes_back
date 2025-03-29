package com.resumen.isst.resumenes.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class Contrato {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) private Long id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    @PositiveOrZero private Double comision;

    @OneToOne
    @JoinColumn(name = "username")  
    private Usuario usuario;

    public Contrato() {
    }

    public Contrato(LocalDate fechaInicio, LocalDate fechaFin, Double comision, Usuario usuario) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.comision = comision;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getComision() {
        return comision;
    }

    public void setComision(Double comision) {
        this.comision = comision;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if(usuario!= null && usuario.getContrato() != this) {
            usuario.setContrato(this);
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
        Contrato other = (Contrato) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Contrato [id=" + id + ", fechaInicio=" + fechaInicio + ", fechaFin=" + fechaFin + ", comision="
                + comision + ", usuario=" + (usuario != null ? usuario.getUsername() : "null") + "]";
    }

    public boolean estaActivo() {
        LocalDate hoy = LocalDate.now();
        return fechaInicio != null && fechaFin != null && hoy.isAfter(fechaInicio) && hoy.isBefore(fechaFin);
    }
}
