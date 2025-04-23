package com.resumen.isst.resumenes.repository;

import java.util.*;

import org.springframework.data.repository.CrudRepository;

import com.resumen.isst.resumenes.model.*;

public interface UsuarioRepository extends CrudRepository <Usuario, Long> {
    Usuario findByUsername(String username);
    List<Usuario> findByEmail(String email);
    List<Usuario> findByRol(RolUsuario rol);    // Lista de escritores (usuarios que han publicado resúmenes)
    boolean existsByUsername(String username);
    void saveAndFlush(Usuario usuario);
}
