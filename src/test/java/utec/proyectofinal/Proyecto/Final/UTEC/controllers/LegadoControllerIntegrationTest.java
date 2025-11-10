package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.LegadoService;

@WebMvcTest(LegadoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para LegadoController")
class LegadoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LegadoService legadoService;

    private LegadoSimpleDTO legadoSimple;
    private LegadoDTO legado;
    private LegadoListadoDTO legadoListado;

    @BeforeEach
    void setUp() {
        legadoSimple = new LegadoSimpleDTO();
        legadoSimple.setLegadoID(1L);

        legado = new LegadoDTO();
        legado.setLegadoID(1L);
        legado.setArchivoOrigen("legado.xlsx");

        legadoListado = new LegadoListadoDTO();
        legadoListado.setLegadoID(1L);
    }

    @Test
    @DisplayName("GET /api/legados - Debe listar todos los registros legados activos")
    @WithMockUser(roles = "ADMIN")
    void obtenerTodosSimple_debeRetornarLista() throws Exception {
        List<LegadoSimpleDTO> legados = Arrays.asList(legadoSimple);

        when(legadoService.obtenerTodosSimple()).thenReturn(legados);

        mockMvc.perform(get("/api/legados")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].legadoID").value(1));
    }

    @Test
    @DisplayName("GET /api/legados/listado - Debe soportar paginación con filtros")
    @WithMockUser(roles = "ANALISTA")
    void obtenerLegadosPaginadas_conFiltros_debeRetornarPagina() throws Exception {
        List<LegadoListadoDTO> legados = Arrays.asList(legadoListado);
        Page<LegadoListadoDTO> page = new PageImpl<>(legados);

        when(legadoService.obtenerLegadosPaginadas(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/legados/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].legadoID").value(1));
    }

    @Test
    @DisplayName("GET /api/legados/{id} - Debe obtener legado por ID")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdValido_debeRetornarLegado() throws Exception {
        when(legadoService.obtenerPorId(1L)).thenReturn(legado);

        mockMvc.perform(get("/api/legados/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legadoID").value(1))
                .andExpect(jsonPath("$.archivoOrigen").value("legado.xlsx"));
    }

    @Test
    @DisplayName("GET /api/legados/{id} - Debe retornar 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void obtenerPorId_conIdInexistente_debeRetornar404() throws Exception {
        when(legadoService.obtenerPorId(anyLong())).thenThrow(new RuntimeException("Legado no encontrado"));

        mockMvc.perform(get("/api/legados/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/legados/archivo/{archivoOrigen} - Debe filtrar por archivo origen")
    @WithMockUser(roles = "ADMIN")
    void obtenerPorArchivo_conArchivoValido_debeRetornarLista() throws Exception {
        List<LegadoSimpleDTO> legados = Arrays.asList(legadoSimple);

        when(legadoService.obtenerPorArchivo(anyString())).thenReturn(legados);

        mockMvc.perform(get("/api/legados/archivo/legado.xlsx")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].legadoID").value(1));
    }

    @Test
    @DisplayName("GET /api/legados/ficha/{ficha} - Debe buscar por número de ficha")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorFicha_conFichaValida_debeRetornarLista() throws Exception {
        List<LegadoSimpleDTO> legados = Arrays.asList(legadoSimple);

        when(legadoService.obtenerPorFicha(anyString())).thenReturn(legados);

        mockMvc.perform(get("/api/legados/ficha/F-2023-001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].legadoID").value(1));
    }

    @Test
    @DisplayName("GET /api/legados/especies - Debe listar especies únicas")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerEspeciesUnicas_debeRetornarLista() throws Exception {
        List<String> especies = Arrays.asList("Triticum aestivum", "Hordeum vulgare");

        when(legadoService.obtenerEspeciesUnicas()).thenReturn(especies);

        mockMvc.perform(get("/api/legados/especies")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Triticum aestivum"));
    }

    @Test
    @DisplayName("DELETE /api/legados/{id} - Admin puede desactivar registro")
    @WithMockUser(roles = "ADMIN")
    void desactivar_comoAdmin_debeRetornar200() throws Exception {
        doNothing().when(legadoService).desactivar(anyLong());

        mockMvc.perform(delete("/api/legados/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/legados/{id} - Debe manejar error al desactivar")
    @WithMockUser(roles = "ADMIN")
    void desactivar_conError_debeRetornar404() throws Exception {
        doThrow(new RuntimeException("Legado no encontrado")).when(legadoService).desactivar(anyLong());

        mockMvc.perform(delete("/api/legados/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/legados/listado - Debe permitir búsqueda por término")
    @WithMockUser(roles = "ANALISTA")
    void obtenerLegadosPaginadas_conSearchTerm_debeRetornarResultados() throws Exception {
        List<LegadoListadoDTO> legados = Arrays.asList(legadoListado);
        Page<LegadoListadoDTO> page = new PageImpl<>(legados);

        when(legadoService.obtenerLegadosPaginadas(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/legados/listado")
                .param("search", "LOTE-2023")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/legados/listado - Debe permitir filtro por especie")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerLegadosPaginadas_conEspecie_debeRetornarResultados() throws Exception {
        List<LegadoListadoDTO> legados = Arrays.asList(legadoListado);
        Page<LegadoListadoDTO> page = new PageImpl<>(legados);

        when(legadoService.obtenerLegadosPaginadas(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/legados/listado")
                .param("especie", "Triticum aestivum")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/legados/listado - Debe permitir filtro por rango de fechas")
    @WithMockUser(roles = "ADMIN")
    void obtenerLegadosPaginadas_conRangoFechas_debeRetornarResultados() throws Exception {
        List<LegadoListadoDTO> legados = Arrays.asList(legadoListado);
        Page<LegadoListadoDTO> page = new PageImpl<>(legados);

        when(legadoService.obtenerLegadosPaginadas(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/legados/listado")
                .param("fechaReciboInicio", "2023-01-01")
                .param("fechaReciboFin", "2023-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

