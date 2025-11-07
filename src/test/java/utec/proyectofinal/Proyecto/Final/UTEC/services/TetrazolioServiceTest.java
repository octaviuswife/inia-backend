package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TetrazolioService
 * 
 * ¿Qué validamos?
 * - Creación de análisis de tetrazolio con estado REGISTRADO
 * - Validación de número de repeticiones esperadas (> 0)
 * - Asignación correcta de lote activo
 * - Configuración de parámetros de tinción
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TetrazolioService")
class TetrazolioServiceTest {

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private TetrazolioService tetrazolioService;

    private TetrazolioRequestDTO tetrazolioRequestDTO;
    private Lote lote;
    private Tetrazolio tetrazolio;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-TEST-001");
        lote.setActivo(true);

        tetrazolioRequestDTO = new TetrazolioRequestDTO();
        tetrazolioRequestDTO.setIdLote(1L);
        tetrazolioRequestDTO.setNumRepeticionesEsperadas(4);
        tetrazolioRequestDTO.setNumSemillasPorRep(100);
        tetrazolioRequestDTO.setPretratamiento("Remojo 24h");
        tetrazolioRequestDTO.setConcentracion("1%");
        tetrazolioRequestDTO.setTincionHs(24);
        tetrazolioRequestDTO.setTincionTemp(30);
        tetrazolioRequestDTO.setFecha(LocalDate.now());

        tetrazolio = new Tetrazolio();
        tetrazolio.setAnalisisID(1L);
        tetrazolio.setLote(lote);
        tetrazolio.setEstado(Estado.REGISTRADO);
        tetrazolio.setActivo(true);
        tetrazolio.setNumRepeticionesEsperadas(4);
    }

    @Test
    @DisplayName("Crear tetrazolio - debe asignar estado REGISTRADO")
    void crearTetrazolio_debeAsignarEstadoRegistrado() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(tetrazolioRepository.save(any(Tetrazolio.class))).thenReturn(tetrazolio);

        // ACT
        TetrazolioDTO resultado = tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);

        // ASSERT
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(tetrazolioRepository, times(1)).save(any(Tetrazolio.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Tetrazolio.class));
    }

    @Test
    @DisplayName("Crear tetrazolio sin repeticiones esperadas - debe lanzar excepción")
    void crearTetrazolio_sinRepeticionesEsperadas_debeLanzarExcepcion() {
        // ARRANGE
        tetrazolioRequestDTO.setNumRepeticionesEsperadas(null);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);
        }, "Debe lanzar excepción cuando no se especifican repeticiones esperadas");
    }

    @Test
    @DisplayName("Crear tetrazolio con repeticiones = 0 - debe lanzar excepción")
    void crearTetrazolio_conRepeticionesCero_debeLanzarExcepcion() {
        // ARRANGE
        tetrazolioRequestDTO.setNumRepeticionesEsperadas(0);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);
        }, "Debe lanzar excepción cuando repeticiones = 0");
    }

    @Test
    @DisplayName("Crear tetrazolio con lote inactivo - debe lanzar excepción")
    void crearTetrazolio_conLoteInactivo_debeLanzarExcepcion() {
        // ARRANGE
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);
        }, "Debe lanzar excepción cuando el lote está inactivo");
    }

    @Test
    @DisplayName("Obtener tetrazolio por ID - debe retornar el análisis si existe")
    void obtenerTetrazolioPorId_cuandoExiste_debeRetornarTetrazolio() {
        // ARRANGE
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));

        // ACT
        TetrazolioDTO resultado = tetrazolioService.obtenerTetrazolioPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        assertEquals(4, resultado.getNumRepeticionesEsperadas());
        verify(tetrazolioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Desactivar tetrazolio - debe cambiar activo a false")
    void desactivarTetrazolio_debeCambiarActivoAFalse() {
        // ARRANGE
        doNothing().when(analisisService).desactivarAnalisis(anyLong(), any());

        // ACT
        tetrazolioService.desactivarTetrazolio(1L);

        // ASSERT
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }
}
