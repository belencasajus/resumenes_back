package com.resumen.isst.resumenes;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.resumen.isst.resumenes.model.*;
import com.resumen.isst.resumenes.repository.*;

@SpringBootTest
@Transactional  // Para revertir los cambios después de cada prueba
public class UsuarioResumenIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ResumenRepository resumenRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Test
    public void testInteraccionUsuarioResumen_FavoritosYLeidos() {
        // 1. Crear datos de prueba
        String uniquePrefix = "test_" + System.currentTimeMillis();
        
        // Crear una categoría
        Categoria categoria = new Categoria(uniquePrefix + "_categoria");
        categoria = categoriaRepository.save(categoria);
        
        // Crear un usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(uniquePrefix + "_user");
        usuario.setPassword("password_encriptado");
        usuario.setEmail(uniquePrefix + "@test.com");
        usuario.setRol(RolUsuario.VISITANTE);
        usuario = usuarioRepository.save(usuario);
        
        // Crear un resumen
        Resumen resumen = new Resumen();
        resumen.setTitulo(uniquePrefix + "_resumen");
        resumen.setAutor(uniquePrefix + "_autor");
        resumen.setTexto("Contenido de prueba: " + uniquePrefix);
        resumen.setCategoria(categoria);
        resumen.setPremium(false);
        resumen.setRevisado(true);
        resumen = resumenRepository.save(resumen);
        
        // 2. Añadir el resumen a favoritos
        usuario.addFavorito(resumen);
        usuario = usuarioRepository.save(usuario);
        
        // 3. Verificar que el resumen está en favoritos
        usuario = usuarioRepository.findByUsername(usuario.getUsername());
        boolean enFavoritos = false;
        for (Resumen r : usuario.getFavoritos()) {
            if (r.getId().equals(resumen.getId())) {
                enFavoritos = true;
                break;
            }
        }
        assertTrue(enFavoritos, "El resumen debería estar en favoritos");
        
        // 4. Verificar la relación bidireccional
        resumen = resumenRepository.findById(resumen.getId()).orElse(null);
        assertNotNull(resumen);
        
        boolean usuarioEnFavoritos = false;
        for (Usuario u : resumen.getUsuariosFavorito()) {
            if (u.getUsername().equals(usuario.getUsername())) {
                usuarioEnFavoritos = true;
                break;
            }
        }
        assertTrue(usuarioEnFavoritos, "El usuario debería estar en usuariosFavorito del resumen");
        
        // 5. Marcar resumen como leído
        usuario.addResumenLeido(resumen);
        usuario = usuarioRepository.save(usuario);
        
        // 6. Verificar que el resumen está en leídos
        usuario = usuarioRepository.findByUsername(usuario.getUsername());
        boolean enLeidos = false;
        for (Resumen r : usuario.getResumenesLeidos()) {
            if (r.getId().equals(resumen.getId())) {
                enLeidos = true;
                break;
            }
        }
        assertTrue(enLeidos, "El resumen debería estar en leídos");
        
        // 7. Verificar la relación bidireccional para leídos
        resumen = resumenRepository.findById(resumen.getId()).orElse(null);
        assertNotNull(resumen);
        
        boolean usuarioEnLeidos = false;
        for (Usuario u : resumen.getUsuariosLeido()) {
            if (u.getUsername().equals(usuario.getUsername())) {
                usuarioEnLeidos = true;
                break;
            }
        }
        assertTrue(usuarioEnLeidos, "El usuario debería estar en usuariosLeido del resumen");
        
        // 8. Quitar de favoritos
        usuario.removeFavorito(resumen);
        usuario = usuarioRepository.save(usuario);
        
        // 9. Verificar que ya no está en favoritos
        usuario = usuarioRepository.findByUsername(usuario.getUsername());
        enFavoritos = false;
        for (Resumen r : usuario.getFavoritos()) {
            if (r.getId().equals(resumen.getId())) {
                enFavoritos = true;
                break;
            }
        }
        assertFalse(enFavoritos, "El resumen no debería estar en favoritos");
    }
    
    @Test
    public void testAccesoResumenPremium() {
        // 1. Crear datos de prueba
        String uniquePrefix = "test_premium_" + System.currentTimeMillis();
        
        // Crear una categoría
        Categoria categoria = new Categoria(uniquePrefix + "_categoria");
        categoria = categoriaRepository.save(categoria);
        
        // Crear un usuario visitante
        Usuario usuarioVisitante = new Usuario();
        usuarioVisitante.setUsername(uniquePrefix + "_visitante");
        usuarioVisitante.setPassword("password_encriptado");
        usuarioVisitante.setEmail(uniquePrefix + "_visitante@test.com");
        usuarioVisitante.setRol(RolUsuario.VISITANTE);
        usuarioVisitante = usuarioRepository.save(usuarioVisitante);
        
        // Crear un usuario lector con suscripción
        Usuario usuarioLector = new Usuario();
        usuarioLector.setUsername(uniquePrefix + "_lector");
        usuarioLector.setPassword("password_encriptado");
        usuarioLector.setEmail(uniquePrefix + "_lector@test.com");
        usuarioLector.setRol(RolUsuario.LECTOR);
        
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setMensualidad(5.99);
        suscripcion.setFechaInicio(java.time.LocalDate.now());
        suscripcion.setFechaFin(java.time.LocalDate.now().plusMonths(1));
        usuarioLector.setSuscripcion(suscripcion);
        
        usuarioLector = usuarioRepository.save(usuarioLector);
        
        // Crear un resumen premium
        Resumen resumenPremium = new Resumen();
        resumenPremium.setTitulo(uniquePrefix + "_resumen_premium");
        resumenPremium.setAutor(uniquePrefix + "_autor_premium");
        resumenPremium.setTexto("Contenido premium de prueba: " + uniquePrefix);
        resumenPremium.setCategoria(categoria);
        resumenPremium.setPremium(true);
        resumenPremium.setRevisado(true);
        resumenPremium = resumenRepository.save(resumenPremium);
        
        // 2. Simular la verificación que haría el controlador para un usuario visitante
        boolean permitidoParaVisitante = !resumenPremium.isPremium() || usuarioVisitante.getRol() != RolUsuario.VISITANTE;
        assertFalse(permitidoParaVisitante, "Un visitante no debería poder acceder a contenido premium");
        
        // 3. Simular la verificación para un usuario lector
        boolean permitidoParaLector = !resumenPremium.isPremium() || usuarioLector.getRol() != RolUsuario.VISITANTE;
        assertTrue(permitidoParaLector, "Un lector sí debería poder acceder a contenido premium");
    }
    
    @Test
    public void testEscritorResumen() {
        // 1. Crear datos de prueba
        String uniquePrefix = "test_escritor_" + System.currentTimeMillis();
        
        // Crear una categoría
        Categoria categoria = new Categoria(uniquePrefix + "_categoria");
        categoria = categoriaRepository.save(categoria);
        
        // Crear un usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(uniquePrefix + "_escritor");
        usuario.setPassword("password_encriptado");
        usuario.setEmail(uniquePrefix + "@test.com");
        usuario.setRol(RolUsuario.VISITANTE);
        usuario = usuarioRepository.save(usuario);
        
        // Crear un resumen con el usuario como escritor
        Resumen resumen = new Resumen();
        resumen.setTitulo(uniquePrefix + "_resumen_escrito");
        resumen.setAutor(uniquePrefix + "_autor");
        resumen.setTexto("Contenido escrito por el usuario: " + uniquePrefix);
        resumen.setCategoria(categoria);
        resumen.setPremium(false);
        resumen.setRevisado(false);
        resumen.setEscritor(usuario);
        resumen = resumenRepository.save(resumen);
        
        // 2. Verificar que el resumen tiene al usuario como escritor
        Resumen resumenGuardado = resumenRepository.findById(resumen.getId()).orElse(null);
        assertNotNull(resumenGuardado, "El resumen debería existir");
        assertNotNull(resumenGuardado.getEscritor(), "El resumen debería tener un escritor");
        assertEquals(usuario.getUsername(), resumenGuardado.getEscritor().getUsername(), 
                "El escritor del resumen debería ser el usuario de prueba");
    }
}