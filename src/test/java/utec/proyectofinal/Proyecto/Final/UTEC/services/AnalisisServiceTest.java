package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de AnalisisService")
class AnalisisServiceTest {

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private AnalisisService analisisService;

    private Analisis analisis;

    @BeforeEach
    void setUp() {
        analisis = new Germinacion();
        analisis.setAnalisisID(1L);
        analisis.setEstado(Estado.EN_PROCESO);
        analisis.setActivo(true);
    }

    @Test
    @DisplayName("Establecer fecha inicio - debe asignar fecha actual")
    void establecerFechaInicio_debeAsignarFechaActual() {
        analisisService.establecerFechaInicio(analisis);

        assertNotNull(analisis.getFechaInicio());
        assertTrue(analisis.getFechaInicio().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Aprobar análisis pendiente - debe cambiar a APROBADO")
    void aprobarAnalisis_pendiente_debeCambiarAAprobado() {
        analisis.setEstado(Estado.PENDIENTE_APROBACION);

        Analisis resultado = analisisService.aprobarAnalisis(analisis);

        assertEquals(Estado.APROBADO, resultado.getEstado());
        assertNotNull(resultado.getFechaFin());
        verify(analisisHistorialService, times(1)).registrarModificacion(analisis);
    }

    @Test
    @DisplayName("Aprobar análisis no pendiente - debe lanzar excepción")
    void aprobarAnalisis_noPendiente_debeLanzarExcepcion() {
        analisis.setEstado(Estado.EN_PROCESO);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analisisService.aprobarAnalisis(analisis);
        });

        assertTrue(exception.getMessage().contains("PENDIENTE_APROBACION"));
    }

    @Test
    @DisplayName("Aprobar análisis inactivo - debe lanzar excepción")
    void aprobarAnalisis_analisisInactivo_debeLanzarExcepcion() {
        analisis.setActivo(false);
        analisis.setEstado(Estado.PENDIENTE_APROBACION);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analisisService.aprobarAnalisis(analisis);
        });

        assertEquals("No se puede aprobar un análisis inactivo", exception.getMessage());
    }

    @Test
    @DisplayName("Marcar para repetir - debe cambiar a A_REPETIR")
    void marcarParaRepetir_debeCambiarARepetir() {
        analisis.setEstado(Estado.APROBADO);

        Analisis resultado = analisisService.marcarParaRepetir(analisis);

        assertEquals(Estado.A_REPETIR, resultado.getEstado());
        verify(analisisHistorialService, times(1)).registrarModificacion(analisis);
    }

    @Test
    @DisplayName("Marcar para repetir análisis inactivo - debe lanzar excepción")
    void marcarParaRepetir_analisisInactivo_debeLanzarExcepcion() {
        analisis.setActivo(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analisisService.marcarParaRepetir(analisis);
        });

        assertEquals("No se puede marcar para repetir un análisis inactivo", exception.getMessage());
    }
}
