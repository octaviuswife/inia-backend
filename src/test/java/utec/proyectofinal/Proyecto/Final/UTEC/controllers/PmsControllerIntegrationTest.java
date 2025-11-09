package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PmsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PmsController.class)
@DisplayName("Tests de integración para PmsController")
class PmsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PmsService pmsService;

    private PmsDTO pmsDTO;
    private PmsRequestDTO pmsRequestDTO;

    @BeforeEach
    void setUp() {
        pmsDTO = new PmsDTO();
        pmsDTO.setAnalisisID(1L);
        pmsDTO.setIdLote(100L);
        pmsDTO.setNumRepeticionesEsperadas(4);
        pmsDTO.setEsSemillaBrozosa(false);
        pmsDTO.setComentarios("Test PMS");
        pmsDTO.setFechaInicio(LocalDateTime.now());
        pmsDTO.setEstado(Estado.EN_PROCESO);
        pmsDTO.setActivo(true);

        pmsRequestDTO = new PmsRequestDTO();
        pmsRequestDTO.setIdLote(100L);
        pmsRequestDTO.setNumRepeticionesEsperadas(4);
        pmsRequestDTO.setEsSemillaBrozosa(false);
        pmsRequestDTO.setComentarios("Test PMS");
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/pms - Crear PMS exitosamente")
    void crearPms_debeRetornarPmsCreado() throws Exception {
        when(pmsService.crearPms(any(PmsRequestDTO.class))).thenReturn(pmsDTO);

        mockMvc.perform(post("/api/pms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pmsRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100))
                .andExpect(jsonPath("$.numRepeticionesEsperadas").value(4))
                .andExpect(jsonPath("$.esSemillaBrozosa").value(false))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/pms - Obtener todos los PMS activos")
    void obtenerTodos_debeRetornarListaDePms() throws Exception {
        List<PmsDTO> listaPms = Arrays.asList(pmsDTO);
        when(pmsService.obtenerTodos()).thenReturn(listaPms);

        mockMvc.perform(get("/api/pms")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analisisID").value(1))
                .andExpect(jsonPath("$[0].idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/pms/{id} - Obtener PMS por ID")
    void obtenerPorId_debeRetornarPms() throws Exception {
        when(pmsService.obtenerPorId(1L)).thenReturn(pmsDTO);

        mockMvc.perform(get("/api/pms/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/pms/listado - Obtener PMS paginados")
    void obtenerPmsPaginadas_debeRetornarPaginacion() throws Exception {
        PmsListadoDTO listadoDTO = new PmsListadoDTO();
        listadoDTO.setAnalisisID(1L);
        listadoDTO.setIdLote(100L);
        listadoDTO.setEstado(Estado.EN_PROCESO);

        Page<PmsListadoDTO> page = new PageImpl<>(Arrays.asList(listadoDTO), PageRequest.of(0, 10), 1);
        when(pmsService.obtenerPmsPaginadasConFiltros(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/pms/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].analisisID").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/pms/{id} - Actualizar PMS")
    void actualizarPms_debeRetornarPmsActualizado() throws Exception {
        pmsDTO.setComentarios("Actualizado");
        when(pmsService.actualizarPms(eq(1L), any(PmsRequestDTO.class))).thenReturn(pmsDTO);

        mockMvc.perform(put("/api/pms/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pmsRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.comentarios").value("Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/pms/{id} - Eliminar PMS")
    void eliminarPms_debeRetornarNoContent() throws Exception {
        doNothing().when(pmsService).eliminarPms(1L);

        mockMvc.perform(delete("/api/pms/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/pms/{id}/desactivar - Desactivar PMS")
    void desactivarPms_debeRetornarOk() throws Exception {
        doNothing().when(pmsService).desactivarPms(1L);

        mockMvc.perform(put("/api/pms/1/desactivar")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/pms/{id}/reactivar - Reactivar PMS")
    void reactivarPms_debeRetornarPmsReactivado() throws Exception {
        when(pmsService.reactivarPms(1L)).thenReturn(pmsDTO);

        mockMvc.perform(put("/api/pms/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/pms/lote/{idLote} - Obtener PMS por ID de lote")
    void obtenerPmsPorIdLote_debeRetornarLista() throws Exception {
        List<PmsDTO> lista = Arrays.asList(pmsDTO);
        when(pmsService.obtenerPmsPorIdLote(100L)).thenReturn(lista);

        mockMvc.perform(get("/api/pms/lote/100")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analisisID").value(1))
                .andExpect(jsonPath("$[0].idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/pms/{id}/redondeo - Actualizar PMS con redondeo")
    void actualizarPmsConRedondeo_debeRetornarPmsActualizado() throws Exception {
        PmsRedondeoRequestDTO redondeoDTO = new PmsRedondeoRequestDTO();
        redondeoDTO.setPmsconRedon(new BigDecimal("25.5"));
        when(pmsService.actualizarPmsConRedondeo(eq(1L), any(PmsRedondeoRequestDTO.class))).thenReturn(pmsDTO);

        mockMvc.perform(put("/api/pms/1/redondeo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(redondeoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/pms/{id}/finalizar - Finalizar análisis PMS")
    void finalizarAnalisis_debeRetornarPmsFinalizado() throws Exception {
        pmsDTO.setEstado(Estado.PENDIENTE_APROBACION);
        when(pmsService.finalizarAnalisis(1L)).thenReturn(pmsDTO);

        mockMvc.perform(put("/api/pms/1/finalizar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_APROBACION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/pms/{id}/aprobar - Aprobar análisis PMS")
    void aprobarAnalisis_debeRetornarPmsAprobado() throws Exception {
        pmsDTO.setEstado(Estado.APROBADO);
        when(pmsService.aprobarAnalisis(1L)).thenReturn(pmsDTO);

        mockMvc.perform(put("/api/pms/1/aprobar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/pms/{id}/repetir - Marcar análisis PMS para repetir")
    void marcarParaRepetir_debeRetornarPmsParaRepetir() throws Exception {
        pmsDTO.setEstado(Estado.A_REPETIR);
        when(pmsService.marcarParaRepetir(1L)).thenReturn(pmsDTO);

        mockMvc.perform(put("/api/pms/1/repetir")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("A_REPETIR"));
    }
}
