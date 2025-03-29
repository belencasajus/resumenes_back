package com.resumen.isst.resumenes.controller;

import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.model.RolUsuario;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.repository.UsuarioRepository;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@CrossOrigin(origins = "http://localhost:5173")

public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    //Crear un nuevo usuario (registro)
    @PostMapping("/usuarios")
    ResponseEntity<?> create(@RequestBody Usuario usuario) {
        if(usuario.getUsername() == null || usuario.getPassword() == null || usuario.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan campos obligatorios");
        }
        if(usuarioRepository.existsById(usuario.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya está en uso");
        }
        usuario.setRol(RolUsuario.VISITANTE);
        usuarioRepository.save(usuario);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    //ver perfil propio
    @GetMapping("/usuarios/me")
    ResponseEntity<Usuario> getPerfil(Principal principal) {
        Usuario usuario = usuarioRepository.findByUsername(principal.getName());
        return ResponseEntity.ok(usuario);
    }
    //ver perfil de otro usuario (admin)
    @GetMapping("/usuarios/{username}")
    ResponseEntity<?> getUsuarioPorNombre(@PathVariable String username, Principal principal) {
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());
        
        if (usuarioActual.getRol() != RolUsuario.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para ver otros perfiles");
        }
        Usuario usuario = usuarioRepository.findByUsername(username);
        return usuario != null ? ResponseEntity.ok(usuario) : ResponseEntity.notFound().build();
    }


    //modificar perfil (usuario o admin)
    @PutMapping("/usuarios/{username}")
    ResponseEntity<?> update(@PathVariable String username, @RequestBody Usuario newUsuario, Principal principal) {
        Usuario usuarioExistente = usuarioRepository.findByUsername(username);
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());
        if(usuarioExistente==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        
        if(usuarioActual.getRol() != RolUsuario.ADMIN || !usuarioActual.getUsername().equals(usuarioExistente.getUsername())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar este usuario");
        }
        usuarioExistente.setEmail(newUsuario.getEmail());
        usuarioExistente.setImagen(newUsuario.getImagen());
        usuarioRepository.save(usuarioExistente);
        return ResponseEntity.ok(usuarioExistente);
    }

    //Eliminar un usuario (propio o como admin)
    @DeleteMapping("usuarios/{username}")
    ResponseEntity<?> delete(@PathVariable String username, Principal principal) {
        Usuario usuarioAEliminar = usuarioRepository.findByUsername(username);
        Usuario usuarioActual = usuarioRepository.findByUsername(principal.getName());

        if(usuarioAEliminar==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        if(usuarioActual.getRol() != RolUsuario.ADMIN || !usuarioActual.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } 

        for(Resumen resumen: usuarioAEliminar.getResumenesEscritos()){
            resumen.setEscritor(null);
        }
        for(Resumen resumen: usuarioAEliminar.getFavoritos()){
            resumen.getUsuariosFavorito().remove(usuarioAEliminar);
        }
        for(Resumen resumen: usuarioAEliminar.getResumenesLeidos()){
            resumen.getUsuariosLeido().remove(usuarioAEliminar);
        }
        usuarioRepository.delete(usuarioAEliminar);
        return ResponseEntity.ok("Usuario eliminado");
    }
}
