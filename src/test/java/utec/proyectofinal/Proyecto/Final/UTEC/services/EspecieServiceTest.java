package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.EspecieRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de EspecieService")
class EspecieServiceTest {

    @Mock
    private EspecieRepository especieRepository;

    @InjectMocks
    private EspecieService especieService;

    private EspecieRequestDTO especieRequestDTO;
    private Especie especie;

    @BeforeEach
    void setUp() {
        especieRequestDTO = new EspecieRequestDTO();
        especieRequestDTO.setNombreComun("Trigo");
        especieRequestDTO.setNombreCientifico("Triticum aestivum");

        especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun("Trigo");
        especie.setNombreCientifico("Triticum aestivum");
        especie.setActivo(true);
    }

    @Test
    @DisplayName("Obtener todas las especies activas - debe retornar lista")
    void obtenerTodas_debeRetornarListaActivas() {
        when(especieRepository.findByActivoTrue()).thenReturn(Arrays.asList(especie));

        List<EspecieDTO> resultado = especieService.obtenerTodas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(especieRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener especies inactivas - debe retornar solo inactivas")
    void obtenerInactivas_debeRetornarSoloInactivas() {
        especie.setActivo(false);
        when(especieRepository.findByActivoFalse()).thenReturn(Arrays.asList(especie));

        List<EspecieDTO> resultado = especieService.obtenerInactivas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(especieRepository, times(1)).findByActivoFalse();
    }

    @Test
    @DisplayName("Obtener todas con filtro null - debe retornar todas")
    void obtenerTodas_conFiltroNull_debeRetornarTodas() {
        when(especieRepository.findAll()).thenReturn(Arrays.asList(especie));

        List<EspecieDTO> resultado = especieService.obtenerTodas(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(especieRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Buscar por nombre común - debe retornar coincidencias")
    void buscarPorNombreComun_debeRetornarCoincidencias() {
        when(especieRepository.findByNombreComunContainingIgnoreCaseAndActivoTrue("Trigo"))
            .thenReturn(Arrays.asList(especie));

        List<EspecieDTO> resultado = especieService.buscarPorNombreComun("Trigo");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombreComun().contains("Trigo"));
        verify(especieRepository, times(1)).findByNombreComunContainingIgnoreCaseAndActivoTrue("Trigo");
    }

    @Test
    @DisplayName("Buscar por nombre científico - debe retornar coincidencias")
    void buscarPorNombreCientifico_debeRetornarCoincidencias() {
        when(especieRepository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue("Triticum"))
            .thenReturn(Arrays.asList(especie));

        List<EspecieDTO> resultado = especieService.buscarPorNombreCientifico("Triticum");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombreCientifico().contains("Triticum"));
        verify(especieRepository, times(1)).findByNombreCientificoContainingIgnoreCaseAndActivoTrue("Triticum");
    }

    @Test
    @DisplayName("Obtener por ID - debe retornar especie existente")
    void obtenerPorId_especieExistente_debeRetornarEspecie() {
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));

        EspecieDTO resultado = especieService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getEspecieID());
        assertEquals("Trigo", resultado.getNombreComun());
        verify(especieRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener por ID inexistente - debe retornar null")
    void obtenerPorId_especieInexistente_debeRetornarNull() {
        when(especieRepository.findById(999L)).thenReturn(Optional.empty());

        EspecieDTO resultado = especieService.obtenerPorId(999L);

        assertNull(resultado);
        verify(especieRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Crear especie válida - debe guardar y retornar DTO")
    void crear_especieValida_debeGuardarYRetornarDTO() {
        when(especieRepository.save(any(Especie.class))).thenReturn(especie);

        EspecieDTO resultado = especieService.crear(especieRequestDTO);

        assertNotNull(resultado);
        assertEquals("Trigo", resultado.getNombreComun());
        assertEquals("Triticum aestivum", resultado.getNombreCientifico());
        assertTrue(resultado.getActivo());
        verify(especieRepository, times(1)).save(any(Especie.class));
    }

    @Test
    @DisplayName("Actualizar especie - debe actualizar y retornar DTO")
    void actualizar_debeActualizarYRetornarDTO() {
        especieRequestDTO.setNombreComun("Trigo Actualizado");
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));
        when(especieRepository.save(any(Especie.class))).thenReturn(especie);

        EspecieDTO resultado = especieService.actualizar(1L, especieRequestDTO);

        assertNotNull(resultado);
        verify(especieRepository, times(1)).save(any(Especie.class));
    }

    @Test
    @DisplayName("Actualizar especie inexistente - debe retornar null")
    void actualizar_especieInexistente_debeRetornarNull() {
        when(especieRepository.findById(999L)).thenReturn(Optional.empty());

        EspecieDTO resultado = especieService.actualizar(999L, especieRequestDTO);

        assertNull(resultado);
        verify(especieRepository, never()).save(any(Especie.class));
    }

    @Test
    @DisplayName("Eliminar especie - debe cambiar activo a false")
    void eliminar_debeCambiarActivoAFalse() {
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));
        when(especieRepository.save(any(Especie.class))).thenReturn(especie);

        especieService.eliminar(1L);

        verify(especieRepository, times(1)).findById(1L);
        verify(especieRepository, times(1)).save(any(Especie.class));
    }

