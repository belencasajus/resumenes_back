package com.resumen.isst.resumenes.repository;

import com.resumen.isst.resumenes.model.Suscripcion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SuscripcionRepository extends CrudRepository<Suscripcion, Long>{

}
