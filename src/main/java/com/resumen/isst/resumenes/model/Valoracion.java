package com.resumen.isst.resumenes.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
public class Valoracion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @PositiveOrZero @DecimalMax("5.0") private Double rating;

    @Size(max= 500) private String comentario;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "username")
    @JsonBackReference("usuario-valoraciones")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "resumen_id")
    @JsonBackReference("resumen-valoraciones")
    private Resumen resumen;

    public Valoracion() {
    }

    public Valoracion(Double rating, String comentario, LocalDateTime fecha, Usuario usuario, Resumen resumen) {
        this.rating = rating;
        this.comentario = comentario;
        this.fecha = fecha;
        this.usuario = usuario;
        this.resumen = resumen;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario (Usuario usuario) {
        if(usuario!= null) {
            this.usuario = usuario;
        }
    }

    public Resumen getResumen() {
        return resumen;
    }

    public void setResumen(Resumen resumen) {
        if(resumen != null) {
            this.resumen = resumen;
            resumen.getValoraciones().add(this);
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
        Valoracion other = (Valoracion) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Valoracion [id=" + id + ", rating=" + rating + ", comentario=" + comentario + ", fecha=" + fecha
                + ", usuario=" + (usuario != null ? usuario.getUsername() : "null") + ", resumen=" + (resumen != null? resumen.getTitulo() : "null") + "]";
    }

    @PrePersist
    public void prePersist() {
        if(fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

}
