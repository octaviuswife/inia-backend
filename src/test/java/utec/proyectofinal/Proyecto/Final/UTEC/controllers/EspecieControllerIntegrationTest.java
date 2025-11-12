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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.EspecieRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.EspecieService;

@WebMvcTest(EspecieController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para EspecieController")
class EspecieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EspecieService especieService;

    @Autowired
    private ObjectMapper objectMapper;

    private EspecieRequestDTO especieRequest;
    private EspecieDTO especieResponse;

    @BeforeEach
    void setUp() {
        especieRequest = new EspecieRequestDTO();
        especieRequest.setNombreComun("Trigo");
        especieRequest.setNombreCientifico("Triticum aestivum");

        especieResponse = new EspecieDTO();
        especieResponse.setEspecieID(1L);
        especieResponse.setNombreComun("Trigo");
        especieResponse.setNombreCientifico("Triticum aestivum");
        especieResponse.setActivo(true);
    }

    @Test
    @DisplayName("POST /api/especie - Debe crear una especie exitosamente")
    @WithMockUser(roles = "ADMIN")
    void crearEspecie_conDatosValidos_debeRetornarCreado() throws Exception {
        when(especieService.crear(any(EspecieRequestDTO.class))).thenReturn(especieResponse);

        mockMvc.perform(post("/api/especie")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especieID").value(1L))
                .andExpect(jsonPath("$.nombreComun").value("Trigo"))
                .andExpect(jsonPath("$.nombreCientifico").value("Triticum aestivum"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("GET /api/especie - Debe listar todas las especies")
    @WithMockUser(roles = "ANALISTA")
    void obtenerTodas_debeRetornarListaDeEspecies() throws Exception {
        EspecieDTO especie2 = new EspecieDTO();
        especie2.setEspecieID(2L);
        especie2.setNombreComun("Maíz");
        especie2.setNombreCientifico("Zea mays");
        especie2.setActivo(true);

        List<EspecieDTO> especies = Arrays.asList(especieResponse, especie2);
        when(especieService.obtenerTodas(null)).thenReturn(especies);

        mockMvc.perform(get("/api/especie")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].especieID").value(1L))
                .andExpect(jsonPath("$[1].especieID").value(2L));
    }

    @Test
    @DisplayName("GET /api/especie/{id} - Debe obtener especie por ID")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdExistente_debeRetornarEspecie() throws Exception {
        when(especieService.obtenerPorId(1L)).thenReturn(especieResponse);

        mockMvc.perform(get("/api/especie/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especieID").value(1L))
                .andExpect(jsonPath("$.nombreComun").value("Trigo"));
    }

    @Test
    @DisplayName("GET /api/especie/{id} - Debe retornar 404 cuando no existe")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorId_conIdInexistente_debeRetornar404() throws Exception {
        when(especieService.obtenerPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/api/especie/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/especie/buscar/comun - Debe buscar por nombre común")
    @WithMockUser(roles = "ANALISTA")
    void buscarPorNombreComun_debeRetornarEspecies() throws Exception {
        List<EspecieDTO> especies = Arrays.asList(especieResponse);
        when(especieService.buscarPorNombreComun("Trigo")).thenReturn(especies);

        mockMvc.perform(get("/api/especie/buscar/comun")
                .param("nombre", "Trigo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreComun").value("Trigo"));
    }

    @Test
    @DisplayName("GET /api/especie/buscar/cientifico - Debe buscar por nombre científico")
    @WithMockUser(roles = "ANALISTA")
    void buscarPorNombreCientifico_debeRetornarEspecies() throws Exception {
        List<EspecieDTO> especies = Arrays.asList(especieResponse);
        when(especieService.buscarPorNombreCientifico("Triticum")).thenReturn(especies);

        mockMvc.perform(get("/api/especie/buscar/cientifico")
                .param("nombre", "Triticum")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreCientifico").value("Triticum aestivum"));
    }

    @Test
    @DisplayName("GET /api/especie/inactivas - Debe listar especies inactivas")
    @WithMockUser(roles = "ADMIN")
    void obtenerInactivas_debeRetornarEspeciesInactivas() throws Exception {
        especieResponse.setActivo(false);
        List<EspecieDTO> especies = Arrays.asList(especieResponse);
        when(especieService.obtenerInactivas()).thenReturn(especies);

        mockMvc.perform(get("/api/especie/inactivas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    @DisplayName("PUT /api/especie/{id} - Debe actualizar una especie existente")
    @WithMockUser(roles = "ADMIN")
    void actualizarEspecie_conDatosValidos_debeRetornarActualizado() throws Exception {
        especieResponse.setNombreComun("Trigo Actualizado");
        when(especieService.actualizar(eq(1L), any(EspecieRequestDTO.class))).thenReturn(especieResponse);

        especieRequest.setNombreComun("Trigo Actualizado");

        mockMvc.perform(put("/api/especie/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especieID").value(1L))
                .andExpect(jsonPath("$.nombreComun").value("Trigo Actualizado"));
    }

    @Test
    @DisplayName("PUT /api/especie/{id} - Debe retornar 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void actualizarEspecie_conIdInexistente_debeRetornar404() throws Exception {
        when(especieService.actualizar(eq(999L), any(EspecieRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/especie/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especieRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/especie/{id}/reactivar - Debe reactivar una especie")
    @WithMockUser(roles = "ADMIN")
    void reactivarEspecie_conIdValido_debeRetornarReactivado() throws Exception {
        especieResponse.setActivo(true);
        when(especieService.reactivar(1L)).thenReturn(especieResponse);

        mockMvc.perform(put("/api/especie/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("DELETE /api/especie/{id} - Debe desactivar una especie")
    @WithMockUser(roles = "ADMIN")
    void eliminarEspecie_conIdValido_debeDesactivar() throws Exception {
        doNothing().when(especieService).eliminar(1L);

        mockMvc.perform(delete("/api/especie/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/especie/listado - Debe obtener especies paginadas")
    @WithMockUser(roles = "ANALISTA")
    void obtenerEspeciesPaginadas_debeRetornarPaginacion() throws Exception {
        Page<EspecieDTO> page = new PageImpl<>(Arrays.asList(especieResponse));
        when(especieService.obtenerEspeciesPaginadasConFiltros(any(Pageable.class), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/especie/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].especieID").value(1L));
    }

    @Test
    @DisplayName("GET /api/especie/listado - Debe filtrar con parámetros de búsqueda")
    @WithMockUser(roles = "ANALISTA")
    void obtenerEspeciesPaginadas_conFiltros_debeRetornarFiltrado() throws Exception {
        Page<EspecieDTO> page = new PageImpl<>(Arrays.asList(especieResponse));
        when(especieService.obtenerEspeciesPaginadasConFiltros(any(Pageable.class), eq("Trigo"), eq(true)))
            .thenReturn(page);

        mockMvc.perform(get("/api/especie/listado")
                .param("page", "0")
                .param("size", "10")
                .param("search", "Trigo")
                .param("activo", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}
