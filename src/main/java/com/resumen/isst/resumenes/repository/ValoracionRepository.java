package com.resumen.isst.resumenes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.model.Valoracion; 

public interface ValoracionRepository extends CrudRepository<Valoracion, Long> {
   Optional<Valoracion> findByUsuarioAndResumen(Usuario usuario, Resumen resumen);
   List<Valoracion> findByResumen(Resumen resumen);
   
}
