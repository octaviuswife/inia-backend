package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
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
}
