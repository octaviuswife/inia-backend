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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.CatalogoService;

@WebMvcTest(CatalogoController.class)
@DisplayName("Tests de integración para CatalogoController")
class CatalogoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogoService catalogoService;

    @Autowired
    private ObjectMapper objectMapper;

    private CatalogoRequestDTO catalogoRequest;
    private CatalogoDTO catalogoResponse;

    @BeforeEach
    void setUp() {
        catalogoRequest = new CatalogoRequestDTO();
        catalogoRequest.setTipo("DEPOSITO");
        catalogoRequest.setValor("Depósito Central");

        catalogoResponse = new CatalogoDTO();
        catalogoResponse.setId(1L);
        catalogoResponse.setTipo("DEPOSITO");
        catalogoResponse.setValor("Depósito Central");
        catalogoResponse.setActivo(true);
    }

    @Test
    @DisplayName("POST /api/catalogo - Debe crear un catálogo exitosamente")
    @WithMockUser(roles = "ADMIN")
    void crearCatalogo_conDatosValidos_debeRetornarCreado() throws Exception {
        when(catalogoService.crear(any(CatalogoRequestDTO.class))).thenReturn(catalogoResponse);

        mockMvc.perform(post("/api/catalogo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.valor").value("Depósito Central"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("GET /api/catalogo - Debe listar todos los catálogos")
    @WithMockUser(roles = "ANALISTA")
    void obtenerTodos_debeRetornarListaDeCatalogos() throws Exception {
        CatalogoDTO catalogo2 = new CatalogoDTO();
        catalogo2.setId(2L);
        catalogo2.setTipo("ORIGEN");
        catalogo2.setValor("Nacional");
        catalogo2.setActivo(true);

        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse, catalogo2);
        when(catalogoService.obtenerTodos()).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("GET /api/catalogo/{id} - Debe obtener catálogo por ID")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdExistente_debeRetornarCatalogo() throws Exception {
        when(catalogoService.obtenerPorId(1L)).thenReturn(catalogoResponse);

        mockMvc.perform(get("/api/catalogo/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.valor").value("Depósito Central"));
    }

    @Test
    @DisplayName("GET /api/catalogo/tipo/{tipo} - Debe filtrar catálogos por tipo")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorTipo_conTipoValido_debeRetornarCatalogosFiltrados() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("DEPOSITO", null)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/tipo/DEPOSITO")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("DEPOSITO"));
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id} - Debe actualizar un catálogo existente")
    @WithMockUser(roles = "ADMIN")
    void actualizarCatalogo_conDatosValidos_debeRetornarActualizado() throws Exception {
        catalogoResponse.setValor("Depósito Actualizado");
        when(catalogoService.actualizar(eq(1L), any(CatalogoRequestDTO.class))).thenReturn(catalogoResponse);

        catalogoRequest.setValor("Depósito Actualizado");

        mockMvc.perform(put("/api/catalogo/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.valor").value("Depósito Actualizado"));
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id}/reactivar - Debe reactivar un catálogo")
    @WithMockUser(roles = "ADMIN")
    void reactivarCatalogo_conIdValido_debeRetornarReactivado() throws Exception {
        catalogoResponse.setActivo(true);
        when(catalogoService.reactivar(1L)).thenReturn(catalogoResponse);

        mockMvc.perform(put("/api/catalogo/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("DELETE /api/catalogo/{id} - Debe desactivar un catálogo")
    @WithMockUser(roles = "ADMIN")
    void eliminarCatalogo_conIdValido_debeDesactivar() throws Exception {
        doNothing().when(catalogoService).eliminar(1L);

        mockMvc.perform(delete("/api/catalogo/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
