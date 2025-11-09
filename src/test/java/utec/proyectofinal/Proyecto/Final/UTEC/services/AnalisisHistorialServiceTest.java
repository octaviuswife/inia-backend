package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.AnalisisHistorial;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisHistorialRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de AnalisisHistorialService")
class AnalisisHistorialServiceTest {

    @Mock
    private AnalisisHistorialRepository analisisHistorialRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AnalisisHistorialService analisisHistorialService;

    private Usuario usuario;
    private Analisis analisis;
    private AnalisisHistorial historial;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setUsuarioID(1);
        usuario.setNombre("testuser");
        usuario.setNombres("Test");
        usuario.setApellidos("User");

        analisis = new Germinacion();
        analisis.setAnalisisID(1L);

        historial = new AnalisisHistorial();
        historial.setId(1L);
        historial.setAnalisis(analisis);
        historial.setUsuario(usuario);
        historial.setFechaHora(LocalDateTime.now());
    }

    @Test
    @DisplayName("Registrar creación de análisis - debe guardar en historial")
    void registrarCreacion_debeGuardarEnHistorial() {
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuario));
        when(analisisHistorialRepository.save(any(AnalisisHistorial.class))).thenReturn(historial);

        analisisHistorialService.registrarCreacion(analisis);

        verify(analisisHistorialRepository, times(1)).save(any(AnalisisHistorial.class));
        verify(usuarioRepository, times(1)).findByNombreIgnoreCase("testuser");
    }

    @Test
    @DisplayName("Registrar modificación de análisis - debe guardar en historial")
    void registrarModificacion_debeGuardarEnHistorial() {
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.of(usuario));
        when(analisisHistorialRepository.save(any(AnalisisHistorial.class))).thenReturn(historial);

        analisisHistorialService.registrarModificacion(analisis);

        verify(analisisHistorialRepository, times(1)).save(any(AnalisisHistorial.class));
        verify(usuarioRepository, times(1)).findByNombreIgnoreCase("testuser");
    }

    @Test
    @DisplayName("Obtener historial de análisis - debe retornar lista de DTOs")
    void obtenerHistorialAnalisis_debeRetornarListaDTOs() {
        List<AnalisisHistorial> historiales = Arrays.asList(historial);
        when(analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(1L)).thenReturn(historiales);
        when(analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(1L)).thenReturn(historiales);

        List<AnalisisHistorialDTO> resultado = analisisHistorialService.obtenerHistorialAnalisis(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(analisisHistorialRepository, times(2)).findByAnalisisIdOrderByFechaHoraDesc(1L);
    }

    @Test
    @DisplayName("Obtener historial de usuario - debe retornar lista de DTOs")
    void obtenerHistorialUsuario_debeRetornarListaDTOs() {
        List<AnalisisHistorial> historiales = Arrays.asList(historial);
        when(analisisHistorialRepository.findByUsuarioIdOrderByFechaHoraDesc(1)).thenReturn(historiales);
        when(analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(1L)).thenReturn(historiales);

        List<AnalisisHistorialDTO> resultado = analisisHistorialService.obtenerHistorialUsuario(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(analisisHistorialRepository, times(1)).findByUsuarioIdOrderByFechaHoraDesc(1);
    }

    @Test
    @DisplayName("Registrar acción sin usuario autenticado - no debe fallar")
    void registrarAccion_sinUsuarioAutenticado_noDebeFallar() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertDoesNotThrow(() -> {
            analisisHistorialService.registrarCreacion(analisis);
        });

        verify(analisisHistorialRepository, never()).save(any(AnalisisHistorial.class));
    }

    @Test
    @DisplayName("Registrar acción con usuario no encontrado - no debe fallar")
    void registrarAccion_usuarioNoEncontrado_noDebeFallar() {
        when(authentication.getName()).thenReturn("testuser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(usuarioRepository.findByNombreIgnoreCase("testuser")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> {
            analisisHistorialService.registrarCreacion(analisis);
        });

        verify(analisisHistorialRepository, never()).save(any(AnalisisHistorial.class));
    }

    @Test
    @DisplayName("Obtener historial de análisis inexistente - debe retornar lista vacía")
    void obtenerHistorialAnalisis_analisisInexistente_debeRetornarListaVacia() {
        when(analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(999L)).thenReturn(Arrays.asList());

        List<AnalisisHistorialDTO> resultado = analisisHistorialService.obtenerHistorialAnalisis(999L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
