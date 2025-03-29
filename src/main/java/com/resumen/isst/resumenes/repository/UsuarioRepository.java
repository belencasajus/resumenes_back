package com.resumen.isst.resumenes.repository;

import java.util.*;

import org.springframework.data.repository.CrudRepository;

import com.resumen.isst.resumenes.model.*;

public interface UsuarioRepository extends CrudRepository <Usuario, String> {
    Usuario findByUsername(String username);
    List<Usuario> findByEmail(String email);
    List<Usuario> findByRol(RolUsuario rol);
    List<Usuario> findByEsEscritor(boolean esEscritor);     // Lista de escritores (usuarios que han publicado resúmenes)
    
}
