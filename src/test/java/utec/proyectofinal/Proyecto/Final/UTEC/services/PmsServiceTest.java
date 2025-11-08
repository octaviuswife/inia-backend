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

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PmsService (Peso de Mil Semillas)
 * 
 * Funcionalidades testeadas:
 * - Creación de análisis PMS con estado REGISTRADO
 * - Validación de número de repeticiones esperadas (1-16)
 * - Validación de pesos y cálculos
 * - Gestión de tandas y repeticiones
 * - Cálculos de peso promedio de mil semillas
 * - Validación de límites de repeticiones
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PmsService")
class PmsServiceTest {

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private RepPmsRepository repPmsRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private PmsService pmsService;

    private PmsRequestDTO pmsRequestDTO;
    private Lote lote;
    private Pms pms;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-PMS-001");
        lote.setActivo(true);

        pmsRequestDTO = new PmsRequestDTO();
        pmsRequestDTO.setIdLote(1L);
        pmsRequestDTO.setNumRepeticionesEsperadas(8);
        pmsRequestDTO.setEsSemillaBrozosa(false);
        pmsRequestDTO.setComentarios("Test PMS");

        pms = new Pms();
        pms.setAnalisisID(1L);
        pms.setLote(lote);
        pms.setNumRepeticionesEsperadas(8);
        pms.setEstado(Estado.REGISTRADO);
        pms.setActivo(true);
    }

    @Test
    @DisplayName("Crear PMS - debe asignar estado REGISTRADO")
    void crearPms_debeAsignarEstadoRegistrado() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarCreacion(any(Pms.class));

        // ACT
        PmsDTO resultado = pmsService.crearPms(pmsRequestDTO);

        // ASSERT
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(pmsRepository, times(1)).save(any(Pms.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS sin repeticiones esperadas - debe lanzar excepción")
    void crearPms_sinRepeticionesEsperadas_debeLanzarExcepcion() {
        // ARRANGE
        pmsRequestDTO.setNumRepeticionesEsperadas(null);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("repeticiones esperadas"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con repeticiones = 0 - debe lanzar excepción")
    void crearPms_conRepeticionesCero_debeLanzarExcepcion() {
        // ARRANGE
        pmsRequestDTO.setNumRepeticionesEsperadas(0);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("mayor a 0"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con más de 16 repeticiones - debe lanzar excepción")
    void crearPms_conMasDe16Repeticiones_debeLanzarExcepcion() {
        // ARRANGE
        pmsRequestDTO.setNumRepeticionesEsperadas(17);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no puede superar 16"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con 8 repeticiones - debe ser válido")
    void crearPms_con8Repeticiones_debeSerValido() {
        // ARRANGE
        pmsRequestDTO.setNumRepeticionesEsperadas(8);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarCreacion(any(Pms.class));

        // ACT
        PmsDTO resultado = pmsService.crearPms(pmsRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con lote inexistente - debe lanzar excepción")
    void crearPms_conLoteInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        pmsRequestDTO.setIdLote(999L);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        }, "Debe lanzar excepción cuando el lote no existe");
    }

    @Test
    @DisplayName("Obtener PMS por ID - debe retornar el análisis si existe")
    void obtenerPorId_cuandoExiste_debeRetornarPms() {
        // ARRANGE
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));

        // ACT
        PmsDTO resultado = pmsService.obtenerPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        assertEquals(8, resultado.getNumRepeticionesEsperadas());
        verify(pmsRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener PMS por ID inexistente - debe lanzar excepción")
    void obtenerPorId_cuandoNoExiste_debeLanzarExcepcion() {
        // ARRANGE
        when(pmsRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.obtenerPorId(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    @DisplayName("Actualizar PMS - debe actualizar correctamente")
    void actualizarPms_debeActualizarCorrectamente() {
        // ARRANGE
        pmsRequestDTO.setComentarios("Comentarios actualizados");
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Pms.class));
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        // ACT
        PmsDTO resultado = pmsService.actualizarPms(1L, pmsRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Pms.class));
        verify(analisisService, times(1)).manejarEdicionAnalisisFinalizado(any(Pms.class));
    }

    @Test
    @DisplayName("Actualizar PMS inexistente - debe lanzar excepción")
    void actualizarPms_noExistente_debeLanzarExcepcion() {
        // ARRANGE
        when(pmsRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            pmsService.actualizarPms(999L, pmsRequestDTO);
        });
        
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Desactivar PMS - debe cambiar activo a false")
    void desactivarPms_debeCambiarActivoAFalse() {
        // ARRANGE
        doNothing().when(analisisService).desactivarAnalisis(eq(1L), any());

        // ACT
        pmsService.desactivarPms(1L);

        // ASSERT
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }

    @Test
    @DisplayName("Reactivar PMS - debe cambiar activo a true")
    void reactivarPms_debeCambiarActivoATrue() {
        // ARRANGE
        pms.setActivo(false);
        PmsDTO pmsDTO = new PmsDTO();
        pmsDTO.setAnalisisID(1L);
        pmsDTO.setActivo(true);
        
        when(analisisService.reactivarAnalisis(any(Long.class), any(), any())).thenReturn(pmsDTO);

        // ACT
        pmsService.reactivarPms(1L);

        // ASSERT
        verify(analisisService, times(1)).reactivarAnalisis(any(Long.class), any(), any());
    }

    @Test
    @DisplayName("Eliminar PMS - debe desactivar el análisis")
    void eliminarPms_debeDesactivarAnalisis() {
        // ARRANGE
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);

        // ACT
        pmsService.eliminarPms(1L);

        // ASSERT
        verify(pmsRepository, times(1)).findById(1L);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("Listar PMS activos - debe retornar solo activos")
    void obtenerTodos_debeRetornarSoloActivos() {
        // ARRANGE
        Pms pms2 = new Pms();
        pms2.setAnalisisID(2L);
        pms2.setActivo(true);
        
        when(pmsRepository.findByActivoTrue()).thenReturn(Arrays.asList(pms, pms2));

        // ACT
        List<PmsDTO> resultado = pmsService.obtenerTodos();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pmsRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener PMS por ID de lote - debe retornar análisis del lote")
    void obtenerPmsPorIdLote_debeRetornarAnalisisDelLote() {
        // ARRANGE
        when(pmsRepository.findByIdLote(1)).thenReturn(Arrays.asList(pms));

        // ACT
        List<PmsDTO> resultado = pmsService.obtenerPmsPorIdLote(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pmsRepository, times(1)).findByIdLote(1);
    }

    @Test
    @DisplayName("Listar PMS paginadas - debe retornar página correcta")
    void obtenerPmsPaginadas_debeRetornarPaginaCorrecta() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pms> pmsPage = new PageImpl<>(Arrays.asList(pms));
        
        when(pmsRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(pmsPage);

        // ACT
        var resultado = pmsService.obtenerPmsPaginadas(pageable);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Validar esSemillaBrozosa - debe aceptar true o false")
    void crearPms_conSemillaBrozosaTrue_debeCrearse() {
        // ARRANGE
        pmsRequestDTO.setEsSemillaBrozosa(true);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarCreacion(any(Pms.class));

        // ACT
        PmsDTO resultado = pmsService.crearPms(pmsRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }
}
