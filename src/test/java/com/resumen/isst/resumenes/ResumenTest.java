package com.resumen.isst.resumenes;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.resumen.isst.resumenes.model.Categoria;
import com.resumen.isst.resumenes.model.Resumen;
import com.resumen.isst.resumenes.model.Usuario;
import com.resumen.isst.resumenes.model.Valoracion;

public class ResumenTest {
    //Declaración de objetos con los que interactúa la clase Resumen
    private Resumen resumen;
    private Usuario usuario;
    private Categoria categoria;
    private Valoracion valoracion;
    
    //Este método se ejecuta antes de cada prueba (gracias a la anotación @BeforeEach). Inicializa los objetos y les asigna valores básicos para usarlos en las pruebas.
    @BeforeEach
    void setUp() {
        // Inicializar objetos para las pruebas
        resumen = new Resumen();
        usuario = new Usuario();
        categoria = new Categoria();
        valoracion = new Valoracion();
        
        // Configurar categoría para las relaciones
        categoria.setNombre("Literatura");
    }
    
    @Test
    void testResumenConstructorVacio() {
        // Verificar que el constructor vacío inicializa correctamente
        assertNotNull(resumen);
        assertNotNull(resumen.getUsuariosFavorito());
        assertNotNull(resumen.getUsuariosLeido());
        assertNotNull(resumen.getValoraciones());
        assertEquals(0, resumen.getUsuariosFavorito().size());
        assertEquals(0, resumen.getUsuariosLeido().size());
        assertEquals(0, resumen.getValoraciones().size());
        assertFalse(resumen.isRevisado()); // Debería ser false por defecto
    }
    
    
    @Test
    void testSetAndGetTitulo() {
        String titulo = "Resumen de prueba";
        resumen.setTitulo(titulo);
        assertEquals(titulo, resumen.getTitulo());
    }
    
    @Test
    void testSetAndGetEscritor() {
        resumen.setEscritor(usuario);
        assertEquals(usuario, resumen.getEscritor());
    }
    
    @Test
    void testSetAndGetAutor() {
        String autor = "Autor de prueba";
        resumen.setAutor(autor);
        assertEquals(autor, resumen.getAutor());
    }
    
    @Test
    void testSetAndGetImagen() {
        String imagen = "https://example.com/imagen.jpg";
        resumen.setImagen(imagen);
        assertEquals(imagen, resumen.getImagen());
    }
    
    @Test
    void testSetAndGetPremium() {
        resumen.setPremium(true);
        assertTrue(resumen.isPremium());
        
        resumen.setPremium(false);
        assertFalse(resumen.isPremium());
    }
    
    @Test
    void testSetAndGetCategoria() {
        resumen.setCategoria(categoria);
        assertEquals(categoria, resumen.getCategoria());
    }
    
    @Test
    void testSetAndGetValoracionMedia() {
        double valoracionMedia = 3;
        resumen.setValoracionMedia(valoracionMedia);
        assertEquals(valoracionMedia, resumen.getValoracionMedia(), 0.001);
    }
    
    @Test
    void testSetAndGetTexto() {
        String texto = "Este es un texto de prueba para el resumen.";
        resumen.setTexto(texto);
        assertEquals(texto, resumen.getTexto());
    }
    
    @Test
    void testSetAndGetAudio() {
        String audio = "https://example.com/audio.mp3";
        resumen.setAudio(audio);
        assertEquals(audio, resumen.getAudio());
    }
    
    @Test
    void testSetAndGetRevisado() {
        resumen.setRevisado(true);
        assertTrue(resumen.isRevisado());
        
        resumen.setRevisado(false);
        assertFalse(resumen.isRevisado());
    }
    
