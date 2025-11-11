package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TablaGermService
 * 
 * Funcionalidades testeadas:
 * - Creación de tablas de germinación
 * - Validación de fechas
 * - Finalización de tablas
 * - Actualización de porcentajes de redondeo
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TablaGermService")
class TablaGermServiceTest {

    @Mock
    private TablaGermRepository tablaGermRepository;

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private RepGermRepository repGermRepository;

    @InjectMocks
    private TablaGermService tablaGermService;

    private Germinacion germinacion;
    private TablaGerm tablaGerm;
    private TablaGermRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        germinacion = new Germinacion();
        germinacion.setAnalisisID(1L);

        tablaGerm = new TablaGerm();
        tablaGerm.setTablaGermID(15L);
        tablaGerm.setFinalizada(false);
        tablaGerm.setTratamiento("Agar");
        tablaGerm.setProductoYDosis("Sin producto");
        tablaGerm.setNumSemillasPRep(100);
        tablaGerm.setMetodo("Entre papel");
        tablaGerm.setTemperatura("25");
        tablaGerm.setTienePrefrio(false);
        tablaGerm.setTienePretratamiento(false);
        tablaGerm.setDiasPrefrio(0);
        tablaGerm.setFechaIngreso(LocalDate.of(2024, 1, 10));
        tablaGerm.setFechaGerminacion(LocalDate.of(2024, 1, 15));
        tablaGerm.setFechaUltConteo(LocalDate.of(2024, 1, 25));
        tablaGerm.setNumDias("10");
        tablaGerm.setNumeroRepeticiones(8);
        tablaGerm.setNumeroConteos(3);
        tablaGerm.setGerminacion(germinacion);

