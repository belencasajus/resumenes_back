package com.resumen.isst.resumenes.controller;

import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.model.RolUsuario;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;



@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Usuario usuario = usuarioRepository.findByUsername(username);

        if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrecta");
        }

        session.setAttribute("username", usuario.getUsername());
        session.setAttribute("rol", usuario.getRol());
        session.setAttribute("esEscritor", usuario.getEsEscritor());

        Map<String, Object> response = new HashMap<>();
        response.put("username", usuario.getUsername());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol());
        response.put("esEscritor", usuario.getEsEscritor());

        return ResponseEntity.ok(response);
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
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol(RolUsuario.VISITANTE);
        usuarioRepository.save(usuario);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }


    //ver perfil propio

    @GetMapping("/usuarios/me")
    ResponseEntity<?> getPerfil(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        Usuario usuario = usuarioRepository.findByUsername(username);
        return ResponseEntity.ok(usuario);
    }

    //ver perfil de otro usuario (admin)
    @GetMapping("/usuarios/{username}")
    ResponseEntity<?> getUsuarioPorNombre(@PathVariable String username, HttpSession session) {
        String loggedUsername = (String) session.getAttribute("username");
        if (loggedUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás autenticado");
        }
        Usuario usuarioActual = usuarioRepository.findByUsername(loggedUsername);
        if (usuarioActual.getRol() != RolUsuario.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para ver otros perfiles");
        }
        Usuario usuario = usuarioRepository.findByUsername(username);
        return usuario != null ? ResponseEntity.ok(usuario) : ResponseEntity.notFound().build();
    }
        

    //modificar perfil (usuario o admin)
    @PutMapping("/usuarios/{username}")
    ResponseEntity<?> update(@PathVariable String username, @RequestBody Usuario newUsuario, HttpSession session) {
        String loggedUsername = (String) session.getAttribute("username");
        if (loggedUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás autenticado");
        }

        Usuario usuarioExistente = usuarioRepository.findByUsername(username);
        Usuario usuarioActual = usuarioRepository.findByUsername(loggedUsername);
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
    ResponseEntity<?> delete(@PathVariable String username, HttpSession session) {
        String loggedUsername = (String) session.getAttribute("username");
        if (loggedUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás autenticado");
        }

        Usuario usuarioAEliminar = usuarioRepository.findByUsername(username);
        Usuario usuarioActual = usuarioRepository.findByUsername(loggedUsername);

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
