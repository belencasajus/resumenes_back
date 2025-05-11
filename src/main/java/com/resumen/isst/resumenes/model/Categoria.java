package com.resumen.isst.resumenes.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.AUTO) private Long id;

    @Column(nullable = false , unique = true)
    private String nombre;

    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    private Set<Resumen> resumenes = new HashSet<>();

    public Categoria() {
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<Resumen> getResumenes() {
        return resumenes;
    }

    public void setResumenes(Set<Resumen> resumenes) {
        this.resumenes = resumenes;
    }

}
