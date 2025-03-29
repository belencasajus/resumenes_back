package com.resumen.isst.resumenes.repository;

import java.util.*;

import org.springframework.data.repository.CrudRepository;


import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.model.Usuario;

public interface ResumenRepository extends CrudRepository <Resumen, Long> {
    List<Resumen> findByTitulo(String titulo);
    List<Resumen> findByAutor(String autor);
    List<Resumen> findByGenero(String genero);
    List<Resumen> findByPremium(boolean premium);
    List<Resumen> findByRevisado(boolean revisado);
    List<Resumen> findByEscritor(Usuario escritor);
    List<Resumen> findByTituloContaining(String palabraClave);
}
