package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeadosRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoTetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TetrazolioService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TetrazolioController.class)
@DisplayName("Tests de integración para TetrazolioController")
class TetrazolioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TetrazolioService tetrazolioService;

    private TetrazolioDTO tetrazolioDTO;
    private TetrazolioRequestDTO tetrazolioRequestDTO;

    @BeforeEach
    void setUp() {
        tetrazolioDTO = new TetrazolioDTO();
        tetrazolioDTO.setAnalisisID(1L);
        tetrazolioDTO.setIdLote(100L);
        tetrazolioDTO.setComentarios("Test Tetrazolio");
        tetrazolioDTO.setFechaInicio(LocalDateTime.now());
        tetrazolioDTO.setEstado(Estado.EN_PROCESO);
        tetrazolioDTO.setActivo(true);

        tetrazolioRequestDTO = new TetrazolioRequestDTO();
        tetrazolioRequestDTO.setIdLote(100L);
        tetrazolioRequestDTO.setComentarios("Test Tetrazolio");
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/tetrazolios - Crear Tetrazolio exitosamente")
    void crearTetrazolio_debeRetornarTetrazolioCreado() throws Exception {
        when(tetrazolioService.crearTetrazolio(any(TetrazolioRequestDTO.class))).thenReturn(tetrazolioDTO);

        mockMvc.perform(post("/api/tetrazolios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tetrazolioRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/tetrazolios - Obtener todos los Tetrazolios")
    void obtenerTodosTetrazolio_debeRetornarRespuesta() throws Exception {
        ResponseListadoTetrazolio response = new ResponseListadoTetrazolio();
        response.setTetrazolios(Arrays.asList(tetrazolioDTO));
        when(tetrazolioService.obtenerTodosTetrazolio()).thenReturn(response);

        mockMvc.perform(get("/api/tetrazolios")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tetrazolios[0].analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/tetrazolios/{id} - Obtener Tetrazolio por ID")
    void obtenerTetrazolioPorId_debeRetornarTetrazolio() throws Exception {
        when(tetrazolioService.obtenerTetrazolioPorId(1L)).thenReturn(tetrazolioDTO);

        mockMvc.perform(get("/api/tetrazolios/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/tetrazolios/listado - Obtener Tetrazolios paginados")
    void obtenerTetrazoliosPaginadas_debeRetornarPaginacion() throws Exception {
        TetrazolioListadoDTO listadoDTO = new TetrazolioListadoDTO();
        listadoDTO.setAnalisisID(1L);
        listadoDTO.setIdLote(100L);
        listadoDTO.setEstado(Estado.EN_PROCESO);

        Page<TetrazolioListadoDTO> page = new PageImpl<>(Arrays.asList(listadoDTO), PageRequest.of(0, 10), 1);
        when(tetrazolioService.obtenerTetrazoliosPaginadasConFiltros(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/tetrazolios/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].analisisID").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/tetrazolios/{id} - Actualizar Tetrazolio")
    void actualizarTetrazolio_debeRetornarTetrazolioActualizado() throws Exception {
        tetrazolioDTO.setComentarios("Actualizado");
        when(tetrazolioService.actualizarTetrazolio(eq(1L), any(TetrazolioRequestDTO.class))).thenReturn(tetrazolioDTO);

        mockMvc.perform(put("/api/tetrazolios/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tetrazolioRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios").value("Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/tetrazolios/{id}/porcentajes - Actualizar porcentajes redondeados")
    void actualizarPorcentajesRedondeados_debeRetornarTetrazolioActualizado() throws Exception {
        PorcentajesRedondeadosRequestDTO porcentajesDTO = new PorcentajesRedondeadosRequestDTO();
        when(tetrazolioService.actualizarPorcentajesRedondeados(eq(1L), any(PorcentajesRedondeadosRequestDTO.class))).thenReturn(tetrazolioDTO);

        mockMvc.perform(put("/api/tetrazolios/1/porcentajes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(porcentajesDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/tetrazolios/{id} - Eliminar Tetrazolio")
    void eliminarTetrazolio_debeRetornarNoContent() throws Exception {
        doNothing().when(tetrazolioService).desactivarTetrazolio(1L);

        mockMvc.perform(delete("/api/tetrazolios/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/tetrazolios/{id}/desactivar - Desactivar Tetrazolio")
    void desactivarTetrazolio_debeRetornarOk() throws Exception {
        doNothing().when(tetrazolioService).desactivarTetrazolio(1L);

        mockMvc.perform(put("/api/tetrazolios/1/desactivar")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/tetrazolios/{id}/reactivar - Reactivar Tetrazolio")
    void reactivarTetrazolio_debeRetornarTetrazolioReactivado() throws Exception {
        when(tetrazolioService.reactivarTetrazolio(1L)).thenReturn(tetrazolioDTO);

        mockMvc.perform(put("/api/tetrazolios/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/tetrazolios/lote/{idLote} - Obtener Tetrazolios por lote")
    void obtenerTetrazoliosPorIdLote_debeRetornarLista() throws Exception {
        List<TetrazolioDTO> lista = Arrays.asList(tetrazolioDTO);
        when(tetrazolioService.obtenerTetrazoliosPorIdLote(100L)).thenReturn(lista);

        mockMvc.perform(get("/api/tetrazolios/lote/100")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analisisID").value(1))
                .andExpect(jsonPath("$[0].idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/tetrazolios/{id}/finalizar - Finalizar análisis")
    void finalizarAnalisis_debeRetornarTetrazolioFinalizado() throws Exception {
        tetrazolioDTO.setEstado(Estado.PENDIENTE_APROBACION);
        when(tetrazolioService.finalizarAnalisis(1L)).thenReturn(tetrazolioDTO);

        mockMvc.perform(put("/api/tetrazolios/1/finalizar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_APROBACION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/tetrazolios/{id}/aprobar - Aprobar análisis")
    void aprobarAnalisis_debeRetornarTetrazolioAprobado() throws Exception {
        tetrazolioDTO.setEstado(Estado.APROBADO);
        when(tetrazolioService.aprobarAnalisis(1L)).thenReturn(tetrazolioDTO);

        mockMvc.perform(put("/api/tetrazolios/1/aprobar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/tetrazolios/{id}/repetir - Marcar para repetir")
    void marcarParaRepetir_debeRetornarTetrazolioParaRepetir() throws Exception {
        tetrazolioDTO.setEstado(Estado.A_REPETIR);
        when(tetrazolioService.marcarParaRepetir(1L)).thenReturn(tetrazolioDTO);

        mockMvc.perform(put("/api/tetrazolios/1/repetir")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("A_REPETIR"));
    }
}
