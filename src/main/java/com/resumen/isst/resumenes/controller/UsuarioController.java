package com.resumen.isst.resumenes.controller;

import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.model.RolUsuario;
import com.resumen.isst.resumenes.model.Suscripcion;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.repository.ResumenRepository;
import com.resumen.isst.resumenes.repository.SuscripcionRepository;
import com.resumen.isst.resumenes.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;



@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final ResumenRepository resumenRepository;
    private final SuscripcionRepository suscripcionRepository;
    private PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, ResumenRepository resumenRepository, SuscripcionRepository suscripcionRepository) {
        this.suscripcionRepository = suscripcionRepository;
        this.resumenRepository = resumenRepository;
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
        
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authToken);

        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Map<String, Object> response = new HashMap<>();
        response.put("username", usuario.getUsername());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol());
        

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Sesión cerrada");
    }

    //Crear un nuevo usuario (registro)
    @PostMapping("/usuarios")
    ResponseEntity<?> create(@RequestBody Usuario usuario, HttpSession session) {
        if(usuario.getUsername() == null || usuario.getPassword() == null || usuario.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan campos obligatorios");
        }
        if(usuarioRepository.existsById(usuario.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya está en uso");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol(RolUsuario.VISITANTE);
        usuarioRepository.save(usuario);

        session.setAttribute("username", usuario.getUsername());
        session.setAttribute("rol", usuario.getRol());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(usuario.getUsername(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Map<String, Object> response = new HashMap<>();
        response.put("username", usuario.getUsername());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol());
        
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }


    //ver perfil propio
    @Transactional
    @GetMapping("/usuarios/me")
    ResponseEntity<?> getPerfil(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        Usuario usuario = usuarioRepository.findByUsername(username);
        usuario.getValoraciones().size();
        return ResponseEntity.ok(usuario);
    }
        

    //modificar perfil (usuario)
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
        
        if(!usuarioActual.getUsername().equals(usuarioExistente.getUsername())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para modificar este usuario");
        }
        usuarioExistente.setEmail(newUsuario.getEmail());
        usuarioExistente.setImagen(newUsuario.getImagen());
        usuarioRepository.save(usuarioExistente);
        return ResponseEntity.ok(usuarioExistente);
    }

    //Eliminar un usuario (propio)
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
        if(!usuarioActual.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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

    @PostMapping("/usuarios/leidos/{resumenId}")
    public ResponseEntity<?> marcarResumenComoLeido(@PathVariable Long resumenId, HttpSession session) {
    String username = (String) session.getAttribute("username");
    if (username == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
    }

    Usuario usuario = usuarioRepository.findByUsername(username);
    Optional<Resumen> resumenOpt = resumenRepository.findById(resumenId);

    if (usuario == null || resumenOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario o resumen no encontrado");
    }

    Resumen resumen = resumenOpt.get();
    if (!usuario.getResumenesLeidos().contains(resumen)) {
       usuario.getResumenesLeidos().add(resumen);
        resumen.getUsuariosLeido().add(usuario);
        usuarioRepository.save(usuario);
    }
    return ResponseEntity.ok("Resumen marcado como leído");

    }
    @PostMapping("/usuarios/favoritos/{resumenId}")
    public ResponseEntity<?> addFavorito(@PathVariable Long resumenId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username);
        Optional<Resumen> resumenOpt = resumenRepository.findById(resumenId);

        if (usuario == null || resumenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario o resumen no encontrado");
        }

        Resumen resumen = resumenOpt.get();

        if (!usuario.getFavoritos().contains(resumen)) {
            usuario.addFavorito(resumen);
            usuarioRepository.save(usuario);
        }

        return ResponseEntity.ok("Resumen añadido a favoritos");
    }
    @DeleteMapping("/usuarios/favoritos/{resumenId}")
    public ResponseEntity<?> removeFavorito(@PathVariable Long resumenId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username);
        Optional<Resumen> resumenOpt = resumenRepository.findById(resumenId);

        if (usuario == null || resumenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario o resumen no encontrado");
        }

        Resumen resumen = resumenOpt.get();

        if (usuario.getFavoritos().contains(resumen)) {
            usuario.removeFavorito(resumen);
            usuarioRepository.save(usuario);
        }

        return ResponseEntity.ok("Resumen eliminado de favoritos");
    }

    @GetMapping("/usuarios/favoritos")
    public ResponseEntity<?> getFavoritos(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Set <Resumen> favoritos = usuario.getFavoritos();
        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/usuarios/leidos")
    public ResponseEntity<?> getResumenesLeidos(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Set <Resumen> leidos = usuario.getResumenesLeidos();
        return ResponseEntity.ok(leidos);
    }

    @PutMapping("/usuarios/imagen")
    public ResponseEntity<?> actualizarImagen(@RequestBody Map<String, String> body, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username);
        String nuevaImagen = body.get("imagen");

        usuario.setImagen(nuevaImagen);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Imagen actualizada");
    }

    @DeleteMapping("/usuarios/suscripcion")
    @Transactional
    public ResponseEntity<?> deleteSuscripcion(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás autenticado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username);
        if(usuario==null || usuario.getSuscripcion() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No tienes una suscripción activa");
        }
        Suscripcion suscripcion = usuario.getSuscripcion();

        usuario.setSuscripcion(null);
        usuario.setRol(RolUsuario.VISITANTE);

        suscripcionRepository.delete(suscripcion); 
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Suscripción cancelada correctamente");
    }

    @PutMapping(value = "/usuarios/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> actualizarPerfil(
        @RequestPart("username") String nuevoUsername,
        @RequestPart("email") String nuevoEmail,
        @RequestPart(value = "imagen", required = false) MultipartFile nuevaImagen,
        HttpSession session) throws IOException {

            String actualUsername = (String) session.getAttribute("username");
            if (actualUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
            }
        
            Usuario usuario = usuarioRepository.findByUsername(actualUsername);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
            }
        
            // Actualizar datos
            usuario.setUsername(nuevoUsername);
            usuario.setEmail(nuevoEmail);
        
            // Guardar imagen si existe
            if (nuevaImagen != null && !nuevaImagen.isEmpty()) {
                Path root = Paths.get("").toAbsolutePath().getParent().resolve("public/user_imgs");
                Files.createDirectories(root);
        
                String nombreArchivo = UUID.randomUUID() + "-" + nuevaImagen.getOriginalFilename();
                Files.copy(nuevaImagen.getInputStream(), root.resolve(nombreArchivo), StandardCopyOption.REPLACE_EXISTING);
        
                usuario.setImagen("/user_imgs/" + nombreArchivo);
            }
        
            usuarioRepository.save(usuario);
        
            // Actualizar sesión
            session.setAttribute("username", nuevoUsername);
        
            return ResponseEntity.ok(usuario);
}

}