    @Test
    void testAddAndRemoveValoracion() {
    // Comprobar que inicialmente no hay valoraciones
    assertEquals(0, resumen.getValoraciones().size());
    
    // Añadir valoración
    resumen.addValoracion(valoracion);
    assertEquals(1, resumen.getValoraciones().size());
    assertTrue(resumen.getValoraciones().contains(valoracion));
    
    // Verificar la relación bidireccional
    assertEquals(resumen, valoracion.getResumen());
    
    // Eliminar valoración
    resumen.removeValoracion(valoracion);
    
    // Verificar que la valoración ya no está en la colección
    assertEquals(0, resumen.getValoraciones().size());
    assertFalse(resumen.getValoraciones().contains(valoracion));
}
    
    @Test
    void testAddAndRemoveUsuarioFavorito() {
        // Configurar el usuario y sus colecciones
        usuario.setFavoritos(new HashSet<>());
        
        // Comprobar que inicialmente no hay usuarios favoritos
        assertEquals(0, resumen.getUsuariosFavorito().size());
        
        // Añadir usuario a favoritos
        resumen.addUsuarioFavorito(usuario);
        assertEquals(1, resumen.getUsuariosFavorito().size());
        assertTrue(resumen.getUsuariosFavorito().contains(usuario));
        assertTrue(usuario.getFavoritos().contains(resumen)); // Verifica la relación bidireccional
        
        // Eliminar usuario de favoritos
        resumen.removeUsuarioFavorito(usuario);
        assertEquals(0, resumen.getUsuariosFavorito().size());
        assertFalse(usuario.getFavoritos().contains(resumen)); // Verifica que se eliminó la relación bidireccional
    }
    
    @Test
    void testAddAndRemoveUsuarioLeido() {
        // Configurar el usuario y sus colecciones
        usuario.setResumenesLeidos(new HashSet<>());
        
        // Comprobar que inicialmente no hay usuarios que lo hayan leído
        assertEquals(0, resumen.getUsuariosLeido().size());
        
        // Añadir usuario a leídos
        resumen.addUsuarioLeido(usuario);
        assertEquals(1, resumen.getUsuariosLeido().size());
        assertTrue(resumen.getUsuariosLeido().contains(usuario));
        assertTrue(usuario.getResumenesLeidos().contains(resumen)); // Verifica la relación bidireccional
        
        // Eliminar usuario de leídos
        resumen.removeUsuarioLeido(usuario);
        assertEquals(0, resumen.getUsuariosLeido().size());
        assertFalse(usuario.getResumenesLeidos().contains(resumen)); // Verifica que se eliminó la relación bidireccional
    }
    
    @Test
    void testEqualsConMismoId() {
        Resumen resumen1 = new Resumen();
        resumen1.setId(1L);
        
        Resumen resumen2 = new Resumen();
        resumen2.setId(1L);
        
        // Dos resúmenes con el mismo ID deben ser iguales
        assertEquals(resumen1, resumen2);
        assertEquals(resumen1.hashCode(), resumen2.hashCode());
    }
    
    @Test
    void testEqualsConDiferenteId() {
        Resumen resumen1 = new Resumen();
        resumen1.setId(1L);
        
        Resumen resumen2 = new Resumen();
        resumen2.setId(2L);
        
        // Dos resúmenes con distinto ID no deben ser iguales
        assertNotEquals(resumen1, resumen2);
        assertNotEquals(resumen1.hashCode(), resumen2.hashCode());
    }
    
    @Test
    void testToString() {
        // Configurar un resumen con datos para probar toString()
        resumen.setId(1L);
        resumen.setTitulo("Resumen de prueba");
        resumen.setAutor("Autor de prueba");
        resumen.setPremium(true);
        resumen.setCategoria(categoria);
        resumen.setValoracionMedia(4.5);
        resumen.setRevisado(true);
        
        String toStringResult = resumen.toString();
        
        // Verificar que toString() contiene la información básica
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("titulo=Resumen de prueba"));
        assertTrue(toStringResult.contains("autor=Autor de prueba"));
        assertTrue(toStringResult.contains("premium=true"));
        assertTrue(toStringResult.contains("valoracionMedia=4.5"));
        assertTrue(toStringResult.contains("revisado=true"));
    }
}