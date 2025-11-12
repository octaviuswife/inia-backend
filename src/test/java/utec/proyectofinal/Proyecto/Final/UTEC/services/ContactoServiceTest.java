package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Contacto;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ContactoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ContactoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ContactoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("Tests de ContactoService")
class ContactoServiceTest {

    @Mock
    private ContactoRepository contactoRepository;

    @InjectMocks
    private ContactoService contactoService;

    private ContactoRequestDTO contactoRequestDTO;
    private Contacto contacto;
    private Contacto empresa;

    @BeforeEach
    void setUp() {
        contactoRequestDTO = new ContactoRequestDTO();
        contactoRequestDTO.setNombre("Juan Pérez");
        contactoRequestDTO.setContacto("juan@example.com");
        contactoRequestDTO.setTipo(TipoContacto.CLIENTE);

        contacto = new Contacto();
        contacto.setContactoID(1L);
        contacto.setNombre("Juan Pérez");
        contacto.setContacto("juan@example.com");
        contacto.setTipo(TipoContacto.CLIENTE);
        contacto.setActivo(true);

        empresa = new Contacto();
        empresa.setContactoID(2L);
        empresa.setNombre("Empresa SA");
        empresa.setContacto("empresa@example.com");
        empresa.setTipo(TipoContacto.EMPRESA);
        empresa.setActivo(true);
    }

    @Test
    @DisplayName("Obtener todos los contactos activos - debe retornar lista")
    void obtenerTodosLosContactos_debeRetornarLista() {
        when(contactoRepository.findByActivoTrue()).thenReturn(Arrays.asList(contacto, empresa));

        List<ContactoDTO> resultado = contactoService.obtenerTodosLosContactos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(contactoRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener contactos por tipo - debe retornar solo clientes")
    void obtenerContactosPorTipo_debeRetornarSoloClientes() {
        when(contactoRepository.findByTipoAndActivoTrue(TipoContacto.CLIENTE)).thenReturn(Arrays.asList(contacto));

        List<ContactoDTO> resultado = contactoService.obtenerContactosPorTipo(TipoContacto.CLIENTE);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoContacto.CLIENTE, resultado.get(0).getTipo());
        verify(contactoRepository, times(1)).findByTipoAndActivoTrue(TipoContacto.CLIENTE);
    }

    @Test
    @DisplayName("Obtener clientes - debe retornar solo clientes activos")
    void obtenerClientes_debeRetornarSoloClientes() {
        when(contactoRepository.findByTipoAndActivoTrue(TipoContacto.CLIENTE)).thenReturn(Arrays.asList(contacto));

        List<ContactoDTO> resultado = contactoService.obtenerClientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoContacto.CLIENTE, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("Obtener empresas - debe retornar solo empresas activas")
    void obtenerEmpresas_debeRetornarSoloEmpresas() {
        when(contactoRepository.findByTipoAndActivoTrue(TipoContacto.EMPRESA)).thenReturn(Arrays.asList(empresa));

        List<ContactoDTO> resultado = contactoService.obtenerEmpresas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoContacto.EMPRESA, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("Obtener contacto por ID - debe retornar contacto existente")
    void obtenerContactoPorId_contactoExistente_debeRetornarContacto() {
        when(contactoRepository.findByContactoIDAndActivoTrue(1L)).thenReturn(Optional.of(contacto));

        ContactoDTO resultado = contactoService.obtenerContactoPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getContactoID());
        assertEquals("Juan Pérez", resultado.getNombre());
        verify(contactoRepository, times(1)).findByContactoIDAndActivoTrue(1L);
    }

    @Test
    @DisplayName("Obtener contacto por ID inexistente - debe lanzar excepción")
    void obtenerContactoPorId_contactoInexistente_debeLanzarExcepcion() {
        when(contactoRepository.findByContactoIDAndActivoTrue(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contactoService.obtenerContactoPorId(999L);
        });

        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    @DisplayName("Crear contacto válido - debe guardar y retornar DTO")
    void crearContacto_valido_debeGuardarYRetornarDTO() {
        when(contactoRepository.existsByNombreIgnoreCaseAndTipo(anyString(), any(TipoContacto.class))).thenReturn(false);
        when(contactoRepository.save(any(Contacto.class))).thenReturn(contacto);

        ContactoDTO resultado = contactoService.crearContacto(contactoRequestDTO);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombre());
        verify(contactoRepository, times(1)).save(any(Contacto.class));
    }

    @Test
    @DisplayName("Crear contacto con nombre duplicado - debe lanzar excepción")
    void crearContacto_nombreDuplicado_debeLanzarExcepcion() {
        when(contactoRepository.existsByNombreIgnoreCaseAndTipo(anyString(), any(TipoContacto.class))).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contactoService.crearContacto(contactoRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Ya existe"));
        verify(contactoRepository, never()).save(any(Contacto.class));
    }

    @Test
    @DisplayName("Crear contacto sin nombre - debe lanzar excepción")
    void crearContacto_sinNombre_debeLanzarExcepcion() {
        contactoRequestDTO.setNombre("");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contactoService.crearContacto(contactoRequestDTO);
        });

        assertTrue(exception.getMessage().contains("requerido"));
        verify(contactoRepository, never()).save(any(Contacto.class));
    }

    @Test
    @DisplayName("Actualizar contacto - debe actualizar y retornar DTO")
    void actualizarContacto_debeActualizarYRetornarDTO() {
        contactoRequestDTO.setNombre("Juan Pérez Actualizado");
        when(contactoRepository.findByContactoIDAndActivoTrue(1L)).thenReturn(Optional.of(contacto));
        when(contactoRepository.existsByNombreIgnoreCaseAndTipoAndContactoIDNot(anyString(), any(TipoContacto.class), anyLong())).thenReturn(false);
        when(contactoRepository.save(any(Contacto.class))).thenReturn(contacto);

        ContactoDTO resultado = contactoService.actualizarContacto(1L, contactoRequestDTO);

        assertNotNull(resultado);
        verify(contactoRepository, times(1)).save(any(Contacto.class));
    }

    @Test
    @DisplayName("Eliminar contacto - debe cambiar activo a false")
    void eliminarContacto_debeCambiarActivoAFalse() {
        when(contactoRepository.findByContactoIDAndActivoTrue(1L)).thenReturn(Optional.of(contacto));
        when(contactoRepository.save(any(Contacto.class))).thenReturn(contacto);

        contactoService.eliminarContacto(1L);

        verify(contactoRepository, times(1)).save(any(Contacto.class));
    }

    @Test
    @DisplayName("Reactivar contacto - debe cambiar activo a true")
    void reactivarContacto_debeCambiarActivoATrue() {
        contacto.setActivo(false);
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(contacto));
        when(contactoRepository.save(any(Contacto.class))).thenReturn(contacto);

        ContactoDTO resultado = contactoService.reactivarContacto(1L);

        assertNotNull(resultado);
        verify(contactoRepository, times(1)).save(any(Contacto.class));
    }

