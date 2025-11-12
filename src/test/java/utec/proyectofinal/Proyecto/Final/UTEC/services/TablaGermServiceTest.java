package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ValoresGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @Mock
    private ValoresGermRepository valoresGermRepository;

    @Mock
    private AnalisisService analisisService;

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
        tablaGerm.setFechaConteos(Arrays.asList(LocalDate.of(2024, 1, 18), LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 25)));
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
        requestDTO.setFechaConteos(Arrays.asList(LocalDate.of(2024, 1, 18), LocalDate.of(2024, 1, 22), LocalDate.of(2024, 1, 25)));
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
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

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
        when(valoresGermRepository.findByTablaGermId(15L)).thenReturn(Arrays.asList());
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
        // Crear repeticiones mockeadas con conteos completos
        List<RepGerm> repeticiones = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            RepGerm rep = new RepGerm();
            rep.setNormales(Arrays.asList(10, 15, 20)); // 3 conteos
            rep.setAnormales(10); // Total de anormales
            rep.setDuras(0);
            rep.setFrescas(0);
            rep.setMuertas(5);
            rep.setTablaGerm(tablaGerm); // Establecer la relación con la tabla
            repeticiones.add(rep);
        }
        tablaGerm.setRepGerm(repeticiones);
        
        // Configurar porcentajes para que estén completos
        tablaGerm.setPorcentajeNormalesConRedondeo(new BigDecimal("85.0"));
        tablaGerm.setPorcentajeAnormalesConRedondeo(new BigDecimal("10.0"));
        tablaGerm.setPorcentajeDurasConRedondeo(new BigDecimal("0.0"));
        tablaGerm.setPorcentajeFrescasConRedondeo(new BigDecimal("0.0"));
        tablaGerm.setPorcentajeMuertasConRedondeo(new BigDecimal("5.0"));
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
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
    @DisplayName("Puede ingresar porcentajes - debe retornar false si tabla no finalizada")
    void puedeIngresarPorcentajes_debeRetornarTrueSiTablaFinalizada() {
        // ARRANGE
        tablaGerm.setFinalizada(false); // La tabla en setUp no está finalizada
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));

        // ACT
        Boolean resultado = tablaGermService.puedeIngresarPorcentajes(15L);

        // ASSERT
        assertFalse(resultado); // Debe retornar false porque no está finalizada
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

        // Crear repeticiones mockeadas con conteos completos
        List<RepGerm> repeticiones = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            RepGerm rep = new RepGerm();
            rep.setNormales(Arrays.asList(10, 15, 20)); // 3 conteos
            rep.setAnormales(10); // Total de anormales
            rep.setDuras(0);
            rep.setFrescas(0);
            rep.setMuertas(5);
            rep.setTablaGerm(tablaGerm); // Establecer la relación con la tabla
            repeticiones.add(rep);
        }
        tablaGerm.setRepGerm(repeticiones);
        tablaGerm.setFinalizada(true);
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        TablaGermDTO resultado = tablaGermService.actualizarPorcentajes(15L, porcentajesDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Validar fechas - debe lanzar excepción si fecha germinación es anterior a fecha ingreso")
    void crearTablaGerm_debeValidarFechas() {
        // ARRANGE
        requestDTO.setFechaIngreso(LocalDate.of(2024, 1, 20));
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 15));
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT & ASSERT
        // El servicio debe validar que fecha germinación sea posterior a fecha ingreso
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.crearTablaGerm(1L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("fecha de germinación") || 
                   exception.getMessage().contains("fecha de ingreso"));
    }

    // ========== TESTS PARA MÉTODOS PRIVADOS PROBADOS INDIRECTAMENTE ==========

    @Test
    @DisplayName("reiniciarDatosConteo - debe reiniciar datos cuando fecha de conteo cambia a futuro")
    void reiniciarDatosConteo_debeReiniciarCuandoCambiaFechaConteo() {
        // ARRANGE
        List<RepGerm> repeticiones = new ArrayList<>();
        RepGerm rep1 = new RepGerm();
        rep1.setRepGermID(1L);
        rep1.setNumRep(1);
        rep1.setNormales(Arrays.asList(10, 15, 20));
        rep1.setTablaGerm(tablaGerm);
        repeticiones.add(rep1);
        
        tablaGerm.setRepGerm(repeticiones);
        tablaGerm.setFechaGerminacion(LocalDate.of(2024, 1, 17));
        tablaGerm.setFechaUltConteo(LocalDate.of(2024, 1, 25));
        
        // Cambiar fecha del segundo conteo a una entre germinación y último conteo
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 17));
        requestDTO.getFechaConteos().set(1, LocalDate.of(2024, 1, 23));
        requestDTO.setFechaUltConteo(LocalDate.of(2024, 1, 25));
        requestDTO.setFechaFinal(LocalDate.of(2024, 1, 26));
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep1);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(repGermRepository, atLeastOnce()).save(any(RepGerm.class));
    }

    @Test
    @DisplayName("validarFechasConteosEnEdicion - debe validar primer conteo posterior a germinación")
    void validarFechasConteosEnEdicion_debeValidarPrimerConteo() {
        // ARRANGE
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 17));
        requestDTO.getFechaConteos().set(0, LocalDate.of(2024, 1, 17)); // Igual a germinación
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.actualizarTablaGerm(15L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("primer conteo") || 
                   exception.getMessage().contains("debe ser posterior"));
    }

    @Test
    @DisplayName("mapearValoresGermADTO - debe mapear correctamente al actualizar valores")
    void mapearValoresGermADTO_debeMapeaCorrectamente() {
        // ARRANGE - Este método se usa internamente en obtenerTablaGermPorId
        // Crear ValoresGerm para probar el mapeo
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm valoresInia = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm();
        valoresInia.setValoresGermID(1L);
        valoresInia.setInstituto(Instituto.INIA);
        valoresInia.setNormales(new BigDecimal("85.0"));
        valoresInia.setAnormales(new BigDecimal("10.0"));
        valoresInia.setDuras(new BigDecimal("2.0"));
        valoresInia.setFrescas(new BigDecimal("1.0"));
        valoresInia.setMuertas(new BigDecimal("2.0"));
        valoresInia.setGerminacion(new BigDecimal("85.0"));
        valoresInia.setTablaGerm(tablaGerm);
        
        tablaGerm.setValoresGerm(Arrays.asList(valoresInia));
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));

        // ACT
        TablaGermDTO resultado = tablaGermService.obtenerTablaGermPorId(15L);

        // ASSERT
        assertNotNull(resultado.getValoresGerm());
        assertEquals(1, resultado.getValoresGerm().size());
        assertEquals(Instituto.INIA, resultado.getValoresGerm().get(0).getInstituto());
        assertEquals(new BigDecimal("85.0"), resultado.getValoresGerm().get(0).getNormales());
    }

    @Test
    @DisplayName("reiniciarCamposUltimoConteo - debe reiniciar cuando fecha último conteo cambia a futuro")
    void reiniciarCamposUltimoConteo_debeReiniciarCuandoFechaCambiaAFuturo() {
        // ARRANGE
        List<RepGerm> repeticiones = new ArrayList<>();
        RepGerm rep1 = new RepGerm();
        rep1.setRepGermID(1L);
        rep1.setNumRep(1);
        rep1.setNormales(Arrays.asList(10, 15, 20));
        rep1.setAnormales(10);
        rep1.setDuras(2);
        rep1.setFrescas(1);
        rep1.setMuertas(3);
        rep1.setTablaGerm(tablaGerm);
        repeticiones.add(rep1);
        
        tablaGerm.setRepGerm(repeticiones);
        tablaGerm.setFechaUltConteo(LocalDate.now().minusDays(1)); // Fecha pasada
        tablaGerm.setFechaFinal(LocalDate.now().plusDays(10)); // Asegurar que fecha final es posterior
        
        // Cambiar fecha a futuro
        requestDTO.setFechaUltConteo(LocalDate.now().plusDays(5));
        requestDTO.setFechaFinal(LocalDate.now().plusDays(10)); // Asegurar que fecha final es posterior
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        when(repGermRepository.findByTablaGermId(15L)).thenReturn(repeticiones);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep1);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(repGermRepository, atLeastOnce()).save(argThat(rep -> 
            rep.getAnormales() == 0 && rep.getDuras() == 0 && 
            rep.getFrescas() == 0 && rep.getMuertas() == 0
        ));
    }

    @Test
    @DisplayName("actualizarValoresInia - debe actualizar valores cuando se finalizan porcentajes")
    void actualizarValoresInia_debeActualizarCuandoSeFinalizanPorcentajes() {
        // ARRANGE
        PorcentajesRedondeoRequestDTO porcentajesDTO = new PorcentajesRedondeoRequestDTO();
        porcentajesDTO.setPorcentajeNormalesConRedondeo(new BigDecimal("85.0"));
        porcentajesDTO.setPorcentajeAnormalesConRedondeo(new BigDecimal("8.0"));
        porcentajesDTO.setPorcentajeDurasConRedondeo(new BigDecimal("3.0"));
        porcentajesDTO.setPorcentajeFrescasConRedondeo(new BigDecimal("2.0"));
        porcentajesDTO.setPorcentajeMuertasConRedondeo(new BigDecimal("2.0"));

        List<RepGerm> repeticiones = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            RepGerm rep = new RepGerm();
            rep.setNormales(Arrays.asList(10, 15, 20));
            rep.setAnormales(10);
            rep.setDuras(0);
            rep.setFrescas(0);
            rep.setMuertas(5);
            rep.setTablaGerm(tablaGerm);
            repeticiones.add(rep);
        }
        tablaGerm.setRepGerm(repeticiones);
        tablaGerm.setFinalizada(true);
        
        // Crear ValoresGerm existente
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm valoresInia = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm();
        valoresInia.setInstituto(Instituto.INIA);
        valoresInia.setTablaGerm(tablaGerm);
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        when(valoresGermRepository.findByTablaGermIdAndInstituto(15L, Instituto.INIA))
            .thenReturn(Optional.of(valoresInia));
        when(valoresGermRepository.save(any())).thenReturn(valoresInia);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarPorcentajes(15L, porcentajesDTO);

        // ASSERT
        verify(valoresGermRepository).save(argThat(valores -> 
            valores.getInstituto() == Instituto.INIA &&
            valores.getNormales().compareTo(new BigDecimal("85.0")) == 0 &&
            valores.getGerminacion().compareTo(new BigDecimal("85.0")) == 0
        ));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - debe actualizar prefrío cuando está activo")
    void actualizarEntidadDesdeSolicitud_debeActualizarPrefrio() {
        // ARRANGE
        requestDTO.setTienePrefrio(true);
        requestDTO.setDiasPrefrio(7);
        requestDTO.setDescripcionPrefrio("7 días a 4°C");
        requestDTO.setFechaIngreso(LocalDate.of(2024, 1, 10));
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 20)); // 10 días después (suficiente)
        requestDTO.setFechaConteos(Arrays.asList(
            LocalDate.of(2024, 1, 22), // Después de germinación
            LocalDate.of(2024, 1, 23),
            LocalDate.of(2024, 1, 24)
        ));
        requestDTO.setFechaUltConteo(LocalDate.of(2024, 1, 25));
        requestDTO.setFechaFinal(LocalDate.of(2024, 1, 26));
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(tablaGermRepository).save(argThat(tabla -> 
            tabla.getTienePrefrio() && 
            tabla.getDiasPrefrio() == 7 &&
            "7 días a 4°C".equals(tabla.getDescripcionPrefrio())
        ));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - debe limpiar prefrío cuando está inactivo")
    void actualizarEntidadDesdeSolicitud_debeLimpiarPrefrioInactivo() {
        // ARRANGE
        tablaGerm.setTienePrefrio(true);
        tablaGerm.setDiasPrefrio(7);
        tablaGerm.setDescripcionPrefrio("7 días a 4°C");
        
        requestDTO.setTienePrefrio(false);
        requestDTO.setDiasPrefrio(null);
        requestDTO.setDescripcionPrefrio(null);
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(tablaGermRepository).save(argThat(tabla -> 
            !tabla.getTienePrefrio() && 
            tabla.getDiasPrefrio() == 0 &&
            tabla.getDescripcionPrefrio() == null
        ));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - debe actualizar pretratamiento cuando está activo")
    void actualizarEntidadDesdeSolicitud_debeActualizarPretratamiento() {
        // ARRANGE
        requestDTO.setTienePretratamiento(true);
        requestDTO.setDescripcionPretratamiento("Escarificación química");
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(tablaGermRepository).save(argThat(tabla -> 
            tabla.getTienePretratamiento() &&
            "Escarificación química".equals(tabla.getDescripcionPretratamiento())
        ));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - debe aumentar número de conteos")
    void actualizarEntidadDesdeSolicitud_debeAumentarNumeroConteos() {
        // ARRANGE
        List<RepGerm> repeticiones = new ArrayList<>();
        RepGerm rep1 = new RepGerm();
        rep1.setRepGermID(1L);
        rep1.setNumRep(1);
        rep1.setNormales(new ArrayList<>(Arrays.asList(10, 15, 20))); // 3 conteos
        rep1.setTablaGerm(tablaGerm);
        repeticiones.add(rep1);
        
        tablaGerm.setRepGerm(repeticiones);
        tablaGerm.setNumeroConteos(3);
        
        requestDTO.setNumeroConteos(5); // Aumentar a 5 conteos
        requestDTO.setFechaConteos(Arrays.asList(
            LocalDate.of(2024, 1, 18), 
            LocalDate.of(2024, 1, 20),
            LocalDate.of(2024, 1, 22), 
            LocalDate.of(2024, 1, 24),
            LocalDate.of(2024, 1, 25)
        ));
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep1);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(repGermRepository, atLeastOnce()).save(argThat(rep -> 
            rep.getNormales() != null && rep.getNormales().size() == 5
        ));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - debe disminuir número de conteos")
    void actualizarEntidadDesdeSolicitud_debeDisminuirNumeroConteos() {
        // ARRANGE
        List<RepGerm> repeticiones = new ArrayList<>();
        RepGerm rep1 = new RepGerm();
        rep1.setRepGermID(1L);
        rep1.setNumRep(1);
        rep1.setNormales(new ArrayList<>(Arrays.asList(10, 15, 20, 25, 30))); // 5 conteos
        rep1.setTablaGerm(tablaGerm);
        repeticiones.add(rep1);
        
        tablaGerm.setRepGerm(repeticiones);
        tablaGerm.setNumeroConteos(5);
        
        requestDTO.setNumeroConteos(3); // Reducir a 3 conteos
        requestDTO.setFechaConteos(Arrays.asList(
            LocalDate.of(2024, 1, 18), 
            LocalDate.of(2024, 1, 22), 
            LocalDate.of(2024, 1, 25)
        ));
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep1);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(repGermRepository, atLeastOnce()).save(argThat(rep -> 
            rep.getNormales() != null && rep.getNormales().size() == 3
        ));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - debe disminuir número de repeticiones")
    void actualizarEntidadDesdeSolicitud_debeDisminuirNumeroRepeticiones() {
        // ARRANGE
        List<RepGerm> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            RepGerm rep = new RepGerm();
            rep.setRepGermID((long) i);
            rep.setNumRep(i);
            rep.setNormales(Arrays.asList(10, 15, 20));
            rep.setTablaGerm(tablaGerm);
            repeticiones.add(rep);
        }
        
        tablaGerm.setRepGerm(repeticiones);
        tablaGerm.setNumeroRepeticiones(8);
        
        requestDTO.setNumeroRepeticiones(4); // Reducir a 4 repeticiones
        
        when(tablaGermRepository.findById(15L)).thenReturn(Optional.of(tablaGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        when(repGermRepository.findByTablaGermId(15L)).thenReturn(repeticiones);
        doNothing().when(repGermRepository).delete(any(RepGerm.class));
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any());

        // ACT
        tablaGermService.actualizarTablaGerm(15L, requestDTO);

        // ASSERT
        verify(repGermRepository, times(4)).delete(any(RepGerm.class));
    }

    @Test
    @DisplayName("validarDatosTablaGerm - debe validar días de prefrío suficientes")
    void validarDatosTablaGerm_debeValidarDiasPrefrio() {
        // ARRANGE
        requestDTO.setTienePrefrio(true);
        requestDTO.setDiasPrefrio(10);
        requestDTO.setFechaIngreso(LocalDate.of(2024, 1, 10));
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 15)); // Solo 5 días, insuficientes
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.crearTablaGerm(1L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("días de prefrío"));
    }

    @Test
    @DisplayName("validarDatosTablaGerm - debe validar fechaFinal posterior a fechaGerminacion")
    void validarDatosTablaGerm_debeValidarFechaFinalPosteriorAGerminacion() {
        // ARRANGE
        requestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 20));
        requestDTO.setFechaFinal(LocalDate.of(2024, 1, 15)); // Anterior a germinación
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.crearTablaGerm(1L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("fecha final"));
    }

    @Test
    @DisplayName("validarDatosTablaGerm - debe validar número de repeticiones en rango 1-20")
    void validarDatosTablaGerm_debeValidarRangoRepeticiones() {
        // ARRANGE
        requestDTO.setNumeroRepeticiones(25); // Fuera del rango
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.crearTablaGerm(1L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("repeticiones") && 
                   exception.getMessage().contains("20"));
    }

    @Test
    @DisplayName("validarDatosTablaGerm - debe validar número de conteos en rango 1-15")
    void validarDatosTablaGerm_debeValidarRangoConteos() {
        // ARRANGE
        requestDTO.setNumeroConteos(20); // Fuera del rango
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.crearTablaGerm(1L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("conteos") && 
                   exception.getMessage().contains("15"));
    }

    @Test
    @DisplayName("validarDatosTablaGerm - debe validar orden cronológico de fechas de conteos")
    void validarDatosTablaGerm_debeValidarOrdenCronologicoConteos() {
        // ARRANGE
        requestDTO.setFechaConteos(Arrays.asList(
            LocalDate.of(2024, 1, 25), // Posterior
            LocalDate.of(2024, 1, 18), // Anterior - orden incorrecto
            LocalDate.of(2024, 1, 22)
        ));
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tablaGermService.crearTablaGerm(1L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("posterior"));
    }
}