    @Test
    @DisplayName("Reactivar especie - debe cambiar activo a true")
    void reactivar_debeCambiarActivoATrue() {
        especie.setActivo(false);
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));
        when(especieRepository.save(any(Especie.class))).thenReturn(especie);

        EspecieDTO resultado = especieService.reactivar(1L);

        assertNotNull(resultado);
        verify(especieRepository, times(1)).save(any(Especie.class));
    }

    @Test
    @DisplayName("Reactivar especie ya activa - debe lanzar excepción")
    void reactivar_especieYaActiva_debeLanzarExcepcion() {
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            especieService.reactivar(1L);
        });

        assertTrue(exception.getMessage().contains("ya está activa"));
        verify(especieRepository, never()).save(any(Especie.class));
    }

    @Test
    @DisplayName("Reactivar especie inexistente - debe retornar null")
    void reactivar_especieInexistente_debeRetornarNull() {
        when(especieRepository.findById(999L)).thenReturn(Optional.empty());

        EspecieDTO resultado = especieService.reactivar(999L);

        assertNull(resultado);
        verify(especieRepository, never()).save(any(Especie.class));
    }

    @Test
    @DisplayName("Obtener entidad por ID - debe retornar entidad")
    void obtenerEntidadPorId_debeRetornarEntidad() {
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));

        Especie resultado = especieService.obtenerEntidadPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getEspecieID());
        verify(especieRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener entidad por ID inexistente - debe retornar null")
    void obtenerEntidadPorId_inexistente_debeRetornarNull() {
        when(especieRepository.findById(999L)).thenReturn(Optional.empty());

        Especie resultado = especieService.obtenerEntidadPorId(999L);

        assertNull(resultado);
        verify(especieRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Obtener especies paginadas - debe retornar página")
    void obtenerEspeciesPaginadas_debeRetornarPagina() {
        when(especieRepository.findByActivoTrueOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadas(org.springframework.data.domain.Pageable.unpaged());

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findByActivoTrueOrderByNombreComunAsc(any());
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtro 'activos'")
    void obtenerEspeciesPaginadasConFiltro_filtroActivos() {
        when(especieRepository.findByActivoTrueOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltro(
                org.springframework.data.domain.Pageable.unpaged(), 
                "activos"
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findByActivoTrueOrderByNombreComunAsc(any());
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtro 'inactivos'")
    void obtenerEspeciesPaginadasConFiltro_filtroInactivos() {
        when(especieRepository.findByActivoFalseOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltro(
                org.springframework.data.domain.Pageable.unpaged(), 
                "inactivos"
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findByActivoFalseOrderByNombreComunAsc(any());
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtro 'todos'")
    void obtenerEspeciesPaginadasConFiltro_filtroTodos() {
        when(especieRepository.findAllByOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltro(
                org.springframework.data.domain.Pageable.unpaged(), 
                "todos"
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findAllByOrderByNombreComunAsc(any());
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtros - sin término de búsqueda y activo null")
    void obtenerEspeciesPaginadasConFiltros_sinBusquedaActivoNull() {
        when(especieRepository.findAllByOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltros(
                org.springframework.data.domain.Pageable.unpaged(),
                null,
                null
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findAllByOrderByNombreComunAsc(any());
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtros - sin término de búsqueda y activo true")
    void obtenerEspeciesPaginadasConFiltros_sinBusquedaActivoTrue() {
        when(especieRepository.findByActivoTrueOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltros(
                org.springframework.data.domain.Pageable.unpaged(),
                null,
                true
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findByActivoTrueOrderByNombreComunAsc(any());
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtros - sin término de búsqueda y activo false")
    void obtenerEspeciesPaginadasConFiltros_sinBusquedaActivoFalse() {
        when(especieRepository.findByActivoFalseOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltros(
                org.springframework.data.domain.Pageable.unpaged(),
                null,
                false
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findByActivoFalseOrderByNombreComunAsc(any());
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtros - con término de búsqueda y activo null")
    void obtenerEspeciesPaginadasConFiltros_conBusquedaActivoNull() {
        when(especieRepository.findAll()).thenReturn(Arrays.asList(especie));

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltros(
                PageRequest.of(0, 10),
                "Trigo",
                null
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtros - con término de búsqueda y activo true")
    void obtenerEspeciesPaginadasConFiltros_conBusquedaActivoTrue() {
        when(especieRepository.findByNombreComunContainingIgnoreCaseAndActivoTrue("Trigo"))
            .thenReturn(Arrays.asList(especie));
        when(especieRepository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue("Trigo"))
            .thenReturn(Arrays.asList());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltros(
                PageRequest.of(0, 10),
                "Trigo",
                true
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findByNombreComunContainingIgnoreCaseAndActivoTrue("Trigo");
        verify(especieRepository, times(1)).findByNombreCientificoContainingIgnoreCaseAndActivoTrue("Trigo");
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtros - con término de búsqueda y activo false")
    void obtenerEspeciesPaginadasConFiltros_conBusquedaActivoFalse() {
        especie.setActivo(false);
        when(especieRepository.findAll()).thenReturn(Arrays.asList(especie));

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltros(
                PageRequest.of(0, 10),
                "Trigo",
                false
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener especies paginadas con filtros - término vacío debe ignorarse")
    void obtenerEspeciesPaginadasConFiltros_terminoVacio() {
        when(especieRepository.findByActivoTrueOrderByNombreComunAsc(any()))
            .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<EspecieDTO> resultado = 
            especieService.obtenerEspeciesPaginadasConFiltros(
                org.springframework.data.domain.Pageable.unpaged(),
                "   ",
                true
            );

        assertNotNull(resultado);
        verify(especieRepository, times(1)).findByActivoTrueOrderByNombreComunAsc(any());
    }
}
