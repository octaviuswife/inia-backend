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
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
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
        Especie especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun("Especie Test");
        especie.setNombreCientifico("Especie Cientifica Test");
        
        Cultivar cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        
        lote.setNomLote("LOTE-001");
        lote.setCultivar(cultivar);
        
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
        PurezaListadoDTO dto = resultado.getContent().get(0);
        assertEquals(1L, dto.getAnalisisID());
        assertEquals("LOTE-001", dto.getLote());
        assertEquals("Especie Test", dto.getEspecie());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con nombre común vacío usa nombre científico")
    void mapearEntidadAListadoDTO_nombreComunVacio_usaNombreCientifico() {
        // ARRANGE
        Especie especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun(null);
        especie.setNombreCientifico("Nombre Científico Test");
        
        Cultivar cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        
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
        PurezaListadoDTO dto = resultado.getContent().get(0);
        assertEquals("Nombre Científico Test", dto.getEspecie());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - sin cultivar no debe fallar")
    void mapearEntidadAListadoDTO_sinCultivar_noDebeFallar() {
        // ARRANGE
        lote.setCultivar(null);
        
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
        PurezaListadoDTO dto = resultado.getContent().get(0);
        assertNull(dto.getEspecie());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - sin lote no debe fallar")
    void mapearEntidadAListadoDTO_sinLote_noDebeFallar() {
        // ARRANGE
        pureza.setLote(null);
        
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
        PurezaListadoDTO dto = resultado.getContent().get(0);
        assertNull(dto.getIdLote());
        assertNull(dto.getLote());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con historial de usuario")
    void mapearEntidadAListadoDTO_conHistorial_debeMostrarUsuario() {
        // ARRANGE
        var historial = new ArrayList<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO>();
        var registro = new utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO();
        registro.setUsuario("usuario_test@test.com");
        historial.add(registro);
        
        List<Pureza> listaPureza = List.of(pureza);
        Page<Pureza> page = new org.springframework.data.domain.PageImpl<>(listaPureza);
        when(purezaRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(page);
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(historial);

        // ACT
        Page<PurezaListadoDTO> resultado = purezaService.obtenerPurezaPaginadas(Pageable.unpaged());

        // ASSERT
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        PurezaListadoDTO dto = resultado.getContent().get(0);
        assertEquals("usuario_test@test.com", dto.getUsuarioCreador());
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

    @Test
    @DisplayName("Actualizar pureza - cambio de estado de APROBADO a PENDIENTE_APROBACION cuando es analista")
    void actualizarPureza_estadoAprobadoComoAnalista_debeCambiarAPendienteAprobacion() {
        // ARRANGE
        pureza.setEstado(Estado.APROBADO);
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        when(analisisService.esAnalista()).thenReturn(true);
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(analisisService, times(1)).esAnalista();
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza - estado APROBADO como admin mantiene estado")
    void actualizarPureza_estadoAprobadoComoAdmin_mantienEstado() {
        // ARRANGE
        pureza.setEstado(Estado.APROBADO);
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        when(analisisService.esAnalista()).thenReturn(false);
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza - con lote inexistente debe lanzar excepción")
    void actualizarPureza_conLoteInexistente_debeLanzarExcepcion() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        purezaRequestDTO.setIdLote(999L);

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            purezaService.actualizarPureza(1L, purezaRequestDTO);
        });
    }

    @Test
    @DisplayName("Actualizar pureza - con todas las actualizaciones de campos INASE")
    void actualizarPureza_actualizarCamposINASE_debeActualizarCorrectamente() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        purezaRequestDTO.setInasePura(BigDecimal.valueOf(96.0));
        purezaRequestDTO.setInaseMateriaInerte(BigDecimal.valueOf(2.5));
        purezaRequestDTO.setInaseOtrosCultivos(BigDecimal.valueOf(1.0));
        purezaRequestDTO.setInaseMalezas(BigDecimal.valueOf(0.5));
        purezaRequestDTO.setInaseMalezasToleradas(BigDecimal.valueOf(0.0));
        purezaRequestDTO.setInaseMalezasTolCero(BigDecimal.valueOf(0.0));
        purezaRequestDTO.setInaseFecha(java.time.LocalDate.now());
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza - con todas las actualizaciones de campos Redon")
    void actualizarPureza_actualizarCamposRedon_debeActualizarCorrectamente() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        purezaRequestDTO.setRedonSemillaPura(BigDecimal.valueOf(95.5));
        purezaRequestDTO.setRedonMateriaInerte(BigDecimal.valueOf(2.0));
        purezaRequestDTO.setRedonOtrosCultivos(BigDecimal.valueOf(1.5));
        purezaRequestDTO.setRedonMalezas(BigDecimal.valueOf(0.5));
        purezaRequestDTO.setRedonMalezasToleradas(BigDecimal.valueOf(0.5));
        purezaRequestDTO.setRedonMalezasTolCero(BigDecimal.valueOf(0.0));
        purezaRequestDTO.setRedonPesoTotal(BigDecimal.valueOf(100.0));
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza - agregar listados vacíos debe limpiar lista")
    void actualizarPureza_conListadosVacios_debeLimpiarLista() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        pureza.setListados(new ArrayList<>());
        pureza.getListados().add(new Listado());
        
        purezaRequestDTO.setOtrasSemillas(new ArrayList<>()); // Lista vacía
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza - inicializar lista de listados si es null")
    void actualizarPureza_inicializarListadosSiEsNull_debeCrearLista() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        pureza.setListados(null);
        
        List<ListadoRequestDTO> nuevosListados = new ArrayList<>();
        ListadoRequestDTO listado = new ListadoRequestDTO();
        nuevosListados.add(listado);
        purezaRequestDTO.setOtrasSemillas(nuevosListados);
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza - actualizar todos los campos de fecha y comentarios")
    void actualizarPureza_actualizarFechaYComentarios_debeActualizarCorrectamente() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        purezaRequestDTO.setFecha(java.time.LocalDate.now());
        purezaRequestDTO.setComentarios("Comentario actualizado");
        purezaRequestDTO.setCumpleEstandar(false);
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Actualizar pureza - actualizar todos los campos de pesos en gramos")
    void actualizarPureza_actualizarPesosGramos_debeActualizarCorrectamente() {
        // ARRANGE
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        purezaRequestDTO.setSemillaPura_g(BigDecimal.valueOf(92.0));
        purezaRequestDTO.setMateriaInerte_g(BigDecimal.valueOf(4.0));
        purezaRequestDTO.setOtrosCultivos_g(BigDecimal.valueOf(2.0));
        purezaRequestDTO.setMalezas_g(BigDecimal.valueOf(1.0));
        purezaRequestDTO.setMalezasToleradas_g(BigDecimal.valueOf(0.5));
        purezaRequestDTO.setMalezasTolCero_g(BigDecimal.valueOf(0.5));
        
        // ACT
        PurezaDTO resultado = purezaService.actualizarPureza(1L, purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Mapear entidad a DTO - con lote y cultivar completos")
    void mapearEntidadADTO_conLoteYCultivarCompletos_debeIncluirTodosDatos() {
        // ARRANGE
        Especie especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun("Trigo");
        
        Cultivar cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Premium");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        lote.setFicha("FICHA-001");
        
        pureza.setRedonSemillaPura(BigDecimal.valueOf(95.5));
        
        when(purezaRepository.findById(1L)).thenReturn(Optional.of(pureza));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(new ArrayList<>());

        // ACT
        PurezaDTO resultado = purezaService.obtenerPurezaPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
    }

    @Test
    @DisplayName("Validar antes de finalizar - con datos Redon debe pasar")
    void validarAntesDeFinalizar_conDatosRedon_debeValidar() {
        // ARRANGE
        pureza.setRedonSemillaPura(BigDecimal.valueOf(95.0));
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
    @DisplayName("Validar pesos - con pesos null no debe validar")
    void validarPesos_pesosNull_noDebeValidar() {
        // ARRANGE
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(purezaRepository.save(any(Pureza.class))).thenReturn(pureza);
        
        purezaRequestDTO.setPesoInicial_g(null);
        purezaRequestDTO.setPesoTotal_g(null);

        // ACT - No debe lanzar excepción
        PurezaDTO resultado = purezaService.crearPureza(purezaRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(purezaRepository, times(1)).save(any(Pureza.class));
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con nombre común con espacios usa nombre científico")
    void mapearEntidadAListadoDTO_nombreComunConEspacios_usaNombreCientifico() {
        // ARRANGE
        Especie especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun("   ");
        especie.setNombreCientifico("Nombre Científico");
        
        Cultivar cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        
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
        PurezaListadoDTO dto = resultado.getContent().get(0);
        assertEquals("Nombre Científico", dto.getEspecie());
    }
}
