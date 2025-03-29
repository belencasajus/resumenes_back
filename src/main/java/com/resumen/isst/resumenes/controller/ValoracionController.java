package com.resumen.isst.resumenes.controller;

import com.resumen.isst.resumenes.model.Valoracion;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.repository.ValoracionRepository;
import com.resumen.isst.resumenes.repository.UsuarioRepository;
import com.resumen.isst.resumenes.repository.ResumenRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
public class ValoracionController {

    private final ValoracionRepository valoracionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ResumenRepository resumenRepository;

    public static final Logger log = LoggerFactory.getLogger(ValoracionController.class);

    public ValoracionController(ValoracionRepository valoracionRepository, UsuarioRepository usuarioRepository, ResumenRepository resumenRepository) {
        this.valoracionRepository = valoracionRepository;
        this.usuarioRepository = usuarioRepository;
        this.resumenRepository = resumenRepository;
    }

    //Obtener todas las valoraciones 
    @GetMapping("/valoraciones")
    public List<Valoracion> getValoraciones() {
        return (List<Valoracion>) valoracionRepository.findAll();
    }

    // Obtener una valoración 
    @GetMapping("/valoraciones/{id}")
    public ResponseEntity<Valoracion> getValoracion(@PathVariable Long id) {
        return valoracionRepository.findById(id)
                .map(valoracion -> ResponseEntity.ok().body(valoracion))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //Crea una nueva valoración sobre un resumen concreto
    /*desde el frontend se hace un POST a /resumenes/{resumenId}/valoraciones, con un body como:
     * {
     * "rating": 5,
     * "comentario": "Esto es un resumen muy interesante"
     * }
     */
    @PostMapping("/resumenes/{resumenId}/valoraciones")
    public ResponseEntity<?> create(@PathVariable Long resumenId, @RequestBody Valoracion nuevaValoracion, Principal principal) {
        Usuario usuario = usuarioRepository.findByUsername(principal.getName());

        return resumenRepository.findById(resumenId).map(resumen -> {
            //Verificar si ya existe una valoracion del usuario
            Optional<Valoracion> existente = valoracionRepository.findByUsuarioAndResumen(usuario, resumen);
            if (existente.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya has valorado este resumen");
            }
            Valoracion valoracion = new Valoracion();
            valoracion.setRating(nuevaValoracion.getRating());
            valoracion.setComentario(nuevaValoracion.getComentario());
            valoracion.setFecha(LocalDateTime.now());
            valoracion.setUsuario(usuario);
            valoracion.setResumen(resumen);

            usuario.addValoracion(valoracion);
            resumen.addValoracion(valoracion);
            valoracionRepository.save(valoracion);
            recalcularValoracionMedia(resumen);

            return ResponseEntity.ok(valoracion);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resumen no encontrado"));
    }

    //Elimina una valoracion (solo el propio usuario puede)
    @DeleteMapping("/valoraciones/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        return valoracionRepository.findById(id).map(valoracion -> {
            Usuario usuario = usuarioRepository.findByUsername(principal.getName());
            if (!valoracion.getUsuario().equals(usuario)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para borrar esta valoración");
            }

            Resumen resumen = valoracion.getResumen();

            resumen.removeValoracion(valoracion);
            usuario.removeValoracion(valoracion);
            valoracionRepository.delete(valoracion);
            recalcularValoracionMedia(resumen);
            return ResponseEntity.ok().build();
            
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private void recalcularValoracionMedia(Resumen resumen) {
        List<Valoracion> valoraciones = valoracionRepository.findByResumen(resumen);
        double media = valoraciones.stream()
           .mapToDouble(Valoracion::getRating)
           .average().orElse(0.0);
        resumen.setValoracionMedia(media);
        resumenRepository.save(resumen);
    }
}

