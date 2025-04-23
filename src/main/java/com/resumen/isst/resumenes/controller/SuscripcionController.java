package com.resumen.isst.resumenes.controller;

import com.resumen.isst.resumenes.model.RolUsuario;
import com.resumen.isst.resumenes.model.Suscripcion;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.repository.SuscripcionRepository;
import com.resumen.isst.resumenes.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class SuscripcionController {
    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;

    public SuscripcionController(SuscripcionRepository suscripcionRepository, UsuarioRepository usuarioRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    //Obtener suscripcion (solo si es propia)
    @GetMapping("/suscripciones/{id}")
    ResponseEntity<?> getSuscripcion(@PathVariable Long id, Principal principal) {
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());
        return suscripcionRepository.findById(id).map(suscripcion -> {
            if(!suscripcion.getUsuario().getUsername().equals(usuarioActual.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes acceso a esta suscripcion.");
            }
            return ResponseEntity.ok(suscripcion);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Suscripcion no encontrada."));
    }

    //Crear una nueva suscripcion
    @PostMapping("/suscripciones")
    ResponseEntity<?> create(@RequestBody Suscripcion suscripcion, HttpSession session) {
        String loggedUsername = (String) session.getAttribute("username");
        if (loggedUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás autenticado");
        }
        Usuario usuario = usuarioRepository.findByUsername(loggedUsername);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        if(usuario.getSuscripcion() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya tiene una suscripcion activa. Actualice su suscripcion si lo desea");
        }
        suscripcion.setUsuario(usuario);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setFechaFin(suscripcion.getFechaInicio().plusMonths(1));
        suscripcion.setMensualidad(5.99); 

        usuario.setSuscripcion(suscripcion); 
        usuario.setRol(RolUsuario.LECTOR); 
        
        usuarioRepository.save(usuario);    // No hace falta poner "suscripcionRepository.save(suscripcion)" ya que al poner cascade = CascadeType.ALL en el usuario, ya que guarda usuario y tambien suscripcion
        return ResponseEntity.status(HttpStatus.CREATED).body(suscripcionRepository.save(suscripcion));
    }

    //Actualizar suscripcion (solo si es propia)
    @PatchMapping("/suscripciones/{id}")
    ResponseEntity<?> update(@PathVariable Long id, @RequestBody Suscripcion update, Principal principal) {
        return suscripcionRepository.findById(id).map(suscripcion -> {
            if(!suscripcion.getUsuario().getUsername().equals(principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if(update.getMensualidad()>0){
                suscripcion.setMensualidad(update.getMensualidad());
            }
            if(update.getFechaInicio()!=null){
                suscripcion.setFechaInicio(update.getFechaInicio());
            }
            if(update.getFechaFin()!=null){
                suscripcion.setFechaFin(update.getFechaFin());
            }
            suscripcionRepository.save(suscripcion);

            return ResponseEntity.ok(suscripcionRepository.save(suscripcion));
        }).orElse(ResponseEntity.notFound().build());
    }

    //Eliminar suscripcion (solo si es propia)
    @DeleteMapping("/suscripciones/{id}")
    @Transactional
    public ResponseEntity<?> deleteSuscripcion(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás autenticado.");
        }
    
        Usuario usuario = usuarioRepository.findByUsername(username);
    
        if (usuario == null || usuario.getSuscripcion() == null || !usuario.getSuscripcion().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para eliminar esta suscripción.");
        }
    
        // ✅ Eliminamos primero la suscripción, antes de modificar el usuario
        Suscripcion suscripcion = usuario.getSuscripcion();
        usuario.setSuscripcion(null); // desvincular del usuario
        usuario.setRol(RolUsuario.VISITANTE);
        suscripcionRepository.delete(suscripcion); // eliminar primero
        usuarioRepository.save(usuario);
    
        return ResponseEntity.ok("Suscripción eliminada correctamente.");
    }
   
}
