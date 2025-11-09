package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Legado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LegadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoSimpleDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de LegadoService")
class LegadoServiceTest {

    @Mock
    private LegadoRepository legadoRepository;

    @Mock
    private LoteService loteService;

    @InjectMocks
    private LegadoService legadoService;

    private Legado legado;
    private Lote lote;

    @BeforeEach
    void setUp() {
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-001");
        lote.setFicha("FICHA-001");
        lote.setFechaRecibo(LocalDate.now());

        legado = new Legado();
        legado.setLegadoID(1L);
        legado.setLote(lote);
        legado.setCodDoc("COD-001");
        legado.setNomDoc("DOC-001");
        legado.setFamilia("Gramíneas");
        legado.setGermC(95);
        legado.setGermSC(92);
        legado.setPeso1000(new BigDecimal("45.5"));
        legado.setPura(new BigDecimal("98.5"));
        legado.setPuraI(new BigDecimal("98.0"));
        legado.setActivo(true);
        legado.setArchivoOrigen("archivo.xlsx");
        legado.setFilaExcel(5);
    }

    @Test
    @DisplayName("Obtener todos los legados simples - debe retornar lista")
    void obtenerTodosSimple_debeRetornarLista() {
        when(legadoRepository.findByActivoTrue()).thenReturn(Arrays.asList(legado));

        List<LegadoSimpleDTO> resultado = legadoService.obtenerTodosSimple();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(legadoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener legado por ID - debe retornar legado completo")
    void obtenerPorId_legadoExistente_debeRetornarLegado() {
        when(legadoRepository.findById(1L)).thenReturn(Optional.of(legado));

        LegadoDTO resultado = legadoService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getLegadoID());
        assertEquals("COD-001", resultado.getCodDoc());
        verify(legadoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener legado por ID inexistente - debe lanzar excepción")
    void obtenerPorId_legadoInexistente_debeLanzarExcepcion() {
        when(legadoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            legadoService.obtenerPorId(999L);
        });

        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    @DisplayName("Obtener legados por archivo - debe retornar lista")
    void obtenerPorArchivo_debeRetornarLista() {
        when(legadoRepository.findByArchivoOrigen("archivo.xlsx")).thenReturn(Arrays.asList(legado));

        List<LegadoSimpleDTO> resultado = legadoService.obtenerPorArchivo("archivo.xlsx");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(legadoRepository, times(1)).findByArchivoOrigen("archivo.xlsx");
    }

    @Test
    @DisplayName("Obtener legados por ficha - debe retornar lista")
    void obtenerPorFicha_debeRetornarLista() {
        when(legadoRepository.findByFicha("FICHA-001")).thenReturn(Arrays.asList(legado));

        List<LegadoSimpleDTO> resultado = legadoService.obtenerPorFicha("FICHA-001");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(legadoRepository, times(1)).findByFicha("FICHA-001");
    }

    @Test
    @DisplayName("Desactivar legado - debe cambiar activo a false")
    void desactivar_debeCambiarActivoAFalse() {
        when(legadoRepository.findById(1L)).thenReturn(Optional.of(legado));
        when(legadoRepository.save(any(Legado.class))).thenReturn(legado);

        legadoService.desactivar(1L);

        verify(legadoRepository, times(1)).findById(1L);
        verify(legadoRepository, times(1)).save(any(Legado.class));
    }

    @Test
    @DisplayName("Desactivar legado inexistente - debe lanzar excepción")
    void desactivar_legadoInexistente_debeLanzarExcepcion() {
        when(legadoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            legadoService.desactivar(999L);
        });

        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(legadoRepository, never()).save(any(Legado.class));
    }

    @Test
    @DisplayName("Obtener especies únicas - debe retornar lista de especies")
    void obtenerEspeciesUnicas_debeRetornarListaEspecies() {
        when(legadoRepository.findByActivoTrue()).thenReturn(Arrays.asList(legado));

        List<String> resultado = legadoService.obtenerEspeciesUnicas();

        assertNotNull(resultado);
        verify(legadoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener legados con lista vacía - debe retornar lista vacía")
    void obtenerTodosSimple_listaVacia_debeRetornarListaVacia() {
        when(legadoRepository.findByActivoTrue()).thenReturn(Arrays.asList());

        List<LegadoSimpleDTO> resultado = legadoService.obtenerTodosSimple();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(legadoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener legado con lote nulo - debe manejar correctamente")
    void obtenerPorId_conLoteNulo_debeRetornarLegado() {
        legado.setLote(null);
        when(legadoRepository.findById(1L)).thenReturn(Optional.of(legado));

        LegadoDTO resultado = legadoService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertNull(resultado.getLote());
        verify(legadoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Convertir a DTO simple - debe mapear correctamente")
    void convertirASimpleDTO_debeMapejarCorrectamente() {
        when(legadoRepository.findByActivoTrue()).thenReturn(Arrays.asList(legado));

        List<LegadoSimpleDTO> resultado = legadoService.obtenerTodosSimple();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        LegadoSimpleDTO dto = resultado.get(0);
        assertEquals(1L, dto.getLegadoID());
        assertEquals("LOTE-001", dto.getNomLote());
        assertEquals("FICHA-001", dto.getFicha());
        assertEquals("COD-001", dto.getCodDoc());
        assertEquals("DOC-001", dto.getNomDoc());
        assertEquals("Gramíneas", dto.getFamilia());
        assertTrue(dto.getActivo());
    }
}