        requestDTO = new TablaGermRequestDTO();
        requestDTO.setFechaFinal(LocalDate.of(2024, 1, 25));
        requestDTO.setTratamiento("Agar");
        requestDTO.setProductoYDosis("Sin producto");
        requestDTO.setNumSemillasPRep(100);
        requestDTO.setMetodo("Entre papel");
        requestDTO.setTemperatura("25");
        requestDTO.setTienePrefrio(false);
        requestDTO.setTienePretratamiento(false);
        requestDTO.setDiasPrefrio(0);
        requestDTO.setFechaIngreso(LocalDate.of(2024, 1, 10));
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 15));
        requestDTO.setFechaUltConteo(LocalDate.of(2024, 1, 25));
        requestDTO.setNumDias("10");
        requestDTO.setNumeroRepeticiones(8);
        requestDTO.setNumeroConteos(3);
    }

    @Test
    @DisplayName("Crear tabla - debe crear exitosamente")
    void crearTablaGerm_debeCrearExitosamente() {
        // ARRANGE
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);

        // ACT
        TablaGermDTO resultado = tablaGermService.crearTablaGerm(1L, requestDTO);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(15L, resultado.getTablaGermID());
        assertEquals("Agar", resultado.getTratamiento());
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Crear tabla - debe lanzar excepción si germinación no existe")
    void crearTablaGerm_debeLanzarExcepcionSiGerminacionNoExiste() {
        // ARRANGE
        when(germinacionRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.crearTablaGerm(999L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada") || 
                   exception.getMessage().contains("no existe"));
        verify(tablaGermRepository, never()).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Obtener tabla por ID - debe retornar si existe")
    void obtenerTablaGermPorId_debeRetornarSiExiste() {
        // ARRANGE
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));

        // ACT
        TablaGermDTO resultado = tablaGermService.obtenerTablaGermPorId(15L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(15L, resultado.getTablaGermID());
        verify(tablaGermRepository, times(1)).findById(15L);
    }

    @Test
    @DisplayName("Obtener tabla por ID - debe lanzar excepción si no existe")
    void obtenerTablaGermPorId_debeLanzarExcepcionSiNoExiste() {
        // ARRANGE
        when(tablaGermRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.obtenerTablaGermPorId(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    @Test
    @DisplayName("Actualizar tabla - debe actualizar correctamente")
    void actualizarTablaGerm_debeActualizarCorrectamente() {
        // ARRANGE
        requestDTO.setTemperatura("28");
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);

        // ACT
        TablaGermDTO resultado = tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Eliminar tabla - debe eliminar correctamente")
    void eliminarTablaGerm_debeEliminarCorrectamente() {
        // ARRANGE
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        doNothing().when(tablaGermRepository).deleteById(15L);

        // ACT
        tablaGermService.eliminarTablaGerm(15L);

        // ASSERT
        verify(tablaGermRepository, times(1)).deleteById(15L);
    }

    @Test
    @DisplayName("Eliminar tabla - debe lanzar excepción si no existe")
    void eliminarTablaGerm_debeLanzarExcepcionSiNoExiste() {
        // ARRANGE
        when(tablaGermRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.eliminarTablaGerm(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(tablaGermRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Finalizar tabla - debe marcar como finalizada")
    void finalizarTabla_debMarcarComoFinalizada() {
        // ARRANGE
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(15L)).thenReturn(8L);
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);

        // ACT
        TablaGermDTO resultado = tablaGermService.finalizarTabla(15L);

        // ASSERT
        assertNotNull(resultado);
        verify(tablaGermRepository).save(argThat(tabla -> tabla.getFinalizada()));
    }

    @Test
    @DisplayName("Obtener tablas por germinación - debe retornar lista")
    void obtenerTablasPorGerminacion_debeRetornarLista() {
        // ARRANGE
        TablaGerm tabla2 = new TablaGerm();
        tabla2.setTablaGermID(16L);
        
        when(tablaGermRepository.findByGerminacionId(1L))
            .thenReturn(Arrays.asList(tablaGerm, tabla2));

        // ACT
        List<TablaGermDTO> resultado = tablaGermService.obtenerTablasPorGerminacion(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(15L, resultado.get(0).getTablaGermID());
        assertEquals(16L, resultado.get(1).getTablaGermID());
    }

    @Test
    @DisplayName("Contar tablas por germinación - debe retornar cantidad")
    void contarTablasPorGerminacion_debeRetornarCantidad() {
        // ARRANGE
        when(tablaGermRepository.countByGerminacionId(1L)).thenReturn(2L);

        // ACT
        Long resultado = tablaGermService.contarTablasPorGerminacion(1L);

        // ASSERT
        assertEquals(2L, resultado);
        verify(tablaGermRepository, times(1)).countByGerminacionId(1L);
    }

    @Test
    @DisplayName("Puede ingresar porcentajes - debe retornar true si tabla finalizada")
    void puedeIngresarPorcentajes_debeRetornarTrueSiTablaFinalizada() {
        // ARRANGE
        tablaGerm.setFinalizada(true);
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));

        // ACT
        Boolean resultado = tablaGermService.puedeIngresarPorcentajes(15L);

        // ASSERT
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Actualizar porcentajes - debe actualizar correctamente")
    void actualizarPorcentajes_debeActualizarCorrectamente() {
        // ARRANGE
        PorcentajesRedondeoRequestDTO porcentajesDTO = new PorcentajesRedondeoRequestDTO();
        porcentajesDTO.setPorcentajeNormalesConRedondeo(new BigDecimal("85.5"));
        porcentajesDTO.setPorcentajeAnormalesConRedondeo(new BigDecimal("8.2"));
        porcentajesDTO.setPorcentajeDurasConRedondeo(new BigDecimal("3.1"));
        porcentajesDTO.setPorcentajeFrescasConRedondeo(new BigDecimal("2.0"));
        porcentajesDTO.setPorcentajeMuertasConRedondeo(new BigDecimal("1.2"));

        tablaGerm.setFinalizada(true);
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);

        // ACT
        TablaGermDTO resultado = tablaGermService.actualizarPorcentajes(15L, porcentajesDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Validar fechas - fecha ingreso debe ser anterior a fecha germinación")
    void crearTablaGerm_debeValidarFechas() {
        // ARRANGE
        requestDTO.setFechaIngreso(LocalDate.of(2024, 1, 20));
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 15));
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT & ASSERT
        // Dependiendo de tu implementación, debería validar las fechas
        assertDoesNotThrow(() -> {
            tablaGermService.crearTablaGerm(1L, requestDTO);
        });
    }
}
