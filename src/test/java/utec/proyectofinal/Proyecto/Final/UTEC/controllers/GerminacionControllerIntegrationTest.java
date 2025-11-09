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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionEditRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoGerminacion;
import utec.proyectofinal.Proyecto.Final.UTEC.services.GerminacionService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GerminacionController.class)
@DisplayName("Tests de integración para GerminacionController")
class GerminacionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GerminacionService germinacionService;

    private GerminacionDTO germinacionDTO;
    private GerminacionRequestDTO germinacionRequestDTO;

    @BeforeEach
    void setUp() {
        germinacionDTO = new GerminacionDTO();
        germinacionDTO.setAnalisisID(1L);
        germinacionDTO.setIdLote(100L);
        germinacionDTO.setComentarios("Test Germinacion");
        germinacionDTO.setFechaInicio(LocalDateTime.now());
        germinacionDTO.setEstado(Estado.EN_PROCESO);
        germinacionDTO.setActivo(true);

        germinacionRequestDTO = new GerminacionRequestDTO();
        germinacionRequestDTO.setIdLote(100L);
        germinacionRequestDTO.setComentarios("Test Germinacion");
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/germinaciones - Crear Germinación exitosamente")
    void crearGerminacion_debeRetornarGerminacionCreada() throws Exception {
        when(germinacionService.crearGerminacion(any(GerminacionRequestDTO.class))).thenReturn(germinacionDTO);

        mockMvc.perform(post("/api/germinaciones")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(germinacionRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinaciones - Obtener todas las Germinaciones")
    void obtenerTodasGerminaciones_debeRetornarRespuesta() throws Exception {
        ResponseListadoGerminacion response = new ResponseListadoGerminacion();
        response.setGerminaciones(Arrays.asList(germinacionDTO));
        when(germinacionService.obtenerTodasGerminaciones()).thenReturn(response);

        mockMvc.perform(get("/api/germinaciones")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.germinaciones[0].analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinaciones/{id} - Obtener Germinación por ID")
    void obtenerGerminacionPorId_debeRetornarGerminacion() throws Exception {
        when(germinacionService.obtenerGerminacionPorId(1L)).thenReturn(germinacionDTO);

        mockMvc.perform(get("/api/germinaciones/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinaciones/listado - Obtener Germinaciones paginadas")
    void obtenerGerminacionesPaginadas_debeRetornarPaginacion() throws Exception {
        GerminacionListadoDTO listadoDTO = new GerminacionListadoDTO();
        listadoDTO.setAnalisisID(1L);
        listadoDTO.setIdLote(100L);
        listadoDTO.setEstado(Estado.EN_PROCESO);

        Page<GerminacionListadoDTO> page = new PageImpl<>(Arrays.asList(listadoDTO), PageRequest.of(0, 10), 1);
        when(germinacionService.obtenerGerminacionesPaginadasConFiltros(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/germinaciones/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].analisisID").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinaciones/{id} - Actualizar Germinación")
    void actualizarGerminacion_debeRetornarGerminacionActualizada() throws Exception {
        GerminacionEditRequestDTO editDTO = new GerminacionEditRequestDTO();
        editDTO.setComentarios("Actualizado");
        
        germinacionDTO.setComentarios("Actualizado");
        when(germinacionService.actualizarGerminacionSeguro(eq(1L), any(GerminacionEditRequestDTO.class))).thenReturn(germinacionDTO);

        mockMvc.perform(put("/api/germinaciones/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios").value("Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/germinaciones/{id} - Eliminar Germinación")
    void eliminarGerminacion_debeRetornarNoContent() throws Exception {
        doNothing().when(germinacionService).eliminarGerminacion(1L);

        mockMvc.perform(delete("/api/germinaciones/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/germinaciones/{id}/desactivar - Desactivar Germinación")
    void desactivarGerminacion_debeRetornarOk() throws Exception {
        doNothing().when(germinacionService).desactivarGerminacion(1L);

        mockMvc.perform(put("/api/germinaciones/1/desactivar")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/germinaciones/{id}/reactivar - Reactivar Germinación")
    void reactivarGerminacion_debeRetornarGerminacionReactivada() throws Exception {
        when(germinacionService.reactivarGerminacion(1L)).thenReturn(germinacionDTO);

        mockMvc.perform(put("/api/germinaciones/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinaciones/lote/{idLote} - Obtener Germinaciones por lote")
    void obtenerGerminacionesPorIdLote_debeRetornarLista() throws Exception {
        List<GerminacionDTO> lista = Arrays.asList(germinacionDTO);
        when(germinacionService.obtenerGerminacionesPorIdLote(100L)).thenReturn(lista);

        mockMvc.perform(get("/api/germinaciones/lote/100")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analisisID").value(1))
                .andExpect(jsonPath("$[0].idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinaciones/{id}/finalizar - Finalizar análisis")
    void finalizarAnalisis_debeRetornarGerminacionFinalizada() throws Exception {
        germinacionDTO.setEstado(Estado.PENDIENTE_APROBACION);
        when(germinacionService.finalizarAnalisis(1L)).thenReturn(germinacionDTO);

        mockMvc.perform(put("/api/germinaciones/1/finalizar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_APROBACION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/germinaciones/{id}/aprobar - Aprobar análisis")
    void aprobarAnalisis_debeRetornarGerminacionAprobada() throws Exception {
        germinacionDTO.setEstado(Estado.APROBADO);
        when(germinacionService.aprobarAnalisis(1L)).thenReturn(germinacionDTO);

        mockMvc.perform(put("/api/germinaciones/1/aprobar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/germinaciones/{id}/repetir - Marcar para repetir")
    void marcarParaRepetir_debeRetornarGerminacionParaRepetir() throws Exception {
        germinacionDTO.setEstado(Estado.A_REPETIR);
        when(germinacionService.marcarParaRepetir(1L)).thenReturn(germinacionDTO);

        mockMvc.perform(put("/api/germinaciones/1/repetir")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("A_REPETIR"));
    }
}
