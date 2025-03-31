package com.resumen.isst.resumenes.model;

import java.util.HashSet;
import java.util.Set;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

@Entity
public class Usuario {

    @Id private String username;

    String password;

    @Email private String email;

    private String imagen;       //Guardamos la imagen como una URL
    
    @ManyToMany @JoinTable(
        name = "favoritos",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "resumen_id")
    ) private Set<Resumen> favoritos = new HashSet<>();

    
    @ManyToMany @JoinTable(
        name = "resumenes_leidos",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "resumen_id")
        ) private Set<Resumen> resumenesLeidos = new HashSet<>();
        
    private RolUsuario rol = RolUsuario.VISITANTE;

    @OneToOne(mappedBy = "usuario", cascade= CascadeType.ALL, orphanRemoval=true) private Suscripcion suscripcion;


    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true) private Set<Valoracion> valoraciones = new HashSet<>();

    public Usuario() {
    }

    public Usuario(String username, String password, @Email String email, String imagen, Set<Resumen> favoritos,
            Set<Resumen> resumenesLeidos, RolUsuario rol, Suscripcion suscripcion,
            Set<Valoracion> valoraciones) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.imagen = imagen;
        this.favoritos = favoritos;
        this.resumenesLeidos = resumenesLeidos;
        this.rol = rol;
        this.suscripcion = suscripcion;
        this.valoraciones = valoraciones;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Set<Resumen> getFavoritos() {
        return favoritos;
    }

    public void setFavoritos(Set<Resumen> favoritos) {
        this.favoritos = favoritos;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        if (rol.equals(RolUsuario.LECTOR)) {                            //Dice el chat que esta parte va en el servicio
            if(this.suscripcion == null){
                this.suscripcion = new Suscripcion();
                this.suscripcion.setUsuario(this);
            }
        }

        this.rol = rol;
    }

 
    public Set<Resumen> getResumenesLeidos() {
        return resumenesLeidos;
    }

    public void setResumenesLeidos(Set<Resumen> resumenesLeidos) {
        this.resumenesLeidos = resumenesLeidos;
    }

    public Suscripcion getSuscripcion() {
        return suscripcion;
    }

    public void setSuscripcion(Suscripcion suscripcion) {
        this.suscripcion = suscripcion;
        this.setRol(RolUsuario.LECTOR);
        if(suscripcion != null) {              
            suscripcion.setUsuario(this);
        }
    }



    public Set<Valoracion> getValoraciones() {
        return valoraciones;
    }

    public void setValoraciones(Set<Valoracion> valoraciones) {
        this.valoraciones = valoraciones;
    }

    // Métodos auxiliares
    public void addFavorito(Resumen resumen) {
        this.favoritos.add(resumen);
        resumen.getUsuariosFavorito().add(this);
    }

    public void removeFavorito(Resumen resumen) {
        this.favoritos.remove(resumen);
        resumen.getUsuariosFavorito().remove(this);
    }

    public void addResumenLeido(Resumen resumen) {
        this.resumenesLeidos.add(resumen);
        resumen.getUsuariosLeido().add(this);
        
    }
    public void removeResumenLeido(Resumen resumen) {
        this.resumenesLeidos.remove(resumen);
        resumen.getUsuariosLeido().remove(this);
    }


    public void addValoracion(Valoracion valoracion) {
        this.valoraciones.add(valoracion);
        if(valoracion.getUsuario() != this) {
            valoracion.setUsuario(this);
        }
    }

    public void removeValoracion(Valoracion valoracion) {
        this.valoraciones.remove(valoracion);
        if(valoracion.getUsuario() == this) {
            valoracion.setUsuario(null);
        }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
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
        Usuario other = (Usuario) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Usuario [username=" + username + ", email=" + email +  ", rol=" + rol
                + "]";
    }

}
