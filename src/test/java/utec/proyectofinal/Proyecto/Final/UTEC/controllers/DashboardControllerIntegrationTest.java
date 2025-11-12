package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.CursorPageResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import utec.proyectofinal.Proyecto.Final.UTEC.exceptions.InvalidCursorException;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DashboardService;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para DashboardController")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    private DashboardStatsDTO statsDTO;
    private AnalisisPendienteDTO analisisPendiente;
    private AnalisisPorAprobarDTO analisisPorAprobar;

    @BeforeEach
    void setUp() {
        statsDTO = new DashboardStatsDTO();
        statsDTO.setLotesActivos(100L);
        statsDTO.setAnalisisPendientes(10L);
        statsDTO.setCompletadosHoy(40L);
        statsDTO.setAnalisisPorAprobar(5L);

        analisisPendiente = new AnalisisPendienteDTO();
        analisisPendiente.setLoteID(1L);
        analisisPendiente.setTipoAnalisis(TipoAnalisis.GERMINACION);
        analisisPendiente.setNomLote("LOTE-001");
        analisisPendiente.setFicha("F-001");

        analisisPorAprobar = new AnalisisPorAprobarDTO();
        analisisPorAprobar.setAnalisisID(2L);
        analisisPorAprobar.setTipo(TipoAnalisis.PUREZA);
        analisisPorAprobar.setLoteID(2L);
        analisisPorAprobar.setNomLote("LOTE-002");
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe obtener estadísticas del dashboard")
    @WithMockUser(roles = "ADMIN")
    void obtenerEstadisticas_debeRetornarStats() throws Exception {
        when(dashboardService.obtenerEstadisticas()).thenReturn(statsDTO);

        mockMvc.perform(get("/api/dashboard/stats")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotesActivos").value(100))
                .andExpect(jsonPath("$.analisisPendientes").value(10))
                .andExpect(jsonPath("$.completadosHoy").value(40))
                .andExpect(jsonPath("$.analisisPorAprobar").value(5));
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe manejar error interno")
    @WithMockUser(roles = "ADMIN")
    void obtenerEstadisticas_conError_debeRetornar500() throws Exception {
        when(dashboardService.obtenerEstadisticas()).thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(get("/api/dashboard/stats")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes - Debe obtener análisis pendientes paginados")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientes_debeRetornarPaginacion() throws Exception {
        Page<AnalisisPendienteDTO> page = new PageImpl<>(Arrays.asList(analisisPendiente));
        when(dashboardService.listarAnalisisPendientesPaginados(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/analisis-pendientes")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].loteID").value(1))
                .andExpect(jsonPath("$.content[0].tipoAnalisis").value("GERMINACION"));
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-por-aprobar - Debe obtener análisis por aprobar con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void obtenerAnalisisPorAprobar_conRolAdmin_debeRetornarPaginacion() throws Exception {
        Page<AnalisisPorAprobarDTO> page = new PageImpl<>(Arrays.asList(analisisPorAprobar));
        when(dashboardService.listarAnalisisPorAprobarPaginados(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/analisis-por-aprobar")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].analisisID").value(2))
                .andExpect(jsonPath("$.content[0].tipo").value("PUREZA"));
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes/keyset - Debe obtener con cursor")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientesKeyset_debeRetornarCursorPage() throws Exception {
        CursorPageResponse<AnalisisPendienteDTO> response = new CursorPageResponse<>();
        response.setItems(Arrays.asList(analisisPendiente));
        response.setNextCursor("base64cursor");
        response.setHasMore(true);
        response.setSize(20);

        when(dashboardService.listarAnalisisPendientesKeyset(null, 20)).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/analisis-pendientes/keyset")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.nextCursor").value("base64cursor"))
                .andExpect(jsonPath("$.hasMore").value(true));
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes/keyset - Debe manejar cursor inválido")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientesKeyset_conCursorInvalido_debeRetornar400() throws Exception {
        when(dashboardService.listarAnalisisPendientesKeyset("invalid", 20))
            .thenThrow(new InvalidCursorException("Cursor inválido"));

        mockMvc.perform(get("/api/dashboard/analisis-pendientes/keyset")
                .param("cursor", "invalid")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-por-aprobar/keyset - Debe obtener con cursor para ADMIN")
    @WithMockUser(roles = "ADMIN")
    void obtenerAnalisisPorAprobarKeyset_conRolAdmin_debeRetornarCursorPage() throws Exception {
        CursorPageResponse<AnalisisPorAprobarDTO> response = new CursorPageResponse<>();
        response.setItems(Arrays.asList(analisisPorAprobar));
        response.setNextCursor("base64cursor2");
        response.setHasMore(false);
        response.setSize(20);

        when(dashboardService.listarAnalisisPorAprobarKeyset(null, 20)).thenReturn(response);

        mockMvc.perform(get("/api/dashboard/analisis-por-aprobar/keyset")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.nextCursor").value("base64cursor2"))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes - Debe usar valores por defecto de paginación")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientes_sinParametros_debeUsarDefaults() throws Exception {
        Page<AnalisisPendienteDTO> page = new PageImpl<>(Arrays.asList(analisisPendiente));
        when(dashboardService.listarAnalisisPendientesPaginados(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/analisis-pendientes")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes - Debe manejar error en servicio")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientes_conError_debeRetornar500() throws Exception {
        when(dashboardService.listarAnalisisPendientesPaginados(any(Pageable.class)))
            .thenThrow(new RuntimeException("Error en servicio"));

        mockMvc.perform(get("/api/dashboard/analisis-pendientes")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-por-aprobar - Debe manejar error en servicio")
    @WithMockUser(roles = "ADMIN")
    void obtenerAnalisisPorAprobar_conError_debeRetornar500() throws Exception {
        when(dashboardService.listarAnalisisPorAprobarPaginados(any(Pageable.class)))
            .thenThrow(new RuntimeException("Error en servicio"));

        mockMvc.perform(get("/api/dashboard/analisis-por-aprobar")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes/keyset - Debe manejar error genérico")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientesKeyset_conErrorGenerico_debeRetornar500() throws Exception {
        when(dashboardService.listarAnalisisPendientesKeyset(null, 20))
            .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(get("/api/dashboard/analisis-pendientes/keyset")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-por-aprobar/keyset - Debe manejar cursor inválido")
    @WithMockUser(roles = "ADMIN")
    void obtenerAnalisisPorAprobarKeyset_conCursorInvalido_debeRetornar400() throws Exception {
        when(dashboardService.listarAnalisisPorAprobarKeyset("invalidCursor", 20))
            .thenThrow(new InvalidCursorException("Cursor inválido o corrupto"));

        mockMvc.perform(get("/api/dashboard/analisis-por-aprobar/keyset")
                .param("cursor", "invalidCursor")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-por-aprobar/keyset - Debe manejar error genérico")
    @WithMockUser(roles = "ADMIN")
    void obtenerAnalisisPorAprobarKeyset_conErrorGenerico_debeRetornar500() throws Exception {
        when(dashboardService.listarAnalisisPorAprobarKeyset(null, 20))
            .thenThrow(new RuntimeException("Error inesperado en servicio"));

        mockMvc.perform(get("/api/dashboard/analisis-por-aprobar/keyset")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
