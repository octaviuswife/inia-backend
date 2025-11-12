package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ContactoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ContactoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ContactoService;


@WebMvcTest(ContactoController.class)
@DisplayName("Tests de integración para ContactoController")
class ContactoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactoService contactoService;

    @Autowired
    private ObjectMapper objectMapper;

    private ContactoRequestDTO contactoRequest;
    private ContactoDTO contactoResponse;

    @BeforeEach
    void setUp() {
        contactoRequest = new ContactoRequestDTO();
        contactoRequest.setNombre("Empresa Test S.A.");
        contactoRequest.setContacto("contacto@empresa.com");
        contactoRequest.setTipo(TipoContacto.EMPRESA);

        contactoResponse = new ContactoDTO();
        contactoResponse.setContactoID(1L);
        contactoResponse.setNombre("Empresa Test S.A.");
        contactoResponse.setContacto("contacto@empresa.com");
        contactoResponse.setTipo(TipoContacto.EMPRESA);
        contactoResponse.setActivo(true);
    }

    @Test
    @DisplayName("POST /api/contactos - Debe crear un contacto exitosamente")
    @WithMockUser(roles = "ADMIN")
    void crearContacto_conDatosValidos_debeRetornarCreado() throws Exception {
        when(contactoService.crearContacto(any(ContactoRequestDTO.class))).thenReturn(contactoResponse);

        mockMvc.perform(post("/api/contactos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contactoID").value(1L))
                .andExpect(jsonPath("$.nombre").value("Empresa Test S.A."))
                .andExpect(jsonPath("$.contacto").value("contacto@empresa.com"))
                .andExpect(jsonPath("$.tipo").value("EMPRESA"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("GET /api/contactos - Debe listar todos los contactos")
    @WithMockUser(roles = "ANALISTA")
    void obtenerTodos_debeRetornarListaDeContactos() throws Exception {
        ContactoDTO contacto2 = new ContactoDTO();
        contacto2.setContactoID(2L);
        contacto2.setNombre("Cliente Test");
        contacto2.setContacto("cliente@test.com");
        contacto2.setTipo(TipoContacto.CLIENTE);
        contacto2.setActivo(true);

        List<ContactoDTO> contactos = Arrays.asList(contactoResponse, contacto2);
        when(contactoService.obtenerTodosLosContactos()).thenReturn(contactos);

        mockMvc.perform(get("/api/contactos")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].contactoID").value(1L))
                .andExpect(jsonPath("$[1].contactoID").value(2L));
    }

    @Test
    @DisplayName("GET /api/contactos/{id} - Debe obtener contacto por ID")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdExistente_debeRetornarContacto() throws Exception {
        when(contactoService.obtenerContactoPorId(1L)).thenReturn(contactoResponse);

        mockMvc.perform(get("/api/contactos/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactoID").value(1L))
                .andExpect(jsonPath("$.nombre").value("Empresa Test S.A."));
    }

    @Test
    @DisplayName("GET /api/contactos/empresas - Debe filtrar contactos por tipo EMPRESA")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorTipo_conTipoEmpresa_debeRetornarContactosFiltrados() throws Exception {
        List<ContactoDTO> contactos = Arrays.asList(contactoResponse);
        when(contactoService.obtenerEmpresas(null)).thenReturn(contactos);

        mockMvc.perform(get("/api/contactos/empresas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("EMPRESA"));
    }

    @Test
    @DisplayName("GET /api/contactos/buscar - Debe buscar contactos por nombre")
    @WithMockUser(roles = "ANALISTA")
    void buscarPorNombre_conNombreValido_debeRetornarContactos() throws Exception {
        List<ContactoDTO> contactos = Arrays.asList(contactoResponse);
        when(contactoService.buscarContactosPorNombre("Empresa")).thenReturn(contactos);

        mockMvc.perform(get("/api/contactos/buscar")
                .param("nombre", "Empresa")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Empresa Test S.A."));
    }

    @Test
    @DisplayName("PUT /api/contactos/{id} - Debe actualizar un contacto existente")
    @WithMockUser(roles = "ADMIN")
    void actualizarContacto_conDatosValidos_debeRetornarActualizado() throws Exception {
        contactoResponse.setNombre("Empresa Actualizada S.A.");
        when(contactoService.actualizarContacto(eq(1L), any(ContactoRequestDTO.class))).thenReturn(contactoResponse);

        contactoRequest.setNombre("Empresa Actualizada S.A.");

        mockMvc.perform(put("/api/contactos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactoID").value(1L))
                .andExpect(jsonPath("$.nombre").value("Empresa Actualizada S.A."));
    }

    @Test
    @DisplayName("PUT /api/contactos/{id}/reactivar - Debe reactivar un contacto")
    @WithMockUser(roles = "ADMIN")
    void reactivarContacto_conIdValido_debeRetornarReactivado() throws Exception {
        contactoResponse.setActivo(true);
        when(contactoService.reactivarContacto(1L)).thenReturn(contactoResponse);

        mockMvc.perform(put("/api/contactos/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("DELETE /api/contactos/{id} - Debe desactivar un contacto")
    @WithMockUser(roles = "ADMIN")
    void eliminarContacto_conIdValido_debeDesactivar() throws Exception {
        doNothing().when(contactoService).eliminarContacto(1L);

        mockMvc.perform(delete("/api/contactos/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/contactos - Debe manejar error interno")
    @WithMockUser(roles = "ANALISTA")
    void obtenerTodos_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.obtenerTodosLosContactos()).thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(get("/api/contactos")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/contactos/clientes - Debe obtener solo clientes activos")
    @WithMockUser(roles = "ANALISTA")
    void obtenerClientes_sinFiltro_debeRetornarClientes() throws Exception {
        ContactoDTO cliente = new ContactoDTO();
        cliente.setContactoID(3L);
        cliente.setNombre("Cliente Test");
        cliente.setContacto("cliente@test.com");
        cliente.setTipo(TipoContacto.CLIENTE);
        cliente.setActivo(true);

        when(contactoService.obtenerClientes(null)).thenReturn(Arrays.asList(cliente));

        mockMvc.perform(get("/api/contactos/clientes")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("CLIENTE"));
    }

    @Test
    @DisplayName("GET /api/contactos/clientes - Debe manejar error interno")
    @WithMockUser(roles = "ANALISTA")
    void obtenerClientes_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.obtenerClientes(null)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/contactos/clientes")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/contactos/clientes - Con filtro activo=true")
    @WithMockUser(roles = "ANALISTA")
    void obtenerClientes_conFiltroActivo_debeRetornarClientesActivos() throws Exception {
        ContactoDTO cliente = new ContactoDTO();
        cliente.setContactoID(3L);
        cliente.setNombre("Cliente Activo");
        cliente.setActivo(true);

        when(contactoService.obtenerClientes(true)).thenReturn(Arrays.asList(cliente));

        mockMvc.perform(get("/api/contactos/clientes")
                .param("activo", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @DisplayName("GET /api/contactos/empresas - Debe manejar error interno")
    @WithMockUser(roles = "ANALISTA")
    void obtenerEmpresas_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.obtenerEmpresas(null)).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/contactos/empresas")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/contactos/empresas - Con filtro activo=false")
    @WithMockUser(roles = "ADMIN")
    void obtenerEmpresas_conFiltroInactivo_debeRetornarEmpresasInactivas() throws Exception {
        ContactoDTO empresa = new ContactoDTO();
        empresa.setContactoID(4L);
        empresa.setNombre("Empresa Inactiva");
        empresa.setActivo(false);

        when(contactoService.obtenerEmpresas(false)).thenReturn(Arrays.asList(empresa));

        mockMvc.perform(get("/api/contactos/empresas")
                .param("activo", "false")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    @DisplayName("GET /api/contactos/{id} - Debe retornar 404 si no existe")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdInexistente_debeRetornar404() throws Exception {
        when(contactoService.obtenerContactoPorId(999L)).thenThrow(new RuntimeException("Contacto no encontrado"));

        mockMvc.perform(get("/api/contactos/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/contactos/{id} - Debe manejar error interno")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.obtenerContactoPorId(1L)).thenThrow(new IllegalStateException("Error interno"));

        mockMvc.perform(get("/api/contactos/1")
                .with(csrf()))
                .andExpect(status().isNotFound()); // RuntimeException se mapea a 404
    }

    @Test
    @DisplayName("POST /api/contactos - Debe retornar 400 con datos inválidos")
    @WithMockUser(roles = "ADMIN")
    void crearContacto_conDatosInvalidos_debeRetornar400() throws Exception {
        when(contactoService.crearContacto(any(ContactoRequestDTO.class)))
                .thenThrow(new RuntimeException("El nombre del contacto es requerido"));

        mockMvc.perform(post("/api/contactos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/contactos - Debe manejar error interno")
    @WithMockUser(roles = "ADMIN")
    void crearContacto_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.crearContacto(any(ContactoRequestDTO.class)))
                .thenThrow(new IllegalStateException("Error de base de datos"));

        mockMvc.perform(post("/api/contactos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactoRequest)))
                .andExpect(status().isBadRequest()); // RuntimeException se mapea a badRequest con mensaje
    }

    @Test
    @DisplayName("PUT /api/contactos/{id} - Debe retornar 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void actualizarContacto_conIdInexistente_debeRetornar404() throws Exception {
        when(contactoService.actualizarContacto(eq(999L), any(ContactoRequestDTO.class)))
                .thenThrow(new RuntimeException("Contacto no encontrado"));

        mockMvc.perform(put("/api/contactos/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactoRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/contactos/{id} - Debe retornar 400 con datos inválidos")
    @WithMockUser(roles = "ADMIN")
    void actualizarContacto_conDatosInvalidos_debeRetornar400() throws Exception {
        when(contactoService.actualizarContacto(eq(1L), any(ContactoRequestDTO.class)))
                .thenThrow(new RuntimeException("Ya existe un cliente con ese nombre"));

        mockMvc.perform(put("/api/contactos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/contactos/{id} - Debe manejar error interno")
    @WithMockUser(roles = "ADMIN")
    void actualizarContacto_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.actualizarContacto(eq(1L), any(ContactoRequestDTO.class)))
                .thenThrow(new IllegalStateException("Error de base de datos"));

        mockMvc.perform(put("/api/contactos/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactoRequest)))
                .andExpect(status().isBadRequest()); // RuntimeException se mapea a badRequest
    }

    @Test
    @DisplayName("DELETE /api/contactos/{id} - Debe retornar 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void eliminarContacto_conIdInexistente_debeRetornar404() throws Exception {
        org.mockito.Mockito.doThrow(new RuntimeException("Contacto no encontrado"))
                .when(contactoService).eliminarContacto(999L);

        mockMvc.perform(delete("/api/contactos/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/contactos/{id} - Debe manejar error interno")
    @WithMockUser(roles = "ADMIN")
    void eliminarContacto_conErrorInterno_debeRetornar500() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("Error de base de datos"))
                .when(contactoService).eliminarContacto(1L);

        mockMvc.perform(delete("/api/contactos/1")
                .with(csrf()))
                .andExpect(status().isNotFound()); // RuntimeException se mapea a 404
    }

    @Test
    @DisplayName("PUT /api/contactos/{id}/reactivar - Debe retornar 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void reactivarContacto_conIdInexistente_debeRetornar404() throws Exception {
        when(contactoService.reactivarContacto(999L))
                .thenThrow(new RuntimeException("Contacto no encontrado"));

        mockMvc.perform(put("/api/contactos/999/reactivar")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/contactos/{id}/reactivar - Debe retornar 400 si ya está activo")
    @WithMockUser(roles = "ADMIN")
    void reactivarContacto_yaActivo_debeRetornar400() throws Exception {
        when(contactoService.reactivarContacto(1L))
                .thenThrow(new RuntimeException("El contacto ya está activo"));

        mockMvc.perform(put("/api/contactos/1/reactivar")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/contactos/{id}/reactivar - Debe manejar error interno")
    @WithMockUser(roles = "ADMIN")
    void reactivarContacto_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.reactivarContacto(1L))
                .thenThrow(new IllegalStateException("Error de base de datos"));

        mockMvc.perform(put("/api/contactos/1/reactivar")
                .with(csrf()))
                .andExpect(status().isBadRequest()); // RuntimeException se mapea a badRequest
    }

    @Test
    @DisplayName("GET /api/contactos/buscar - Debe manejar error interno")
    @WithMockUser(roles = "ANALISTA")
    void buscarPorNombre_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.buscarContactosPorNombre(anyString()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/contactos/buscar")
                .param("nombre", "Test")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/contactos/clientes/buscar - Debe buscar clientes por nombre")
    @WithMockUser(roles = "ANALISTA")
    void buscarClientes_conNombre_debeRetornarClientes() throws Exception {
        ContactoDTO cliente = new ContactoDTO();
        cliente.setContactoID(3L);
        cliente.setNombre("Cliente Encontrado");
        cliente.setTipo(TipoContacto.CLIENTE);

        when(contactoService.buscarClientes("Cliente")).thenReturn(Arrays.asList(cliente));

        mockMvc.perform(get("/api/contactos/clientes/buscar")
                .param("nombre", "Cliente")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("CLIENTE"));
    }

    @Test
    @DisplayName("GET /api/contactos/clientes/buscar - Debe manejar error interno")
    @WithMockUser(roles = "ANALISTA")
    void buscarClientes_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.buscarClientes(anyString()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/contactos/clientes/buscar")
                .param("nombre", "Test")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/contactos/empresas/buscar - Debe buscar empresas por nombre")
    @WithMockUser(roles = "ANALISTA")
    void buscarEmpresas_conNombre_debeRetornarEmpresas() throws Exception {
        when(contactoService.buscarEmpresas("Empresa")).thenReturn(Arrays.asList(contactoResponse));

        mockMvc.perform(get("/api/contactos/empresas/buscar")
                .param("nombre", "Empresa")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("EMPRESA"));
    }

    @Test
    @DisplayName("GET /api/contactos/empresas/buscar - Debe manejar error interno")
    @WithMockUser(roles = "ANALISTA")
    void buscarEmpresas_conErrorInterno_debeRetornar500() throws Exception {
        when(contactoService.buscarEmpresas(anyString()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/contactos/empresas/buscar")
                .param("nombre", "Test")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/contactos/listado - Debe obtener contactos paginados sin filtros")
    @WithMockUser(roles = "ANALISTA")
    void obtenerContactosPaginados_sinFiltros_debeRetornarPagina() throws Exception {
        org.springframework.data.domain.Page<ContactoDTO> page = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(contactoResponse));
        
        when(contactoService.obtenerContactosPaginadosConFiltros(
                any(org.springframework.data.domain.Pageable.class), 
                eq(null), 
                eq(null), 
                eq(null)))
            .thenReturn(page);

        mockMvc.perform(get("/api/contactos/listado")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/contactos/listado - Con búsqueda y filtros")
    @WithMockUser(roles = "ADMIN")
    void obtenerContactosPaginados_conFiltros_debeRetornarPaginaFiltrada() throws Exception {
        org.springframework.data.domain.Page<ContactoDTO> page = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(contactoResponse));
        
        when(contactoService.obtenerContactosPaginadosConFiltros(
                any(org.springframework.data.domain.Pageable.class), 
                eq("Empresa"), 
                eq(true), 
                eq(TipoContacto.EMPRESA)))
            .thenReturn(page);

        mockMvc.perform(get("/api/contactos/listado")
                .param("page", "0")
                .param("size", "10")
                .param("search", "Empresa")
                .param("activo", "true")
                .param("tipo", "EMPRESA")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipo").value("EMPRESA"));
    }

    @Test
    @DisplayName("GET /api/contactos/listado - Con tipo inválido debe ignorar filtro")
    @WithMockUser(roles = "ANALISTA")
    void obtenerContactosPaginados_conTipoInvalido_debeIgnorarFiltro() throws Exception {
        org.springframework.data.domain.Page<ContactoDTO> page = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(contactoResponse));
        
        when(contactoService.obtenerContactosPaginadosConFiltros(
                any(org.springframework.data.domain.Pageable.class), 
                eq(null), 
                eq(null), 
                eq(null)))
            .thenReturn(page);

        mockMvc.perform(get("/api/contactos/listado")
                .param("tipo", "TIPO_INVALIDO")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/contactos/listado - Con tipo vacío debe ignorar filtro")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerContactosPaginados_conTipoVacio_debeIgnorarFiltro() throws Exception {
        org.springframework.data.domain.Page<ContactoDTO> page = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(contactoResponse));
        
        when(contactoService.obtenerContactosPaginadosConFiltros(
                any(org.springframework.data.domain.Pageable.class), 
                eq(null), 
                eq(null), 
                eq(null)))
            .thenReturn(page);

        mockMvc.perform(get("/api/contactos/listado")
                .param("tipo", "")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
