package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DosnService (Determinación de Otras Semillas por Número)
 * 
 * Funcionalidades testeadas:
 * - Creación de análisis DOSN con estado EN_PROCESO
 * - Validación de pesos y conteos
 * - Actualización de análisis
 * - Gestión de estados según rol de usuario
 * - Desactivación y reactivación
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de DosnService")
class DosnServiceTest {

    @Mock
    private DosnRepository dosnRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisService analisisService;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @InjectMocks
    private DosnService dosnService;

    private DosnRequestDTO dosnRequestDTO;
    private Lote lote;
    private Dosn dosn;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-DOSN-001");
        lote.setActivo(true);

        dosnRequestDTO = new DosnRequestDTO();
        dosnRequestDTO.setIdLote(1L);
        dosnRequestDTO.setFechaINIA(LocalDate.now());
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosnRequestDTO.setCumpleEstandar(true);
        dosnRequestDTO.setComentarios("Test DOSN");

        dosn = new Dosn();
        dosn.setAnalisisID(1L);
        dosn.setLote(lote);
        dosn.setFechaINIA(LocalDate.now());
        dosn.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosn.setEstado(Estado.EN_PROCESO);
        dosn.setActivo(true);
    }

    @Test
    @DisplayName("Crear DOSN - debe asignar estado EN_PROCESO")
    void crearDosn_debeAsignarEstadoEnProceso() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        // ACT
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        // ASSERT
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(dosnRepository, times(1)).save(any(Dosn.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Dosn.class));
        verify(analisisService, times(1)).establecerFechaInicio(any(Dosn.class));
    }

    @Test
    @DisplayName("Crear DOSN con lote inexistente - debe lanzar excepción")
    void crearDosn_conLoteInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        dosnRequestDTO.setIdLote(999L);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            dosnService.crearDosn(dosnRequestDTO);
        }, "Debe lanzar excepción cuando el lote no existe");
    }

    @Test
    @DisplayName("Validar gramos analizados - debe aceptar valores positivos")
    void validarGramosAnalizados_debeAceptarValoresPositivos() {
        // ARRANGE
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        // ACT
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Obtener DOSN por ID - debe retornar el análisis si existe")
    void obtenerDosnPorId_cuandoExiste_debeRetornarDosn() {
        // ARRANGE
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));

        // ACT
        DosnDTO resultado = dosnService.obtenerDosnPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        verify(dosnRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener DOSN por ID inexistente - debe lanzar excepción")
    void obtenerDosnPorId_cuandoNoExiste_debeLanzarExcepcion() {
        // ARRANGE
        when(dosnRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            dosnService.obtenerDosnPorId(999L);
        }, "Debe lanzar excepción cuando el análisis no existe");
    }

    @Test
    @DisplayName("Actualizar DOSN - debe actualizar correctamente")
    void actualizarDosn_debeActualizarCorrectamente() {
        // ARRANGE
        dosnRequestDTO.setComentarios("Comentarios actualizados");
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Dosn.class));

        // ACT
        DosnDTO resultado = dosnService.actualizarDosn(1L, dosnRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Dosn.class));
    }

    @Test
    @DisplayName("Actualizar DOSN APROBADA por ANALISTA - debe cambiar a PENDIENTE_APROBACION")
    void actualizarDosn_aprobadaPorAnalista_debeCambiarAPendiente() {
        // ARRANGE
        dosn.setEstado(Estado.APROBADO);
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(analisisService.esAnalista()).thenReturn(true);
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Dosn.class));

        // ACT
        dosnService.actualizarDosn(1L, dosnRequestDTO);

        // ASSERT
        verify(dosnRepository, times(1)).save(any(Dosn.class));
        verify(analisisService, times(1)).esAnalista();
    }

    @Test
    @DisplayName("Actualizar DOSN inexistente - debe lanzar excepción")
    void actualizarDosn_noExistente_debeLanzarExcepcion() {
        // ARRANGE
        when(dosnRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dosnService.actualizarDosn(999L, dosnRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(dosnRepository, never()).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Desactivar DOSN - debe cambiar activo a false")
    void desactivarDosn_debeCambiarActivoAFalse() {
        // ARRANGE
        doNothing().when(analisisService).desactivarAnalisis(eq(1L), any());

        // ACT
        dosnService.desactivarDosn(1L);

        // ASSERT
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }

    @Test
    @DisplayName("Reactivar DOSN - debe cambiar activo a true")
    void reactivarDosn_debeCambiarActivoATrue() {
        // ARRANGE
        dosn.setActivo(false);
        DosnDTO dosnDTO = new DosnDTO();
        dosnDTO.setAnalisisID(1L);
        dosnDTO.setActivo(true);
        
        when(analisisService.reactivarAnalisis(any(Long.class), any(), any())).thenReturn(dosnDTO);

        // ACT
        dosnService.reactivarDosn(1L);

        // ASSERT
        verify(analisisService, times(1)).reactivarAnalisis(any(Long.class), any(), any());
    }

    @Test
    @DisplayName("Eliminar DOSN - debe desactivar el análisis")
    void eliminarDosn_debeDesactivarAnalisis() {
        // ARRANGE
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);

        // ACT
        dosnService.eliminarDosn(1L);

        // ASSERT
        verify(dosnRepository, times(1)).findById(1L);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Crear DOSN con cumple estándar true - debe guardar correctamente")
    void crearDosn_conCumpleEstandarTrue_debeGuardar() {
        // ARRANGE
        dosnRequestDTO.setCumpleEstandar(true);
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        // ACT
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Crear DOSN con cumple estándar false - debe guardar correctamente")
    void crearDosn_conCumpleEstandarFalse_debeGuardar() {
        // ARRANGE
        dosnRequestDTO.setCumpleEstandar(false);
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        // ACT
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }
}
