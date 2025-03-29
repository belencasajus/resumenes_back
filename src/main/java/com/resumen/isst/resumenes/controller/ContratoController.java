package com.resumen.isst.resumenes.controller;

import com.resumen.isst.resumenes.model.Contrato;
import com.resumen.isst.resumenes.model.RolUsuario;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.repository.ContratoRepository;
import com.resumen.isst.resumenes.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
public class ContratoController {

    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;
    public static final Logger log = LoggerFactory.getLogger(ContratoController.class);

    public ContratoController(ContratoRepository contratoRepository, UsuarioRepository usuarioRepository) {
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Obtener contrato (admin o dueño)
    @GetMapping("/contratos/{id}")
    ResponseEntity<?> getContrato(@PathVariable Long id, Principal principal) {
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());
        return contratoRepository.findById(id).map(contrato -> {
            if(!contrato.getUsuario().getUsername().equals(usuarioActual.getUsername()) || usuarioActual.getRol()!=RolUsuario.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tiene permisos para ver este contrato");
            }
            return ResponseEntity.ok().body(contrato);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contrato no encontrado"));
    }

    //Crea contrato (usuario actual se convierte en escritor)
    @PostMapping("/contratos")
    ResponseEntity<?> create(@RequestBody Contrato nuevoContrato, @RequestParam String username, Principal principal) {
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());
        Usuario usuario = usuarioRepository.findByUsername(username);
        if(usuario==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        if (usuario.getContrato() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya tiene un contrato activo");
        }
        if(!usuarioActual.getUsername().equals(usuario.getUsername())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No puede crear un contrato para este usuario");
        }

        nuevoContrato.setUsuario(usuario);
        usuario.setContrato(nuevoContrato);
        usuario.setEsEscritor(true);

        usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoRepository.save(nuevoContrato));
    }

    //Actualiza contrato (solo admin o dueño)
    @PutMapping("/contratos/{id}")
    ResponseEntity<?> update(@RequestBody Contrato nuevoContrato, @PathVariable Long id, Principal principal) {
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());
        return contratoRepository.findById(id).map(contrato -> {
            if(!contrato.getUsuario().getUsername().equals(usuarioActual.getUsername()) || usuarioActual.getRol()!=RolUsuario.ADMIN) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            contrato.setFechaInicio(nuevoContrato.getFechaInicio());
            contrato.setFechaFin(nuevoContrato.getFechaFin());
            contrato.setComision(nuevoContrato.getComision());

            contratoRepository.save(contrato);
            return ResponseEntity.ok().body(contrato);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //Elimina el contrato (solo admin o dueño)
    @DeleteMapping("/contratos/{id}")
    ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());
        return contratoRepository.findById(id).map(contrato -> {
            if(!contrato.getUsuario().getUsername().equals(usuarioActual.getUsername()) || usuarioActual.getRol()!=RolUsuario.ADMIN) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            Usuario usuario = contrato.getUsuario();
            usuario.setContrato(null);
            usuario.setEsEscritor(false);
            usuarioRepository.save(usuario);
            contratoRepository.delete(contrato);
            return ResponseEntity.ok().build();
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
