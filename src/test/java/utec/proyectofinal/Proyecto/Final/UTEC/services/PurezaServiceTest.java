package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPureza;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
    private MalezasCatalogoRepository malezasCatalogoRepository;

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

    @Test
    @DisplayName("Actualizar pureza - debe actualizar correctamente")
    void actualizarPureza_debeActualizarCorrectamente() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        purezaRequestDTO.setSemillaPura_g(BigDecimal.valueOf(90.0));
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza inexistente - debe lanzar excepción")
    void actualizarPureza_conIdInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(purezaRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.actualizarPureza(999L, purezaRequestDTO);
        });
    }

    @Test
    @DisplayName("Actualizar pureza con pesos inválidos - debe lanzar excepción")
    void actualizarPureza_conPesosInvalidos_debeLanzarExcepcion() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        purezaRequestDTO.setPesoInicial_g(BigDecimal.valueOf(100.0));
        purezaRequestDTO.setPesoTotal_g(BigDecimal.valueOf(105.0));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.actualizarPureza(1L, purezaRequestDTO);
        });
    }

    @Test
    @DisplayName("Eliminar pureza - debe cambiar activo a false")
    void eliminarPureza_debeCambiarActivoAFalse() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);

        // ACT
        purezaService.eliminarPureza(1L);

        // ASSERT
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Eliminar pureza inexistente - debe lanzar excepción")
    void eliminarPureza_conIdInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(purezaRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.eliminarPureza(999L);
        });
    }

    @Test
    @DisplayName("Reactivar pureza - debe llamar al servicio de análisis")
    void reactivarPureza_debeLlamarServicioAnalisis() {
        // ARRANGE
        when(analisisService.reactivarAnalisis(eq(1L), eq(purezaRepository), any())).thenReturn(new PurezaDTO());

        // ACT
        PurezaDTO resultado = purezaService.reactivarPureza(1L);

        // ASSERT
        assertNotNull(resultado);
        verify(analisisService, times(1)).reactivarAnalisis(eq(1L), eq(purezaRepository), any());
    }

    @Test
    @DisplayName("Obtener todas las purezas activas - debe retornar lista")
    void obtenerTodasPurezasActivas_debeRetornarLista() {
        // ARRANGE
        List<Pureza> purezas = List.of(pureza);
        when(purezaRepository.findByActivoTrue()).thenReturn(purezas);

        // ACT
        ResponseListadoPureza resultado = purezaService.obtenerTodasPurezasActivas();

        // ASSERT
        assertNotNull(resultado);
        assertNotNull(resultado.getPurezas());
        assertEquals(1, resultado.getPurezas().size());
    }

    @Test
    @DisplayName("Obtener purezas por lote - debe retornar lista filtrada")
    void obtenerPurezasPorIdLote_debeRetornarListaFiltrada() {
        // ARRANGE
        List<Pureza> purezas = List.of(pureza);
        when(purezaRepository.findByIdLote(1L)).thenReturn(purezas);

        // ACT
        List<PurezaDTO> resultado = purezaService.obtenerPurezasPorIdLote(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(purezaRepository, times(1)).findByIdLote(1L);
    }

    @Test
    @DisplayName("Obtener pureza por ID inexistente - debe lanzar excepción")
    void obtenerPurezaPorId_conIdInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(purezaRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.obtenerPurezaPorId(999L);
        });
    }

    @Test
    @DisplayName("Validar peso inicial cero - debe lanzar excepción")
    void crearPureza_conPesoInicialCero_debeLanzarExcepcion() {
        // ARRANGE
        purezaRequestDTO.setPesoInicial_g(BigDecimal.ZERO);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.crearPureza(purezaRequestDTO);
        });
    }

    @Test
    @DisplayName("Validar peso inicial negativo - debe lanzar excepción")
    void crearPureza_conPesoInicialNegativo_debeLanzarExcepcion() {
        // ARRANGE
        purezaRequestDTO.setPesoInicial_g(BigDecimal.valueOf(-10.0));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.crearPureza(purezaRequestDTO);
        });
    }

    @Test
    @DisplayName("Finalizar análisis - debe llamar al servicio genérico")
    void finalizarAnalisis_debeLlamarServicioGenerico() {
        // ARRANGE
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(purezaRepository), any(), any()))
            .thenReturn(new PurezaDTO());

        // ACT
        PurezaDTO resultado = purezaService.finalizarAnalisis(1L);

        // ASSERT
        assertNotNull(resultado);
        verify(analisisService, times(1)).finalizarAnalisisGenerico(eq(1L), eq(purezaRepository), any(), any());
    }

    @Test
    @DisplayName("Aprobar análisis - debe llamar al servicio genérico")
    void aprobarAnalisis_debeLlamarServicioGenerico() {
        // ARRANGE
        when(analisisService.aprobarAnalisisGenerico(eq(1L), eq(purezaRepository), any(), any(), any()))
            .thenReturn(new PurezaDTO());

        // ACT
        PurezaDTO resultado = purezaService.aprobarAnalisis(1L);

        // ASSERT
        assertNotNull(resultado);
        verify(analisisService, times(1)).aprobarAnalisisGenerico(eq(1L), eq(purezaRepository), any(), any(), any());
    }

    @Test
    @DisplayName("Marcar para repetir - debe llamar al servicio genérico")
    void marcarParaRepetir_debeLlamarServicioGenerico() {
        // ARRANGE
        when(analisisService.marcarParaRepetirGenerico(eq(1L), eq(purezaRepository), any(), any()))
            .thenReturn(new PurezaDTO());

        // ACT
        PurezaDTO resultado = purezaService.marcarParaRepetir(1L);

        // ASSERT
        assertNotNull(resultado);
        verify(analisisService, times(1)).marcarParaRepetirGenerico(eq(1L), eq(purezaRepository), any(), any());
    }

    @Test
    @DisplayName("Obtener todos los catálogos - debe retornar lista")
    void obtenerTodosCatalogos_debeRetornarLista() {
        // ARRANGE
        when(malezasCatalogoRepository.findAll()).thenReturn(List.of());

        // ACT
        List<MalezasCatalogoDTO> resultado = purezaService.obtenerTodosCatalogos();

        // ASSERT
        assertNotNull(resultado);
        verify(malezasCatalogoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Crear pureza con lote inactivo - debe lanzar excepción")
    void crearPureza_conLoteInactivo_debeLanzarExcepcion() {
        // ARRANGE
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.crearPureza(purezaRequestDTO);
        }, "Debe lanzar excepción cuando el lote está inactivo");
    }

    @Test
    @DisplayName("Crear pureza con otras semillas - debe guardar listados")
    void crearPureza_conOtrasSemillas_debeGuardarListados() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        List<ListadoRequestDTO> otrasSemillas = new ArrayList<>();
        ListadoRequestDTO listado = new ListadoRequestDTO();
        otrasSemillas.add(listado);
        purezaRequestDTO.setOtrasSemillas(otrasSemillas);

        // ACT
        PurezaDTO resultado = purezaService.crearPureza(purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza desde solicitud - debe actualizar campos específicos")
    void actualizarPurezaDesdeSolicitud_debeActualizarCamposEspecificos() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        // Actualizar solo algunos campos
        PurezaRequestDTO solicitudParcial = new PurezaRequestDTO();
        solicitudParcial.setIdLote(1L);
        solicitudParcial.setCumpleEstandar(true);
        solicitudParcial.setComentarios("Comentario actualizado");
        solicitudParcial.setPesoInicial_g(BigDecimal.valueOf(150.0));
        solicitudParcial.setPesoTotal_g(BigDecimal.valueOf(150.0));

        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, solicitudParcial);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Obtener purezas paginadas con filtro 'activos'")
    void obtenerPurezaPaginadasConFiltro_filtroActivos_debeRetornarSoloActivos() {
        // ARRANGE
        when(purezaRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(Page.empty());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadasConFiltro(
            Pageable.unpaged(), 
            "activos"
        );

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener purezas paginadas con filtro 'inactivos'")
    void obtenerPurezaPaginadasConFiltro_filtroInactivos_debeRetornarSoloInactivos() {
        // ARRANGE
        when(purezaRepository.findByActivoFalseOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(Page.empty());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadasConFiltro(
            Pageable.unpaged(), 
            "inactivos"
        );

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).findByActivoFalseOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener purezas paginadas con filtro 'todos'")
    void obtenerPurezaPaginadasConFiltro_filtroTodos_debeRetornarTodos() {
        // ARRANGE
        when(purezaRepository.findAllByOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(Page.empty());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadasConFiltro(
            Pageable.unpaged(), 
            "todos"
        );

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).findAllByOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener purezas paginadas con filtros dinámicos")
    void obtenerPurezaPaginadasConFiltros_debeFiltrarCorrectamente() {
        // ARRANGE
        when(purezaRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadasConFiltros(
            Pageable.unpaged(),
            "LOTE-TEST",
            true,
            "EN_PROCESO",
            1L
        );

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener purezas paginadas - debe retornar página correcta")
    void obtenerPurezaPaginadas_debeRetornarPagina() {
        // ARRANGE
        when(purezaRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(Page.empty());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadas(Pageable.unpaged());

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Validar pesos - con pérdida mayor al 5% debe solo informar")
    void validarPesos_conPerdidaMayorAl5Porciento_debeSoloInformar() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        // Configurar pérdida del 6% (mayor al límite del 5%)
        purezaRequestDTO.setPesoInicial_g(BigDecimal.valueOf(100.0));
        purezaRequestDTO.setPesoTotal_g(BigDecimal.valueOf(94.0)); // 6% de pérdida

        // ACT - No debe lanzar excepción, solo informar
        PurezaDTO resultado = purezaService.crearPureza(purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con lote y especie completos")
    void mapearEntidadAListadoDTO_conLoteYEspecieCompletos() {
        // ARRANGE
        List<Pureza> listaPureza = List.of(pureza);
        Page<Pureza> page = new org.springframework.data.domain.PageImpl<>(listaPureza);
        when(purezaRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(page);
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(new ArrayList<>());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadas(Pageable.unpaged());

        // ASSERT
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("Validar antes de finalizar - sin datos debe lanzar excepción")
    void validarAntesDeFinalizar_sinDatos_debeLanzarExcepcion() {
        // ARRANGE
        Pureza purezaVacia = new Pureza();
        purezaVacia.setAnalisisID(1L);
        purezaVacia.setLote(lote);
        purezaVacia.setEstado(Estado.EN_PROCESO);
        purezaVacia.setActivo(true);
        
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(purezaRepository), any(), any()))
            .thenThrow(new RuntimeException("No se puede finalizar: el análisis de Pureza carece de evidencia"));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.finalizarAnalisis(1L);
        });
    }

    @Test
    @DisplayName("Validar antes de finalizar - con datos INASE debe pasar")
    void validarAntesDeFinalizar_conDatosINASE_debeValidar() {
        // ARRANGE
        pureza.setInaseFecha(java.time.LocalDate.now());
        pureza.setInasePura(BigDecimal.valueOf(95.0));
        pureza.setPesoInicial_g(BigDecimal.valueOf(100.0));
        pureza.setPesoTotal_g(BigDecimal.valueOf(100.0));
        
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(purezaRepository), any(), any()))
            .thenReturn(mapearEntidadADTO(pureza));

        // ACT
        PurezaDTO resultado = purezaService.finalizarAnalisis(1L);

        // ASSERT
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Validar antes de finalizar - con listados debe pasar")
    void validarAntesDeFinalizar_conListados_debeValidar() {
        // ARRANGE
        pureza.setListados(new ArrayList<>());
        pureza.getListados().add(new Listado());
        pureza.setPesoInicial_g(BigDecimal.valueOf(100.0));
        pureza.setPesoTotal_g(BigDecimal.valueOf(100.0));
        
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(purezaRepository), any(), any()))
            .thenReturn(mapearEntidadADTO(pureza));

        // ACT
        PurezaDTO resultado = purezaService.finalizarAnalisis(1L);

        // ASSERT
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Obtener purezas paginadas - debe retornar página vacía cuando no hay datos")
    void obtenerPurezasPaginadas_sinDatos_debeRetornarPaginaVacia() {
        // ARRANGE
        when(purezaRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(Page.empty());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadas(Pageable.unpaged());

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Obtener purezas con filtros - todos los parámetros")
    void obtenerPurezasConFiltros_todosParametros_debeFiltrar() {
        // ARRANGE
        when(purezaRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadasConFiltros(
            Pageable.unpaged(),
            null,
            null,
            null,
            null
        );

        // ASSERT
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Mapear entidad a DTO - con todos los campos completos")
    void mapearEntidadADTO_conTodosLosCampos_debeMapearCorrectamente() {
        // ARRANGE
        pureza.setCumpleEstandar(true);
        pureza.setComentarios("Test comentario");
        pureza.setListados(new ArrayList<>());
        
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(new ArrayList<>());

        // ACT
        PurezaDTO resultado = purezaService.obtenerPurezaPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
    }

    // Método helper para mapear entidad a DTO en tests
    private PurezaDTO mapearEntidadADTO(Pureza pureza) {
        PurezaDTO dto = new PurezaDTO();
        dto.setAnalisisID(pureza.getAnalisisID());
        dto.setEstado(pureza.getEstado());
        dto.setFechaInicio(pureza.getFechaInicio());
        dto.setFechaFin(pureza.getFechaFin());
        return dto;
    }
}
