package com.resumen.isst.resumenes.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.resumen.isst.resumenes.model.Categoria;
import com.resumen.isst.resumenes.repository.CategoriaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaController {
    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/categorias")
    public List<Categoria> getCategorias() {
        return (List<Categoria>) categoriaRepository.findAll();
    }

    @PostMapping("/categorias")
    public ResponseEntity<?> createCategoria(@RequestBody Categoria categoria) {
       if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de la categoría no puede estar vacío.");
        }

        Optional<Categoria> categoriaExistente = categoriaRepository.findByNombreIgnoreCase(categoria.getNombre().trim());

        if (categoriaExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe una categoría con ese nombre.");
        }
        categoria.setNombre(categoria.getNombre().trim());
        Categoria nuevaCategoria = categoriaRepository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCategoria); 
    }
    
    
}
