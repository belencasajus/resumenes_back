package com.resumen.isst.resumenes.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class Resumen {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) private Long id;

    private String titulo;

    private String autor;

    private String imagen;    //imagen tiene que ser una url

    private boolean premium;

    private String genero;

    @PositiveOrZero @DecimalMax("5.0") private double valoracionMedia;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private String audio;

    private boolean revisado=false;

    @JsonIgnore
    @ManyToMany(mappedBy = "favoritos") private Set<Usuario> 
    usuariosFavorito = new HashSet<>();

    @ManyToMany(mappedBy = "resumenesLeidos") 
    @JsonIgnore
    private Set<Usuario> usuariosLeido = new HashSet<>();

    @OneToMany(mappedBy = "resumen", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("resumen-valoraciones")
    private Set<Valoracion> valoraciones= new HashSet<>();

    public Resumen() {
    }

    public Resumen(Long id, String titulo, String autor, String imagen, boolean premium, String genero,
            @PositiveOrZero @DecimalMax("5.0") double valoracionMedia, String texto, String audio, boolean revisado,
            Set<Usuario> usuariosFavorito, Set<Usuario> usuariosLeido,
            Set<Valoracion> valoraciones) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.imagen = imagen;
        this.premium = premium;
        this.genero = genero;
        this.valoracionMedia = valoracionMedia;
        this.texto = texto;
        this.audio = audio;
        this.revisado = revisado;
        this.usuariosFavorito = usuariosFavorito;
        this.usuariosLeido = usuariosLeido;
        this.valoraciones = valoraciones;
    }

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getTitulo() {
        return titulo;
    }


    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }


    public String getAutor() {
        return autor;
    }


    public void setAutor(String autor) {
        this.autor = autor;
    }


    public String getImagen() {
        return imagen;
    }


    public void setImagen(String imagen) {
        this.imagen = imagen;
    }


    public boolean isPremium() {
        return premium;
    }


    public void setPremium(boolean premium) {
        this.premium = premium;
    }


    public String getGenero() {
        return genero;
    }


    public void setGenero(String genero) {
        this.genero = genero;
    }


    public double getValoracionMedia() {
        return valoracionMedia;
    }


    public void setValoracionMedia(double valoracionMedia) {
        this.valoracionMedia = valoracionMedia;
    }


    public String getTexto() {
        return texto;
    }


    public void setTexto(String texto) {
        this.texto = texto;
    }


    public String getAudio() {
        return audio;
    }


    public void setAudio(String audio) {
        this.audio = audio;
    }


    public boolean isRevisado() {
        return revisado;
    }


    public void setRevisado(boolean revisado) {
        this.revisado = revisado;
    }

    public Set<Usuario> getUsuariosFavorito() {
        return usuariosFavorito;
    }


    public void setUsuariosFavorito(Set<Usuario> usuariosFavorito) {
        this.usuariosFavorito = usuariosFavorito;
    }


    public Set<Usuario> getUsuariosLeido() {
        return usuariosLeido;
    }


    public void setUsuariosLeido(Set<Usuario> usuariosLeido) {
        this.usuariosLeido = usuariosLeido;
    }


    public Set<Valoracion> getValoraciones() {
        return valoraciones;
    }


    public void setValoraciones(Set<Valoracion> valoraciones) {
        this.valoraciones = valoraciones;
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
        Resumen other = (Resumen) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Resumen [id=" + id + ", titulo=" + titulo + ", autor=" + autor  + ", premium="
                + premium + ", genero=" + genero + ", valoracionMedia=" + valoracionMedia 
                + ", revisado=" + revisado + "]";
    }


    public void addValoracion(Valoracion valoracion) {
        valoraciones.add(valoracion);
        if(valoracion.getResumen() != this) {
            valoracion.setResumen(this);
        }
    }

    public void removeValoracion(Valoracion valoracion) {
        valoraciones.remove(valoracion);
        if(valoracion.getResumen() == this) {
            valoracion.setResumen(null);
        }
    }

    public void addUsuarioFavorito(Usuario usuario) {
        usuariosFavorito.add(usuario);
        usuario.getFavoritos().add(this);
    }

    public void removeUsuarioFavorito(Usuario usuario) {
        usuariosFavorito.remove(usuario);
        usuario.getFavoritos().remove(this);
    }

    public void addUsuarioLeido(Usuario usuario) {
        usuariosLeido.add(usuario);
        usuario.getResumenesLeidos().add(this);
    }

    public void removeUsuarioLeido(Usuario usuario) {
        usuariosLeido.remove(usuario);
        usuario.getResumenesLeidos().remove(this);
    }


}
