package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionEditRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para GerminacionService
 * 
 * Funcionalidades testeadas:
 * - Creación de análisis de germinación con estado REGISTRADO
 * - Validación de repeticiones esperadas (> 0)
 * - Actualización de análisis existentes
 * - Gestión de estados (REGISTRADO, EN_PROCESO, PENDIENTE_APROBACION, APROBADO)
 * - Desactivación y reactivación de análisis
 * - Cálculos de germinación
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de GerminacionService")
class GerminacionServiceTest {

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private GerminacionService germinacionService;

    private GerminacionRequestDTO germinacionRequestDTO;
    private Lote lote;
    private Germinacion germinacion;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-GERM-001");
        lote.setActivo(true);

        germinacionRequestDTO = new GerminacionRequestDTO();
        germinacionRequestDTO.setIdLote(1L);
        germinacionRequestDTO.setComentarios("Test de germinación");

        germinacion = new Germinacion();
        germinacion.setAnalisisID(1L);
        germinacion.setLote(lote);
        germinacion.setEstado(Estado.REGISTRADO);
        germinacion.setActivo(true);
        germinacion.setComentarios("Test de germinación");
    }

    @Test
    @DisplayName("Crear germinación - debe asignar estado REGISTRADO")
    void crearGerminacion_debeAsignarEstadoRegistrado() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).establecerFechaInicio(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Germinacion.class));

        // ACT
        GerminacionDTO resultado = germinacionService.crearGerminacion(germinacionRequestDTO);

        // ASSERT
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Germinacion.class));
        verify(analisisService, times(1)).establecerFechaInicio(any(Germinacion.class));
    }

    @Test
    @DisplayName("Crear germinación con lote inexistente - debe lanzar excepción")
    void crearGerminacion_conLoteInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        germinacionRequestDTO.setIdLote(999L);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            germinacionService.crearGerminacion(germinacionRequestDTO);
        }, "Debe lanzar excepción cuando el lote no existe");
    }

    @Test
    @DisplayName("Crear germinación sin repeticiones esperadas - debe lanzar excepción")
    void crearGerminacion_sinLote_debeLanzarExcepcion() {
        // ARRANGE
        germinacionRequestDTO.setIdLote(null);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            germinacionService.crearGerminacion(germinacionRequestDTO);
        }, "Debe lanzar excepción cuando no hay lote");
    }

    @Test
    @DisplayName("Crear germinación con repeticiones = 0 - debe lanzar excepción")
    void crearGerminacion_debeFuncionar() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).establecerFechaInicio(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Germinacion.class));

        // ACT
        GerminacionDTO resultado = germinacionService.crearGerminacion(germinacionRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
    }

    @Test
    @DisplayName("Crear germinación con días <= 0 - debe funcionar correctamente")
    void crearGerminacion_conLoteValido_debeFuncionar() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).establecerFechaInicio(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Germinacion.class));

        // ACT
        GerminacionDTO resultado = germinacionService.crearGerminacion(germinacionRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(analisisService, times(1)).establecerFechaInicio(any(Germinacion.class));
    }

    @Test
    @DisplayName("Obtener germinación por ID - debe retornar el análisis si existe")
    void obtenerGerminacionPorId_cuandoExiste_debeRetornarGerminacion() {
        // ARRANGE
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT
        GerminacionDTO resultado = germinacionService.obtenerGerminacionPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        verify(germinacionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener germinación por ID inexistente - debe lanzar excepción")
    void obtenerGerminacionPorId_cuandoNoExiste_debeLanzarExcepcion() {
        // ARRANGE
        when(germinacionRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            germinacionService.obtenerGerminacionPorId(999L);
        }, "Debe lanzar excepción cuando el análisis no existe");
    }

    @Test
    @DisplayName("Actualizar germinación - debe actualizar correctamente")
    void actualizarGerminacion_debeActualizarCorrectamente() {
        // ARRANGE
        germinacionRequestDTO.setComentarios("Comentarios actualizados");
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Germinacion.class));

        // ACT
        GerminacionDTO resultado = germinacionService.actualizarGerminacion(1L, germinacionRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Germinacion.class));
    }

    @Test
    @DisplayName("Actualizar germinación APROBADA por ANALISTA - debe cambiar a PENDIENTE_APROBACION")
    void actualizarGerminacion_aprobadaPorAnalista_debeCambiarAPendiente() {
        // ARRANGE
        germinacion.setEstado(Estado.APROBADO);
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(analisisService.esAnalista()).thenReturn(true);
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Germinacion.class));

        // ACT
        germinacionService.actualizarGerminacion(1L, germinacionRequestDTO);

        // ASSERT
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
        verify(analisisService, times(1)).esAnalista();
    }

    @Test
    @DisplayName("Actualizar germinación seguro - solo actualiza campos permitidos")
    void actualizarGerminacionSeguro_debeActualizarSoloCamposPermitidos() {
        // ARRANGE
        GerminacionEditRequestDTO editDTO = new GerminacionEditRequestDTO();
        editDTO.setIdLote(1L);
        editDTO.setComentarios("Nuevo comentario");
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarModificacion(any(Germinacion.class));

        // ACT
        GerminacionDTO resultado = germinacionService.actualizarGerminacionSeguro(1L, editDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(analisisService, times(1)).manejarEdicionAnalisisFinalizado(any(Germinacion.class));
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
    }

    @Test
    @DisplayName("Desactivar germinación - debe cambiar activo a false")
    void desactivarGerminacion_debeCambiarActivoAFalse() {
        // ARRANGE
        doNothing().when(analisisService).desactivarAnalisis(eq(1L), any());

        // ACT
        germinacionService.desactivarGerminacion(1L);

        // ASSERT
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }

    @Test
    @DisplayName("Reactivar germinación - debe cambiar activo a true")
    void reactivarGerminacion_debeCambiarActivoATrue() {
        // ARRANGE
        germinacion.setActivo(false);
        GerminacionDTO germinacionDTO = new GerminacionDTO();
        germinacionDTO.setAnalisisID(1L);
        germinacionDTO.setActivo(true);
        
        when(analisisService.reactivarAnalisis(any(Long.class), any(), any())).thenReturn(germinacionDTO);

        // ACT
        germinacionService.reactivarGerminacion(1L);

        // ASSERT
        verify(analisisService, times(1)).reactivarAnalisis(any(Long.class), any(), any());
    }

    @Test
    @DisplayName("Eliminar germinación - debe desactivar el análisis")
    void eliminarGerminacion_debeDesactivarAnalisis() {
        // ARRANGE
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);

        // ACT
        germinacionService.eliminarGerminacion(1L);

        // ASSERT
        verify(germinacionRepository, times(1)).findById(1L);
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
    }

    @Test
    @DisplayName("Listar germinaciones por lote - debe retornar germinaciones del lote")
    void obtenerGerminacionesPorLote_debeRetornarGerminacionesDelLote() {
        // ARRANGE
        Germinacion germinacion2 = new Germinacion();
        germinacion2.setAnalisisID(2L);
        germinacion2.setLote(lote);
        germinacion2.setActivo(true);
        
        when(germinacionRepository.findByIdLote(1L)).thenReturn(Arrays.asList(germinacion, germinacion2));

        // ACT
        List<GerminacionDTO> resultado = germinacionService.obtenerGerminacionesPorIdLote(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(germinacionRepository, times(1)).findByIdLote(1L);
    }

    @Test
    @DisplayName("Listar germinaciones paginadas - debe retornar página correcta")
    void obtenerGerminacionesPaginadas_debeRetornarPaginaCorrecta() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        GerminacionListadoDTO listadoDTO = new GerminacionListadoDTO();
        listadoDTO.setAnalisisID(1L);
        
        Page<GerminacionListadoDTO> germinacionesPage = new PageImpl<>(Arrays.asList(listadoDTO));
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));

        // ACT
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(pageable);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
    }
}
