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
}
