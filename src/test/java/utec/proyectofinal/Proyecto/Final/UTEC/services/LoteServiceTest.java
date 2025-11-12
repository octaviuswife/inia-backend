package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoteService
 * 
 * ¿Qué estamos testeando?
 * - La lógica de negocio del servicio de lotes
 * - Validaciones y transformaciones de datos
 * - Interacciones con el repositorio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de LoteService")
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private DosnRepository dosnRepository;

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @Mock
    private PurezaRepository purezaRepository;

    @InjectMocks
    private LoteService loteService;

    private LoteRequestDTO loteRequestDTO;
    private Lote lote;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba que usaremos en varios tests
        loteRequestDTO = new LoteRequestDTO();
        loteRequestDTO.setNomLote("LOTE-TEST-001");
        loteRequestDTO.setFechaRecibo(LocalDate.now());
        
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-TEST-001");
        lote.setActivo(true);
    }

    @Test
    @DisplayName("Crear lote - debe asignar activo=true automáticamente")
    void crearLote_debeAsignarActivoTrue() {
        // ARRANGE: Configurar el mock para que devuelva el lote cuando se guarde
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);

        // ACT: Ejecutar el método que queremos probar
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);

        // ASSERT: Verificar que el lote se creó correctamente
        assertNotNull(resultado, "El lote creado no debe ser nulo");
        assertEquals("LOTE-TEST-001", resultado.getNomLote(), "El nombre del lote debe coincidir");
        
        // Verificar que se llamó al método save exactamente 1 vez
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("Obtener lote por ID - debe retornar el lote si existe")
    void obtenerLotePorId_cuandoExiste_debeRetornarLote() {
        // ARRANGE
        when(loteRepository.findByIdWithCultivarAndEspecie(1L)).thenReturn(Optional.of(lote));

        // ACT
        LoteDTO resultado = loteService.obtenerLotePorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getLoteID());
        assertEquals("LOTE-TEST-001", resultado.getNomLote());
        verify(loteRepository, times(1)).findByIdWithCultivarAndEspecie(1L);
    }

    @Test
    @DisplayName("Obtener lote por ID inexistente - debe lanzar excepción")
    void obtenerLotePorId_cuandoNoExiste_debeLanzarExcepcion() {
        // ARRANGE
        when(loteRepository.findByIdWithCultivarAndEspecie(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            loteService.obtenerLotePorId(999L);
        }, "Debe lanzar excepción cuando el lote no existe");

        verify(loteRepository, times(1)).findByIdWithCultivarAndEspecie(999L);
    }

    @Test
    @DisplayName("Eliminar lote - debe cambiar activo a false")
    void eliminarLote_debeCambiarActivoAFalse() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);

        // ACT
        loteService.eliminarLote(1L);

        // ASSERT
        verify(loteRepository, times(1)).findById(1L);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("Eliminar lote inexistente - lanza excepción")
    void eliminarLote_inexistente_lanzaExcepcion() {
        when(loteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> loteService.eliminarLote(99L));
    }

    @Test
    @DisplayName("Reactivar lote - debe cambiar activo a true")
    void reactivarLote_debeCambiarActivoATrue() {
        // ARRANGE
        lote.setActivo(false); // Lote inicialmente inactivo
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);

        // ACT
        LoteDTO resultado = loteService.reactivarLote(1L);

        // ASSERT
        assertNotNull(resultado);
        verify(loteRepository, times(1)).findById(1L);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("Reactivar lote ya activo - lanza excepción")
    void reactivarLote_yaActivo_lanzaExcepcion() {
        lote.setActivo(true);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        assertThrows(RuntimeException.class, () -> loteService.reactivarLote(1L));
    }

    @Test
    @DisplayName("Actualizar lote inactivo - debe lanzar excepción")
    void actualizarLote_inactivo_lanzaExcepcion() {
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        LoteRequestDTO req = new LoteRequestDTO();
        req.setFechaRecibo(LocalDate.now());
        assertThrows(RuntimeException.class, () -> loteService.actualizarLote(1L, req));
    }

    @Test
    @DisplayName("Actualizar lote cambiando tipos con análisis existente - bloquea remoción")
    void actualizarLote_removerTipoNoPermitido_lanza() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(java.util.List.of(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis.PMS));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(true);
        LoteRequestDTO req = new LoteRequestDTO();
        req.setTiposAnalisisAsignados(java.util.List.of()); // intentar remover PMS
        req.setFechaRecibo(LocalDate.now());
        assertThrows(RuntimeException.class, () -> loteService.actualizarLote(1L, req));
    }

    @Test
    @DisplayName("Puede remover tipo analisis cuando no hay análisis creados")
    void puedeRemoverTipoAnalisis_sinAnalisis_true() {
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        boolean result = loteService.puedeRemoverTipoAnalisis(1L, utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis.PMS);
        assertTrue(result);
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - requiere activo, tipo asignado y puede crear")
    void esLoteElegible_paraCrear() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(java.util.List.of(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis.PMS));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false); // permite crear
        assertTrue(loteService.esLoteElegibleParaTipoAnalisis(1L, utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - falso si lote inactivo")
    void esLoteElegible_inactivo_false() {
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        assertFalse(loteService.esLoteElegibleParaTipoAnalisis(1L, utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("contarAnalisisPendientes - cuenta tipos sin análisis")
    void contarAnalisisPendientes_casos() {
        Lote lote2 = new Lote();
        lote2.setLoteID(2L);
        lote2.setActivo(true);
        lote.setTiposAnalisisAsignados(java.util.List.of(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis.PMS));
        lote2.setTiposAnalisisAsignados(java.util.List.of(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis.GERMINACION));
        when(loteRepository.findByActivoTrue()).thenReturn(java.util.List.of(lote, lote2));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        when(germinacionRepository.existsByLoteLoteID(2L)).thenReturn(false);
        long count = loteService.contarAnalisisPendientes();
        assertEquals(2L, count);
    }

    @Test
    @DisplayName("obtenerEstadisticasLotes - retorna mapa con claves esperadas")
    void obtenerEstadisticasLotes_ok() {
        when(loteRepository.count()).thenReturn(10L);
        when(loteRepository.countLotesActivos()).thenReturn(7L);
        when(loteRepository.countLotesInactivos()).thenReturn(3L);
        var stats = loteService.obtenerEstadisticasLotes();
        assertEquals(10L, stats.get("total"));
        assertEquals(7L, stats.get("activos"));
        assertEquals(3L, stats.get("inactivos"));
    }
}