    @Test
    @DisplayName("Reactivar contacto ya activo - debe lanzar excepción")
    void reactivarContacto_yaActivo_debeLanzarExcepcion() {
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(contacto));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            contactoService.reactivarContacto(1L);
        });

        assertTrue(exception.getMessage().contains("ya está activo"));
    }

    @Test
    @DisplayName("Buscar contactos por nombre - debe retornar coincidencias")
    void buscarContactosPorNombre_debeRetornarCoincidencias() {
        when(contactoRepository.findByNombreContainingIgnoreCaseAndActivoTrue("Juan")).thenReturn(Arrays.asList(contacto));

        List<ContactoDTO> resultado = contactoService.buscarContactosPorNombre("Juan");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(contactoRepository, times(1)).findByNombreContainingIgnoreCaseAndActivoTrue("Juan");
    }

    @Test
    @DisplayName("Buscar clientes por nombre - debe retornar solo clientes")
    void buscarClientes_debeRetornarSoloClientes() {
        when(contactoRepository.findByNombreContainingIgnoreCaseAndTipoAndActivoTrue("Juan", TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(contacto));

        List<ContactoDTO> resultado = contactoService.buscarClientes("Juan");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoContacto.CLIENTE, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("Buscar empresas por nombre - debe retornar solo empresas")
    void buscarEmpresas_debeRetornarSoloEmpresas() {
        when(contactoRepository.findByNombreContainingIgnoreCaseAndTipoAndActivoTrue("Empresa", TipoContacto.EMPRESA))
            .thenReturn(Arrays.asList(empresa));

        List<ContactoDTO> resultado = contactoService.buscarEmpresas("Empresa");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoContacto.EMPRESA, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("obtenerContactosPorTipo - con activo=true retorna solo activos")
    void obtenerContactosPorTipo_activoTrue_retornaSoloActivos() {
        when(contactoRepository.findByTipoAndActivoTrue(TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(contacto));

        List<ContactoDTO> resultado = contactoService.obtenerContactosPorTipo(TipoContacto.CLIENTE, true);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(contactoRepository, times(1)).findByTipoAndActivoTrue(TipoContacto.CLIENTE);
    }

    @Test
    @DisplayName("obtenerContactosPorTipo - con activo=false retorna solo inactivos")
    void obtenerContactosPorTipo_activoFalse_retornaSoloInactivos() {
        Contacto contactoInactivo = new Contacto();
        contactoInactivo.setContactoID(3L);
        contactoInactivo.setNombre("Inactivo");
        contactoInactivo.setTipo(TipoContacto.CLIENTE);
        contactoInactivo.setActivo(false);

        when(contactoRepository.findByTipoAndActivoFalse(TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(contactoInactivo));

        List<ContactoDTO> resultado = contactoService.obtenerContactosPorTipo(TipoContacto.CLIENTE, false);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(contactoRepository, times(1)).findByTipoAndActivoFalse(TipoContacto.CLIENTE);
    }

    @Test
    @DisplayName("obtenerContactosPorTipo - con activo=null retorna todos")
    void obtenerContactosPorTipo_activoNull_retornaTodos() {
        when(contactoRepository.findByTipo(TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(contacto, empresa));

        List<ContactoDTO> resultado = contactoService.obtenerContactosPorTipo(TipoContacto.CLIENTE, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(contactoRepository, times(1)).findByTipo(TipoContacto.CLIENTE);
    }

    @Test
    @DisplayName("obtenerEmpresas - con activo=true retorna solo empresas activas")
    void obtenerEmpresas_activoTrue_retornaSoloActivas() {
        when(contactoRepository.findByTipoAndActivoTrue(TipoContacto.EMPRESA))
            .thenReturn(Arrays.asList(empresa));

        List<ContactoDTO> resultado = contactoService.obtenerEmpresas(true);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoContacto.EMPRESA, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("obtenerEmpresas - con activo=false retorna solo empresas inactivas")
    void obtenerEmpresas_activoFalse_retornaInactivas() {
        Contacto empresaInactiva = new Contacto();
        empresaInactiva.setContactoID(4L);
        empresaInactiva.setNombre("Empresa Inactiva");
        empresaInactiva.setTipo(TipoContacto.EMPRESA);
        empresaInactiva.setActivo(false);

        when(contactoRepository.findByTipoAndActivoFalse(TipoContacto.EMPRESA))
            .thenReturn(Arrays.asList(empresaInactiva));

        List<ContactoDTO> resultado = contactoService.obtenerEmpresas(false);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("obtenerEmpresas - con activo=null retorna todas")
    void obtenerEmpresas_activoNull_retornaTodas() {
        when(contactoRepository.findByTipo(TipoContacto.EMPRESA))
            .thenReturn(Arrays.asList(empresa));

        List<ContactoDTO> resultado = contactoService.obtenerEmpresas(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("obtenerClientes - con activo=true retorna solo clientes activos")
    void obtenerClientes_activoTrue_retornaSoloActivos() {
        when(contactoRepository.findByTipoAndActivoTrue(TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(contacto));

        List<ContactoDTO> resultado = contactoService.obtenerClientes(true);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(TipoContacto.CLIENTE, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("obtenerClientes - con activo=false retorna solo clientes inactivos")
    void obtenerClientes_activoFalse_retornaInactivos() {
        Contacto clienteInactivo = new Contacto();
        clienteInactivo.setContactoID(5L);
        clienteInactivo.setNombre("Cliente Inactivo");
        clienteInactivo.setTipo(TipoContacto.CLIENTE);
        clienteInactivo.setActivo(false);

        when(contactoRepository.findByTipoAndActivoFalse(TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(clienteInactivo));

        List<ContactoDTO> resultado = contactoService.obtenerClientes(false);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("obtenerClientes - con activo=null retorna todos")
    void obtenerClientes_activoNull_retornaTodos() {
        when(contactoRepository.findByTipo(TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(contacto));

        List<ContactoDTO> resultado = contactoService.obtenerClientes(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("obtenerContactosPaginadosConFiltros - sin filtros retorna todos activos")
    void obtenerContactosPaginadosConFiltros_sinFiltros_retornaTodosActivos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contacto> contactoPage = new PageImpl<>(Arrays.asList(contacto, empresa));

        when(contactoRepository.findByActivoTrueOrderByNombreAsc(pageable)).thenReturn(contactoPage);

        Page<ContactoDTO> resultado = contactoService.obtenerContactosPaginadosConFiltros(
            pageable, null, true, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    @DisplayName("obtenerContactosPaginadosConFiltros - con tipo CLIENTE")
    void obtenerContactosPaginadosConFiltros_conTipoCliente() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contacto> contactoPage = new PageImpl<>(Arrays.asList(contacto));

        when(contactoRepository.findByTipoAndActivoTrueOrderByNombreAsc(TipoContacto.CLIENTE, pageable))
            .thenReturn(contactoPage);

        Page<ContactoDTO> resultado = contactoService.obtenerContactosPaginadosConFiltros(
            pageable, null, true, TipoContacto.CLIENTE);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("obtenerContactosPaginadosConFiltros - con tipo EMPRESA")
    void obtenerContactosPaginadosConFiltros_conTipoEmpresa() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contacto> contactoPage = new PageImpl<>(Arrays.asList(empresa));

        when(contactoRepository.findByTipoAndActivoTrueOrderByNombreAsc(TipoContacto.EMPRESA, pageable))
            .thenReturn(contactoPage);

        Page<ContactoDTO> resultado = contactoService.obtenerContactosPaginadosConFiltros(
            pageable, null, true, TipoContacto.EMPRESA);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("obtenerContactosPaginadosConFiltros - con searchTerm")
    void obtenerContactosPaginadosConFiltros_conSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10);

        when(contactoRepository.findByActivoTrue()).thenReturn(Arrays.asList(contacto, empresa));

        Page<ContactoDTO> resultado = contactoService.obtenerContactosPaginadosConFiltros(
            pageable, "Juan", true, null);

        assertNotNull(resultado);
        // Debe filtrar solo los que coinciden con "Juan"
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("obtenerContactosPaginadosConFiltros - con activo=false")
    void obtenerContactosPaginadosConFiltros_activoFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contacto> contactoPage = new PageImpl<>(Arrays.asList());

        when(contactoRepository.findByActivoFalseOrderByNombreAsc(pageable)).thenReturn(contactoPage);

        Page<ContactoDTO> resultado = contactoService.obtenerContactosPaginadosConFiltros(
            pageable, null, false, null);

        assertNotNull(resultado);
        verify(contactoRepository, times(1)).findByActivoFalseOrderByNombreAsc(pageable);
    }

    @Test
    @DisplayName("obtenerContactosPaginadosConFiltros - con activo=null retorna todos")
    void obtenerContactosPaginadosConFiltros_activoNull_retornaTodos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contacto> contactoPage = new PageImpl<>(Arrays.asList(contacto, empresa));

        when(contactoRepository.findAllByOrderByNombreAsc(pageable)).thenReturn(contactoPage);

        Page<ContactoDTO> resultado = contactoService.obtenerContactosPaginadosConFiltros(
            pageable, null, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalElements());
    }

    @Test
    @DisplayName("obtenerContactosPaginadosConFiltros - con tipo y searchTerm")
    void obtenerContactosPaginadosConFiltros_conTipoYSearchTerm() {
        Pageable pageable = PageRequest.of(0, 10);

        when(contactoRepository.findByTipoAndActivoTrue(TipoContacto.CLIENTE))
            .thenReturn(Arrays.asList(contacto));

        Page<ContactoDTO> resultado = contactoService.obtenerContactosPaginadosConFiltros(
            pageable, "Juan", true, TipoContacto.CLIENTE);

        assertNotNull(resultado);
        assertEquals(1, resultado.getContent().size());
    }
}
