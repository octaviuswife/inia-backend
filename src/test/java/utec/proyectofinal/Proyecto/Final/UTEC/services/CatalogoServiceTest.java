package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoCrudRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CatalogoService
 * 
 * Funcionalidades testeadas:
 * - Creación de catálogos (especies, sustratos, lugares, etc.)
 * - Validación de duplicados por tipo y valor
 * - Obtención de catálogos por tipo
 * - Activación/Desactivación de catálogos
 * - Actualización de valores
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CatalogoService")
class CatalogoServiceTest {

    @Mock
    private CatalogoCrudRepository catalogoRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    private CatalogoRequestDTO catalogoRequestDTO;
    private Catalogo catalogo;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        catalogoRequestDTO = new CatalogoRequestDTO();
        catalogoRequestDTO.setTipo("HUMEDAD");
        catalogoRequestDTO.setValor("10%");

        catalogo = new Catalogo();
        catalogo.setId(1L);
        catalogo.setTipo(TipoCatalogo.HUMEDAD);
        catalogo.setValor("10%");
        catalogo.setActivo(true);
    }

    @Test
    @DisplayName("Crear catálogo - debe crear con activo=true")
    void crear_debeCrearCatalogoActivo() {
        // ARRANGE
        when(catalogoRepository.findByTipoAndValor(TipoCatalogo.HUMEDAD, "10%"))
            .thenReturn(Optional.empty());
        when(catalogoRepository.save(any(Catalogo.class))).thenReturn(catalogo);

        // ACT
        CatalogoDTO resultado = catalogoService.crear(catalogoRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("10%", resultado.getValor());
        verify(catalogoRepository, times(1)).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Crear catálogo duplicado - debe lanzar excepción")
    void crear_conDuplicado_debeLanzarExcepcion() {
        // ARRANGE
        when(catalogoRepository.findByTipoAndValor(TipoCatalogo.HUMEDAD, "10%"))
            .thenReturn(Optional.of(catalogo));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            catalogoService.crear(catalogoRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("Ya existe un catálogo"));
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Obtener todos los catálogos activos - debe retornar solo activos")
    void obtenerTodos_debeRetornarSoloActivos() {
        // ARRANGE
        Catalogo catalogo2 = new Catalogo();
        catalogo2.setId(2L);
        catalogo2.setTipo(TipoCatalogo.ORIGEN);
        catalogo2.setValor("Nacional");
        catalogo2.setActivo(true);
        
        when(catalogoRepository.findByActivoTrue())
            .thenReturn(Arrays.asList(catalogo, catalogo2));

        // ACT
        List<CatalogoDTO> resultado = catalogoService.obtenerTodos();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(catalogoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener por tipo - debe retornar solo del tipo especificado")
    void obtenerPorTipo_debeRetornarSoloDelTipo() {
        // ARRANGE
        when(catalogoRepository.findByTipoAndActivoTrue(TipoCatalogo.HUMEDAD))
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        List<CatalogoDTO> resultado = catalogoService.obtenerPorTipo("HUMEDAD");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("10%", resultado.get(0).getValor());
        verify(catalogoRepository, times(1)).findByTipoAndActivoTrue(TipoCatalogo.HUMEDAD);
    }

    @Test
    @DisplayName("Obtener por tipo con filtro activo=true - debe retornar solo activos")
    void obtenerPorTipoConFiltro_activos_debeRetornarSoloActivos() {
        // ARRANGE
        when(catalogoRepository.findByTipoAndActivoTrue(TipoCatalogo.HUMEDAD))
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        List<CatalogoDTO> resultado = catalogoService.obtenerPorTipo("HUMEDAD", true);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(catalogoRepository, times(1)).findByTipoAndActivoTrue(TipoCatalogo.HUMEDAD);
    }

    @Test
    @DisplayName("Obtener por tipo con filtro activo=false - debe retornar solo inactivos")
    void obtenerPorTipoConFiltro_inactivos_debeRetornarSoloInactivos() {
        // ARRANGE
        catalogo.setActivo(false);
        when(catalogoRepository.findByTipoAndActivoFalse(TipoCatalogo.HUMEDAD))
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        List<CatalogoDTO> resultado = catalogoService.obtenerPorTipo("HUMEDAD", false);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(catalogoRepository, times(1)).findByTipoAndActivoFalse(TipoCatalogo.HUMEDAD);
    }

    @Test
    @DisplayName("Obtener por tipo sin filtro - debe retornar todos (activos e inactivos)")
    void obtenerPorTipoSinFiltro_debeRetornarTodos() {
        // ARRANGE
        Catalogo catalogoInactivo = new Catalogo();
        catalogoInactivo.setId(2L);
        catalogoInactivo.setTipo(TipoCatalogo.HUMEDAD);
        catalogoInactivo.setValor("12%");
        catalogoInactivo.setActivo(false);
        
        when(catalogoRepository.findByTipo(TipoCatalogo.HUMEDAD))
            .thenReturn(Arrays.asList(catalogo, catalogoInactivo));

        // ACT
        List<CatalogoDTO> resultado = catalogoService.obtenerPorTipo("HUMEDAD", null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(catalogoRepository, times(1)).findByTipo(TipoCatalogo.HUMEDAD);
    }

    @Test
    @DisplayName("Obtener por ID - debe retornar el catálogo si existe")
    void obtenerPorId_cuandoExiste_debeRetornarCatalogo() {
        // ARRANGE
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));

        // ACT
        CatalogoDTO resultado = catalogoService.obtenerPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("10%", resultado.getValor());
    }

    @Test
    @DisplayName("Obtener por ID inexistente - debe retornar null")
    void obtenerPorId_cuandoNoExiste_debeRetornarNull() {
        // ARRANGE
        when(catalogoRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT
        CatalogoDTO resultado = catalogoService.obtenerPorId(999L);

        // ASSERT
        assertNull(resultado);
    }

    @Test
    @DisplayName("Actualizar catálogo - debe actualizar correctamente")
    void actualizar_debeActualizarCorrectamente() {
        // ARRANGE
        catalogoRequestDTO.setValor("12%");
        
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));
        when(catalogoRepository.findByTipoAndValor(TipoCatalogo.HUMEDAD, "12%"))
            .thenReturn(Optional.empty());
        when(catalogoRepository.save(any(Catalogo.class))).thenReturn(catalogo);

        // ACT
        CatalogoDTO resultado = catalogoService.actualizar(1L, catalogoRequestDTO);

        // ASSERT
        assertNotNull(resultado);
        verify(catalogoRepository, times(1)).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Actualizar con valor duplicado - debe lanzar excepción")
    void actualizar_conValorDuplicado_debeLanzarExcepcion() {
        // ARRANGE
        Catalogo otroCatalogo = new Catalogo();
        otroCatalogo.setId(2L);
        otroCatalogo.setTipo(TipoCatalogo.HUMEDAD);
        otroCatalogo.setValor("15%");
        
        catalogoRequestDTO.setValor("15%");
        
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));
        when(catalogoRepository.findByTipoAndValor(TipoCatalogo.HUMEDAD, "15%"))
            .thenReturn(Optional.of(otroCatalogo));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            catalogoService.actualizar(1L, catalogoRequestDTO);
        });
        
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Eliminar catálogo - debe cambiar activo a false")
    void eliminar_debeCambiarActivoAFalse() {
        // ARRANGE
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));
        when(catalogoRepository.save(any(Catalogo.class))).thenReturn(catalogo);

        // ACT
        catalogoService.eliminar(1L);

        // ASSERT
        verify(catalogoRepository, times(1)).findById(1L);
        verify(catalogoRepository, times(1)).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Reactivar catálogo - debe cambiar activo a true")
    void reactivar_debeCambiarActivoATrue() {
        // ARRANGE
        catalogo.setActivo(false);
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));
        when(catalogoRepository.save(any(Catalogo.class))).thenReturn(catalogo);

        // ACT
        CatalogoDTO resultado = catalogoService.reactivar(1L);

        // ASSERT
        assertNotNull(resultado);
        verify(catalogoRepository, times(1)).findById(1L);
        verify(catalogoRepository, times(1)).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Crear catálogos de diferentes tipos - debe permitir mismo valor en diferentes tipos")
    void crear_mismoValorDiferentesTipos_debePermitir() {
        // ARRANGE
        CatalogoRequestDTO origenDTO = new CatalogoRequestDTO();
        origenDTO.setTipo("ORIGEN");
        origenDTO.setValor("Nacional");
        
        Catalogo origenCatalogo = new Catalogo();
        origenCatalogo.setId(2L);
        origenCatalogo.setTipo(TipoCatalogo.ORIGEN);
        origenCatalogo.setValor("Nacional");
        origenCatalogo.setActivo(true);
        
        when(catalogoRepository.findByTipoAndValor(TipoCatalogo.ORIGEN, "Nacional"))
            .thenReturn(Optional.empty());
        when(catalogoRepository.save(any(Catalogo.class))).thenReturn(origenCatalogo);

        // ACT
        CatalogoDTO resultado = catalogoService.crear(origenDTO);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Nacional", resultado.getValor());
        verify(catalogoRepository, times(1)).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Obtener entidad por ID - debe retornar la entidad si existe")
    void obtenerEntidadPorId_cuandoExiste_debeRetornarEntidad() {
        // ARRANGE
        when(catalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));

        // ACT
        Catalogo resultado = catalogoService.obtenerEntidadPorId(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("10%", resultado.getValor());
    }

    @Test
    @DisplayName("Obtener entidad por ID inexistente - debe retornar null")
    void obtenerEntidadPorId_cuandoNoExiste_debeRetornarNull() {
        // ARRANGE
        when(catalogoRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT
        Catalogo resultado = catalogoService.obtenerEntidadPorId(999L);

        // ASSERT
        assertNull(resultado);
    }

    @Test
    @DisplayName("Eliminar físicamente - debe eliminar el registro de la base de datos")
    void eliminarFisicamente_debeEliminarRegistro() {
        // ARRANGE
        doNothing().when(catalogoRepository).deleteById(1L);

        // ACT
        catalogoService.eliminarFisicamente(1L);

        // ASSERT
        verify(catalogoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Obtener catálogos paginados - debe retornar página de catálogos activos")
    void obtenerCatalogosPaginados_debeRetornarPaginaActivos() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginados(pageable);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(catalogoRepository, times(1)).findByActivoTrueOrderByTipoAscValorAsc(pageable);
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtro activos - debe retornar solo activos")
    void obtenerCatalogosPaginadosConFiltro_activos_debeRetornarSoloActivos() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltro(pageable, "activos");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(catalogoRepository, times(1)).findByActivoTrueOrderByTipoAscValorAsc(pageable);
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtro inactivos - debe retornar solo inactivos")
    void obtenerCatalogosPaginadosConFiltro_inactivos_debeRetornarSoloInactivos() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        catalogo.setActivo(false);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByActivoFalseOrderByTipoAscValorAsc(pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltro(pageable, "inactivos");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(catalogoRepository, times(1)).findByActivoFalseOrderByTipoAscValorAsc(pageable);
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtro todos - debe retornar todos")
    void obtenerCatalogosPaginadosConFiltro_todos_debeRetornarTodos() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findAllByOrderByTipoAscValorAsc(pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltro(pageable, "todos");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
        verify(catalogoRepository, times(1)).findAllByOrderByTipoAscValorAsc(pageable);
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con tipo y búsqueda")
    void obtenerCatalogosPaginadosConFiltros_conTipoYBusqueda_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        when(catalogoRepository.findByTipoAndActivoTrue(TipoCatalogo.HUMEDAD))
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, "10", true, "HUMEDAD");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con tipo sin búsqueda activos")
    void obtenerCatalogosPaginadosConFiltros_conTipoSinBusquedaActivos_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByTipoAndActivoTrueOrderByValorAsc(TipoCatalogo.HUMEDAD, pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, null, true, "HUMEDAD");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con tipo sin búsqueda inactivos")
    void obtenerCatalogosPaginadosConFiltros_conTipoSinBusquedaInactivos_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        catalogo.setActivo(false);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByTipoAndActivoFalseOrderByValorAsc(TipoCatalogo.HUMEDAD, pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, null, false, "HUMEDAD");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con tipo sin búsqueda sin filtro activo")
    void obtenerCatalogosPaginadosConFiltros_conTipoSinBusquedaSinFiltroActivo_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByTipoOrderByValorAsc(TipoCatalogo.HUMEDAD, pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, null, null, "HUMEDAD");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con tipo inválido")
    void obtenerCatalogosPaginadosConFiltros_conTipoInvalido_debeRetornarVacio() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, null, true, "TIPO_INVALIDO");

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - sin tipo con búsqueda activos")
    void obtenerCatalogosPaginadosConFiltros_sinTipoConBusquedaActivos_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        when(catalogoRepository.findByActivoTrue())
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, "10", true, null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - sin tipo con búsqueda inactivos")
    void obtenerCatalogosPaginadosConFiltros_sinTipoConBusquedaInactivos_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        catalogo.setActivo(false);
        
        when(catalogoRepository.findAll())
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, "10", false, null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - sin tipo con búsqueda sin filtro activo")
    void obtenerCatalogosPaginadosConFiltros_sinTipoConBusquedaSinFiltroActivo_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        when(catalogoRepository.findAll())
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, "10", null, null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - sin tipo sin búsqueda activos")
    void obtenerCatalogosPaginadosConFiltros_sinTipoSinBusquedaActivos_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, null, true, null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - sin tipo sin búsqueda inactivos")
    void obtenerCatalogosPaginadosConFiltros_sinTipoSinBusquedaInactivos_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        catalogo.setActivo(false);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findByActivoFalseOrderByTipoAscValorAsc(pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, null, false, null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - sin tipo sin búsqueda sin filtro activo")
    void obtenerCatalogosPaginadosConFiltros_sinTipoSinBusquedaSinFiltroActivo_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Catalogo> page = new org.springframework.data.domain.PageImpl<>(
            Arrays.asList(catalogo)
        );
        
        when(catalogoRepository.findAllByOrderByTipoAscValorAsc(pageable))
            .thenReturn(page);

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, null, null, null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con tipo y búsqueda inactivos")
    void obtenerCatalogosPaginadosConFiltros_conTipoYBusquedaInactivos_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        catalogo.setActivo(false);
        
        when(catalogoRepository.findByTipoAndActivoFalse(TipoCatalogo.HUMEDAD))
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, "10", false, "HUMEDAD");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con tipo y búsqueda sin filtro activo")
    void obtenerCatalogosPaginadosConFiltros_conTipoYBusquedaSinFiltroActivo_debeFiltrar() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        when(catalogoRepository.findByTipo(TipoCatalogo.HUMEDAD))
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, "10", null, "HUMEDAD");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener catálogos paginados con filtros - con búsqueda sin coincidencias")
    void obtenerCatalogosPaginadosConFiltros_conBusquedaSinCoincidencias_debeRetornarVacio() {
        // ARRANGE
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        when(catalogoRepository.findByActivoTrue())
            .thenReturn(Arrays.asList(catalogo));

        // ACT
        org.springframework.data.domain.Page<CatalogoDTO> resultado = 
            catalogoService.obtenerCatalogosPaginadosConFiltros(pageable, "999", true, null);

        // ASSERT
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Actualizar catálogo inexistente - debe retornar null")
    void actualizar_cuandoNoExiste_debeRetornarNull() {
        // ARRANGE
        when(catalogoRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT
        CatalogoDTO resultado = catalogoService.actualizar(999L, catalogoRequestDTO);

        // ASSERT
        assertNull(resultado);
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Reactivar catálogo inexistente - debe retornar null")
    void reactivar_cuandoNoExiste_debeRetornarNull() {
        // ARRANGE
        when(catalogoRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT
        CatalogoDTO resultado = catalogoService.reactivar(999L);

        // ASSERT
        assertNull(resultado);
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Eliminar catálogo inexistente - no debe lanzar excepción")
    void eliminar_cuandoNoExiste_noDebeLanzarExcepcion() {
        // ARRANGE
        when(catalogoRepository.findById(999L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertDoesNotThrow(() -> catalogoService.eliminar(999L));
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }
}
