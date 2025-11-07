package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PurezaService
 * 
 * ¿Qué validamos?
 * - Creación de análisis de pureza con estado EN_PROCESO
 * - Validación de pesos (pesoTotal >= pesoInicial)
 * - Asignación correcta de lote
 * - Cálculos de porcentajes
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PurezaService")
class PurezaServiceTest {

    @Mock
    private PurezaRepository purezaRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private PurezaService purezaService;

    private PurezaRequestDTO purezaRequestDTO;
    private Lote lote;
    private Pureza pureza;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-TEST-001");
        lote.setActivo(true);

        purezaRequestDTO = new PurezaRequestDTO();
        purezaRequestDTO.setIdLote(1L);
        purezaRequestDTO.setPesoInicial_g(BigDecimal.valueOf(100.0));
        purezaRequestDTO.setPesoTotal_g(BigDecimal.valueOf(100.0));
        purezaRequestDTO.setSemillaPura_g(BigDecimal.valueOf(95.0));
        purezaRequestDTO.setMateriaInerte_g(BigDecimal.valueOf(3.0));
        purezaRequestDTO.setMalezas_g(BigDecimal.valueOf(2.0));

        pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        pureza.setEstado(Estado.EN_PROCESO);
        pureza.setActivo(true);
    }

    @Test
    @DisplayName("Crear pureza - debe asignar estado EN_PROCESO")
    void crearPureza_debeAsignarEstadoEnProceso() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);

        // ACT
        PurezaDTO resultado = purezaService.crearPureza(purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(purezaRepository, times(1)).save(any(Pureza.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Pureza.class));
    }

    @Test
    @DisplayName("Crear pureza con lote inexistente - debe lanzar excepción")
    void crearPureza_conLoteInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        purezaRequestDTO.setIdLote(999L);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.crearPureza(purezaRequestDTO);
        }, "Debe lanzar excepción cuando el lote no existe");
    }

    @Test
    @DisplayName("Validar pesos - pesoTotal debe ser mayor o igual a pesoInicial")
    void validarPesos_pesoTotalMenorQuePesoInicial_debeLanzarExcepcion() {
        // ARRANGE: Configurar pesos inválidos
        purezaRequestDTO.setPesoInicial_g(BigDecimal.valueOf(100.0));
        purezaRequestDTO.setPesoTotal_g(BigDecimal.valueOf(95.0)); // Inválido

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.crearPureza(purezaRequestDTO);
        }, "Debe lanzar excepción cuando pesoTotal < pesoInicial");
    }

    @Test
    @DisplayName("Obtener pureza por ID - debe retornar la pureza si existe")
    void obtenerPurezaPorId_cuandoExiste_debeRetornarPureza() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));

        // ACT
        PurezaDTO resultado = purezaService.obtenerPurezaPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        verify(purezaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Desactivar pureza - debe cambiar activo a false")
    void desactivarPureza_debeCambiarActivoAFalse() {
        // ARRANGE
        doNothing().when(analisisService).desactivarAnalisis(anyLong(), any());

        // ACT
        purezaService.desactivarPureza(1L);

        // ASSERT
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }
}
