package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RepPmsService (Repeticiones de Peso de Mil Semillas)
 * 
 * Funcionalidades testeadas:
 * - Creación de repeticiones de PMS
 * - Validación de límite de 16 repeticiones
 * - Asignación automática de tandas
 * - Cálculos de validación y estadísticas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RepPmsService")
class RepPmsServiceTest {

    @Mock
    private RepPmsRepository repPmsRepository;

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private PmsService pmsService;

    @Mock
    private AnalisisService analisisService;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @InjectMocks
    private RepPmsService repPmsService;

    private Pms pms;
    private RepPms repPms;
    private RepPmsRequestDTO repPmsRequestDTO;

    @BeforeEach
    void setUp() {
        pms = new Pms();
        pms.setAnalisisID(1L);
        pms.setEstado(Estado.REGISTRADO);
        pms.setNumRepeticionesEsperadas(8);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        pms.setActivo(true);

        repPms = new RepPms();
        repPms.setRepPMSID(10L);
        repPms.setNumRep(1);
        repPms.setNumTanda(1);
        repPms.setPeso(new BigDecimal("45.5"));
        repPms.setValido(null);
        repPms.setPms(pms);

        repPmsRequestDTO = new RepPmsRequestDTO();
        repPmsRequestDTO.setNumRep(1);
        repPmsRequestDTO.setPeso(new BigDecimal("45.5"));
    }

    @Test
    @DisplayName("Crear repetición PMS - debe crear exitosamente")
    void crearRepeticion_debeCrearExitosamente() {
        // ARRANGE
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(0L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList());
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        // ACT
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(10L, resultado.getRepPMSID());
        assertEquals(1, resultado.getNumRep());
        assertEquals(new BigDecimal("45.5"), resultado.getPeso());
        verify(repPmsRepository, times(1)).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Crear repetición - debe cambiar estado a EN_PROCESO en primera repetición")
    void crearRepeticion_debeCambiarEstadoEnProceso() {
        // ARRANGE
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(0L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList());
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        // ACT
        repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        // ASSERT
        verify(pmsRepository).save(argThat(p -> p.getEstado() == Estado.EN_PROCESO));
    }

    @Test
    @DisplayName("Crear repetición - debe lanzar excepción si PMS no existe")
    void crearRepeticion_debeLanzarExcepcionSiPmsNoExiste() {
        // ARRANGE
        when(pmsRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(999L, repPmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(repPmsRepository, never()).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Crear repetición - debe lanzar excepción si PMS está finalizado")
    void crearRepeticion_debeLanzarExcepcionSiPmsEstFinalizado() {
        // ARRANGE
        pms.setEstado(Estado.APROBADO);
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(1L, repPmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("REGISTRADO o EN_PROCESO"));
        verify(repPmsRepository, never()).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Crear repetición - debe lanzar excepción si se exceden 16 repeticiones")
    void crearRepeticion_debeLanzarExcepcionSiExcede16() {
        // ARRANGE
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(16L);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(1L, repPmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("más de 16 repeticiones"));
        verify(repPmsRepository, never()).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe retornar repetición si existe")
    void obtenerPorId_debeRetornarRepeticionSiExiste() {
        // ARRANGE
        when(repPmsRepository.findById(10L)).thenReturn(Optional.of(repPms));

        // ACT
        RepPmsDTO resultado = repPmsService.obtenerPorId(10L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(10L, resultado.getRepPMSID());
        assertEquals(new BigDecimal("45.5"), resultado.getPeso());
        verify(repPmsRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe lanzar excepción si no existe")
    void obtenerPorId_debeLanzarExcepcionSiNoExiste() {
        // ARRANGE
        when(repPmsRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.obtenerPorId(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    @Test
    @DisplayName("Actualizar repetición - debe actualizar correctamente")
    void actualizarRepeticion_debeActualizarCorrectamente() {
        // ARRANGE
        RepPmsRequestDTO updateDTO = new RepPmsRequestDTO();
        updateDTO.setNumRep(1);
        updateDTO.setPeso(new BigDecimal("47.3"));
        
        when(repPmsRepository.findById(10L)).thenReturn(Optional.of(repPms));
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(8L);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Pms.class));
        doNothing().when(pmsService).validarTodasLasRepeticiones(1L);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        // ACT
        RepPmsDTO resultado = repPmsService.actualizarRepeticion(10L, updateDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(repPmsRepository, times(2)).save(any(RepPms.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Pms.class));
    }

    @Test
    @DisplayName("Eliminar repetición - debe eliminar correctamente")
    void eliminarRepeticion_debeEliminarCorrectamente() {
        // ARRANGE
        when(repPmsRepository.findById(10L)).thenReturn(Optional.of(repPms));
        doNothing().when(repPmsRepository).deleteById(10L);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(7L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList());
        doNothing().when(pmsService).actualizarEstadisticasPms(1L);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        // ACT
        repPmsService.eliminarRepeticion(10L);

        // ASSERT
        verify(repPmsRepository, times(1)).deleteById(10L);
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Pms.class));
    }

    @Test
    @DisplayName("Eliminar repetición - debe lanzar excepción si no existe")
    void eliminarRepeticion_debeLanzarExcepcionSiNoExiste() {
        // ARRANGE
        when(repPmsRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.eliminarRepeticion(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(repPmsRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Obtener repeticiones por PMS - debe retornar lista")
    void obtenerPorPms_debeRetornarLista() {
        // ARRANGE
        RepPms rep2 = new RepPms();
        rep2.setRepPMSID(11L);
        rep2.setNumRep(2);
        rep2.setNumTanda(1);
        rep2.setPeso(new BigDecimal("46.2"));
        rep2.setPms(pms);
        
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList(repPms, rep2));

        // ACT
        List<RepPmsDTO> resultado = repPmsService.obtenerPorPms(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(10L, resultado.get(0).getRepPMSID());
        assertEquals(11L, resultado.get(1).getRepPMSID());
    }

    @Test
    @DisplayName("Contar repeticiones por PMS - debe retornar cantidad correcta")
    void contarPorPms_debeRetornarCantidad() {
        // ARRANGE
        when(repPmsRepository.countByPmsId(1L)).thenReturn(8L);

        // ACT
        Long resultado = repPmsService.contarPorPms(1L);

        // ASSERT
        assertEquals(8L, resultado);
        verify(repPmsRepository, times(1)).countByPmsId(1L);
    }
}
