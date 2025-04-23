package com.resumen.isst.resumenes.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.resumen.isst.resumenes.model.Categoria;

public interface CategoriaRepository extends CrudRepository<Categoria, Long> {

    Optional<Categoria> findByNombreIgnoreCase(String trim);
}
