package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CultivarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CultivarRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CultivarDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CultivarService")
class CultivarServiceTest {

    @Mock
    private CultivarRepository cultivarRepository;

    @Mock
    private EspecieService especieService;

    @InjectMocks
    private CultivarService cultivarService;

    private CultivarRequestDTO cultivarRequestDTO;
    private Cultivar cultivar;
    private Especie especie;

    @BeforeEach
    void setUp() {
        especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun("Trigo");

        cultivarRequestDTO = new CultivarRequestDTO();
        cultivarRequestDTO.setNombre("Cultivar Test");
        cultivarRequestDTO.setEspecieID(1L);

        cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        cultivar.setActivo(true);
    }

    @Test
    @DisplayName("Obtener todos los cultivares activos - debe retornar lista")
    void obtenerTodos_debeRetornarListaActivos() {
        when(cultivarRepository.findByActivoTrue()).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener cultivares inactivos - debe retornar solo inactivos")
    void obtenerInactivos_debeRetornarSoloInactivos() {
        cultivar.setActivo(false);
        when(cultivarRepository.findByActivoFalse()).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerInactivos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findByActivoFalse();
    }

    @Test
    @DisplayName("Obtener todos con filtro null - debe retornar todos")
    void obtenerTodos_conFiltroNull_debeRetornarTodos() {
        when(cultivarRepository.findAll()).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerTodos(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener cultivares por especie - debe retornar cultivares de la especie")
    void obtenerPorEspecie_debeRetornarCultivaresDeLaEspecie() {
        when(cultivarRepository.findByEspecieEspecieIDAndActivoTrue(1L)).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerPorEspecie(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getEspecieID());
        verify(cultivarRepository, times(1)).findByEspecieEspecieIDAndActivoTrue(1L);
    }

    @Test
    @DisplayName("Buscar por nombre - debe retornar coincidencias")
    void buscarPorNombre_debeRetornarCoincidencias() {
        when(cultivarRepository.findByNombreContainingIgnoreCaseAndActivoTrue("Test")).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.buscarPorNombre("Test");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombre().contains("Test"));
        verify(cultivarRepository, times(1)).findByNombreContainingIgnoreCaseAndActivoTrue("Test");
    }

    @Test
    @DisplayName("Obtener por ID - debe retornar cultivar existente")
    void obtenerPorId_cultivarExistente_debeRetornarCultivar() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));

        CultivarDTO resultado = cultivarService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCultivarID());
        assertEquals("Cultivar Test", resultado.getNombre());
        verify(cultivarRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener por ID inexistente - debe retornar null")
    void obtenerPorId_cultivarInexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        CultivarDTO resultado = cultivarService.obtenerPorId(999L);

        assertNull(resultado);
        verify(cultivarRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Crear cultivar válido - debe guardar y retornar DTO")
    void crear_cultivarValido_debeGuardarYRetornarDTO() {
        when(especieService.obtenerEntidadPorId(1L)).thenReturn(especie);
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        CultivarDTO resultado = cultivarService.crear(cultivarRequestDTO);

        assertNotNull(resultado);
        assertEquals("Cultivar Test", resultado.getNombre());
        verify(especieService, times(1)).obtenerEntidadPorId(1L);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Crear cultivar con especie inexistente - debe lanzar excepción")
    void crear_especieInexistente_debeLanzarExcepcion() {
        when(especieService.obtenerEntidadPorId(999L)).thenReturn(null);
        cultivarRequestDTO.setEspecieID(999L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cultivarService.crear(cultivarRequestDTO);
        });

        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Actualizar cultivar - debe actualizar y retornar DTO")
    void actualizar_debeActualizarYRetornarDTO() {
        cultivarRequestDTO.setNombre("Cultivar Actualizado");
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(especieService.obtenerEntidadPorId(1L)).thenReturn(especie);
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        CultivarDTO resultado = cultivarService.actualizar(1L, cultivarRequestDTO);

        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Actualizar cultivar inexistente - debe retornar null")
    void actualizar_cultivarInexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        CultivarDTO resultado = cultivarService.actualizar(999L, cultivarRequestDTO);

        assertNull(resultado);
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Eliminar cultivar - debe cambiar activo a false")
    void eliminar_debeCambiarActivoAFalse() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        cultivarService.eliminar(1L);

        verify(cultivarRepository, times(1)).findById(1L);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Reactivar cultivar - debe cambiar activo a true")
    void reactivar_debeCambiarActivoATrue() {
        cultivar.setActivo(false);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        CultivarDTO resultado = cultivarService.reactivar(1L);

        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Reactivar cultivar ya activo - debe lanzar excepción")
    void reactivar_cultivarYaActivo_debeLanzarExcepcion() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cultivarService.reactivar(1L);
        });

        assertTrue(exception.getMessage().contains("ya está activo"));
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Reactivar cultivar inexistente - debe retornar null")
    void reactivar_cultivarInexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        CultivarDTO resultado = cultivarService.reactivar(999L);

        assertNull(resultado);
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Obtener entidad por ID - debe retornar entidad")
    void obtenerEntidadPorId_debeRetornarEntidad() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));

        Cultivar resultado = cultivarService.obtenerEntidadPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCultivarID());
        verify(cultivarRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener entidad por ID inexistente - debe retornar null")
    void obtenerEntidadPorId_inexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        Cultivar resultado = cultivarService.obtenerEntidadPorId(999L);

        assertNull(resultado);
        verify(cultivarRepository, times(1)).findById(999L);
    }
}
