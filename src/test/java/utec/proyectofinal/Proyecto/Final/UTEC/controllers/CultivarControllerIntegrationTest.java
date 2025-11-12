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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CultivarRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CultivarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.CultivarService;

@WebMvcTest(CultivarController.class)
@DisplayName("Tests de integración para CultivarController")
class CultivarControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CultivarService cultivarService;

    @Autowired
    private ObjectMapper objectMapper;

    private CultivarRequestDTO cultivarRequest;
    private CultivarDTO cultivarResponse;

    @BeforeEach
    void setUp() {
        cultivarRequest = new CultivarRequestDTO();
        cultivarRequest.setEspecieID(1L);
        cultivarRequest.setNombre("Cultivar Test");

        cultivarResponse = new CultivarDTO();
        cultivarResponse.setCultivarID(1L);
        cultivarResponse.setEspecieID(1L);
        cultivarResponse.setEspecieNombre("Triticum aestivum");
        cultivarResponse.setNombre("Cultivar Test");
        cultivarResponse.setActivo(true);
    }

    @Test
    @DisplayName("POST /api/cultivar - Debe crear un cultivar exitosamente")
    @WithMockUser(roles = "ADMIN")
    void crearCultivar_conDatosValidos_debeRetornarCreado() throws Exception {
        when(cultivarService.crear(any(CultivarRequestDTO.class))).thenReturn(cultivarResponse);

        mockMvc.perform(post("/api/cultivar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cultivarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cultivarID").value(1L))
                .andExpect(jsonPath("$.especieID").value(1L))
                .andExpect(jsonPath("$.nombre").value("Cultivar Test"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("GET /api/cultivar - Debe listar todos los cultivares")
    @WithMockUser(roles = "ANALISTA")
    void obtenerTodos_debeRetornarListaDeCultivares() throws Exception {
        CultivarDTO cultivar2 = new CultivarDTO();
        cultivar2.setCultivarID(2L);
        cultivar2.setEspecieID(1L);
        cultivar2.setEspecieNombre("Triticum aestivum");
        cultivar2.setNombre("Cultivar Test 2");
        cultivar2.setActivo(true);

        List<CultivarDTO> cultivares = Arrays.asList(cultivarResponse, cultivar2);
        when(cultivarService.obtenerTodos(null)).thenReturn(cultivares);

        mockMvc.perform(get("/api/cultivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cultivarID").value(1L))
                .andExpect(jsonPath("$[1].cultivarID").value(2L));
    }

    @Test
    @DisplayName("GET /api/cultivar/{id} - Debe obtener cultivar por ID")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdExistente_debeRetornarCultivar() throws Exception {
        when(cultivarService.obtenerPorId(1L)).thenReturn(cultivarResponse);

        mockMvc.perform(get("/api/cultivar/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cultivarID").value(1L))
                .andExpect(jsonPath("$.nombre").value("Cultivar Test"));
    }

    @Test
    @DisplayName("GET /api/cultivar/especie/{especieId} - Debe filtrar cultivares por especie")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorEspecie_conEspecieValida_debeRetornarCultivaresFiltrados() throws Exception {
        List<CultivarDTO> cultivares = Arrays.asList(cultivarResponse);
        when(cultivarService.obtenerPorEspecie(1L)).thenReturn(cultivares);

        mockMvc.perform(get("/api/cultivar/especie/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].especieID").value(1L));
    }

    @Test
    @DisplayName("GET /api/cultivar/buscar - Debe buscar cultivares por nombre")
    @WithMockUser(roles = "ANALISTA")
    void buscarPorNombre_conNombreValido_debeRetornarCultivares() throws Exception {
        List<CultivarDTO> cultivares = Arrays.asList(cultivarResponse);
        when(cultivarService.buscarPorNombre("Test")).thenReturn(cultivares);

        mockMvc.perform(get("/api/cultivar/buscar")
                .param("nombre", "Test")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Cultivar Test"));
    }

    @Test
    @DisplayName("PUT /api/cultivar/{id} - Debe actualizar un cultivar existente")
    @WithMockUser(roles = "ADMIN")
    void actualizarCultivar_conDatosValidos_debeRetornarActualizado() throws Exception {
        cultivarResponse.setNombre("Cultivar Actualizado");
        when(cultivarService.actualizar(eq(1L), any(CultivarRequestDTO.class))).thenReturn(cultivarResponse);

        cultivarRequest.setNombre("Cultivar Actualizado");

        mockMvc.perform(put("/api/cultivar/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cultivarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cultivarID").value(1L))
                .andExpect(jsonPath("$.nombre").value("Cultivar Actualizado"));
    }

    @Test
    @DisplayName("PUT /api/cultivar/{id}/reactivar - Debe reactivar un cultivar")
    @WithMockUser(roles = "ADMIN")
    void reactivarCultivar_conIdValido_debeRetornarReactivado() throws Exception {
        cultivarResponse.setActivo(true);
        when(cultivarService.reactivar(1L)).thenReturn(cultivarResponse);

        mockMvc.perform(put("/api/cultivar/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("DELETE /api/cultivar/{id} - Debe desactivar un cultivar")
    @WithMockUser(roles = "ADMIN")
    void eliminarCultivar_conIdValido_debeDesactivar() throws Exception {
        doNothing().when(cultivarService).eliminar(1L);

        mockMvc.perform(delete("/api/cultivar/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/cultivar/inactivos - Debe listar cultivares inactivos")
    @WithMockUser(roles = "ADMIN")
    void obtenerInactivos_debeRetornarListaInactivos() throws Exception {
    CultivarDTO inactivo = new CultivarDTO();
    inactivo.setCultivarID(3L);
    inactivo.setNombre("Cultivar Inactivo");
    inactivo.setActivo(false);

    when(cultivarService.obtenerInactivos()).thenReturn(Arrays.asList(inactivo));

    mockMvc.perform(get("/api/cultivar/inactivos")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    @DisplayName("GET /api/cultivar/{id} - Debe retornar 404 cuando no existe")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdNoExistente_debeRetornarNotFound() throws Exception {
    when(cultivarService.obtenerPorId(2L)).thenReturn(null);

    mockMvc.perform(get("/api/cultivar/2")
        .with(csrf()))
        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/cultivar - Debe devolver BadRequest cuando el servicio falla")
    @WithMockUser(roles = "ADMIN")
    void crearCultivar_servicioFalla_debeRetornarBadRequest() throws Exception {
    when(cultivarService.crear(any(CultivarRequestDTO.class)))
        .thenThrow(new RuntimeException("Error al crear"));

    mockMvc.perform(post("/api/cultivar")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cultivarRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/cultivar/{id} - Debe retornar 404 cuando actualizar no encuentra")
    @WithMockUser(roles = "ADMIN")
    void actualizarCultivar_noExiste_debeRetornarNotFound() throws Exception {
    when(cultivarService.actualizar(eq(2L), any(CultivarRequestDTO.class))).thenReturn(null);

    mockMvc.perform(put("/api/cultivar/2")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cultivarRequest)))
        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/cultivar/{id} - Debe retornar BadRequest cuando el servicio lanza RuntimeException")
    @WithMockUser(roles = "ADMIN")
    void actualizarCultivar_servicioFalla_debeRetornarBadRequest() throws Exception {
    when(cultivarService.actualizar(eq(1L), any(CultivarRequestDTO.class)))
        .thenThrow(new RuntimeException("Error actualizar"));

    mockMvc.perform(put("/api/cultivar/1")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(cultivarRequest)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/cultivar/{id}/reactivar - Debe devolver 404 cuando reactivar no existe")
    @WithMockUser(roles = "ADMIN")
    void reactivarCultivar_noExiste_debeRetornarNotFound() throws Exception {
    when(cultivarService.reactivar(2L)).thenReturn(null);

    mockMvc.perform(put("/api/cultivar/2/reactivar")
        .with(csrf()))
        .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/cultivar/listado - Debe retornar paginado")
    @WithMockUser(roles = "ANALISTA")
    void obtenerCultivaresPaginados_debeRetornarPagina() throws Exception {
    List<CultivarDTO> lista = Arrays.asList(cultivarResponse);
    PageImpl<CultivarDTO> page = new PageImpl<>(lista, PageRequest.of(0, 10), lista.size());

    when(cultivarService.obtenerCultivaresPaginadosConFiltros(any(), eq("search"), eq(true)))
        .thenReturn(page);

    mockMvc.perform(get("/api/cultivar/listado")
        .param("page", "0")
        .param("size", "10")
        .param("search", "search")
        .param("activo", "true")
        .with(csrf()))
        .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
}
