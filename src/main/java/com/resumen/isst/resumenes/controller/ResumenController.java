package com.resumen.isst.resumenes.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.model.RolUsuario;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.repository.*;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;



@RestController
@CrossOrigin(origins = "http://localhost:5173") 
public class ResumenController {
    private final ResumenRepository resumenRepository;
    private final UsuarioRepository usuarioRepository;

    
    public static final Logger log = LoggerFactory.getLogger(ResumenController.class);  // Para hacer logs

    public ResumenController(ResumenRepository resumenRepository, UsuarioRepository usuarioRepository) {
        this.resumenRepository = resumenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Obtener los resumenes incluidos en el catálogo (resúmenes revisados)
    @GetMapping("/resumenes")
    List<Resumen> getResumenes() {
        //return (List<Resumen>) resumenRepository.findByRevisado(true); //cambiar a true
        return (List<Resumen>) resumenRepository.findAll();
    } 

    //Crear un resumen nuevo
    @PostMapping("/resumenes")
    ResponseEntity<?> create(@RequestBody Resumen resumen, HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username);
        
        resumen.setRevisado(false);
        resumenRepository.save(resumen);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //Obtener un resumen 
    @GetMapping("/resumenes/{id}")
    ResponseEntity<?> getResumen(@PathVariable Long id, HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado");
        }

        Usuario usuarioActual = usuarioRepository.findByUsername(username);
        if (usuarioActual == null) {
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado");
        }
        Resumen resumen = resumenRepository.findById(id).orElse(null);
        if (resumen == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(usuarioActual.getRol()== RolUsuario.VISITANTE && resumen.isPremium()) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No se puede ver resúmenes premium como visitante");
        }
        return ResponseEntity.ok(resumen);
    }

    //Eliminar un resumen (solo los administradores pueden)
    @DeleteMapping("/resumenes/{id}")
    @Transactional
    ResponseEntity<?> delete(@PathVariable Long id, HttpSession session ) {

        Optional<Resumen> resumenOpt = resumenRepository.findById(id);

        if(resumenOpt.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resumen resumen = resumenOpt.get();


        for(Usuario user: new HashSet<>(resumen.getUsuariosFavorito())) {
            user.removeFavorito(resumen);
            usuarioRepository.save(user);
        }
        for(Usuario user: new HashSet<>(resumen.getUsuariosLeido())) {
            user.removeResumenLeido(resumen);
            usuarioRepository.save(user);
        }
        resumenRepository.delete(resumen);
        return ResponseEntity.ok().body("Resumen eliminado");
    }

    //Modificar un resumen (admin o el escritor)
    @PutMapping("/resumenes/{id}")
    ResponseEntity<?> update(@RequestBody Resumen newResumen, @PathVariable Long id, HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado");
        }

        return resumenRepository.findById(id).map(resumen -> {
            Usuario usuario = usuarioRepository.findByUsername(username);

            if(usuario.getRol() != RolUsuario.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar este resumen");
            }
            resumen.setTitulo(newResumen.getTitulo());
            resumen.setAutor(newResumen.getAutor());
            resumen.setImagen(newResumen.getImagen());
            resumen.setPremium(newResumen.isPremium());
            resumen.setGenero(newResumen.getGenero());
            resumen.setTexto(newResumen.getTexto());
            resumen.setAudio(newResumen.getAudio());
            resumen.setRevisado(false);  //Se pone en false para que un admin lo revise

            resumenRepository.save(resumen);
            return ResponseEntity.ok().body(resumen);
        }).orElse(new ResponseEntity<Resumen>(HttpStatus.NOT_FOUND));
    }

    //Modificar un resumen (solo escritor o admin)
    @PatchMapping("/resumenes/{id}")
    ResponseEntity<?> partialUpdate(@RequestBody Resumen newResumen, @PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado");
        }

        return resumenRepository.findById(id).map(resumen -> {
            Usuario usuario = usuarioRepository.findByUsername(username);

            if(usuario.getRol()!= RolUsuario.ADMIN) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            if (newResumen.getTitulo() != null) {
                resumen.setTitulo(newResumen.getTitulo());
            }
            if (newResumen.getAutor() != null) {
                resumen.setAutor(newResumen.getAutor());
            }
            if (newResumen.getImagen() != null) {
                resumen.setImagen(newResumen.getImagen());
            }
            if (newResumen.getGenero() != null) {
                resumen.setGenero(newResumen.getGenero());
            }
            if (newResumen.getTexto() != null) {
                resumen.setTexto(newResumen.getTexto());
            }
            if (newResumen.getAudio() != null) {
                resumen.setAudio(newResumen.getAudio());
            }
            if (newResumen.isPremium()!= resumen.isPremium()) {
                resumen.setPremium(newResumen.isPremium());
            }
            resumen.setRevisado(false);  //Se pone en false para que un admin lo revise
            resumenRepository.save(resumen);
            return ResponseEntity.ok().body(resumen);

        }).orElse(new ResponseEntity<Resumen>(HttpStatus.NOT_FOUND)); 
    }

    // Revisar un resumen (solo admin)
    @PutMapping("/resumenes/{id}/revisar")
    ResponseEntity<?> revisarResumen(@PathVariable Long id, HttpSession session, @RequestParam boolean aprobado) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuario no autenticado");
        }

        return resumenRepository.findById(id).map(resumen -> {
            Usuario usuario = usuarioRepository.findByUsername(username);
            if(usuario.getRol()!= RolUsuario.ADMIN) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            resumen.setRevisado(aprobado);
            resumenRepository.save(resumen);
            return ResponseEntity.ok().body(resumen);
        }).orElse(new ResponseEntity<Resumen>(HttpStatus.NOT_FOUND));
    }

}
