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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DosnService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DosnController.class)
@DisplayName("Tests de integración para DosnController")
class DosnControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DosnService dosnService;

    private DosnDTO dosnDTO;
    private DosnRequestDTO dosnRequestDTO;

    @BeforeEach
    void setUp() {
        dosnDTO = new DosnDTO();
        dosnDTO.setAnalisisID(1L);
        dosnDTO.setIdLote(100L);
        dosnDTO.setComentarios("Test DOSN");
        dosnDTO.setFechaInicio(LocalDateTime.now());
        dosnDTO.setEstado(Estado.EN_PROCESO);
        dosnDTO.setActivo(true);

        dosnRequestDTO = new DosnRequestDTO();
        dosnRequestDTO.setIdLote(100L);
        dosnRequestDTO.setComentarios("Test DOSN");
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/dosn - Crear DOSN exitosamente")
    void crearDosn_debeRetornarDosnCreado() throws Exception {
        when(dosnService.crearDosn(any(DosnRequestDTO.class))).thenReturn(dosnDTO);

        mockMvc.perform(post("/api/dosn")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dosnRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/dosn - Obtener todas las DOSN")
    void obtenerTodasDosnActivas_debeRetornarRespuesta() throws Exception {
        ResponseListadoDosn response = new ResponseListadoDosn();
        response.setDosns(Arrays.asList(dosnDTO));
        when(dosnService.obtenerTodasDosnActivas()).thenReturn(response);

        mockMvc.perform(get("/api/dosn")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dosns[0].analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/dosn/{id} - Obtener DOSN por ID")
    void obtenerDosnPorId_debeRetornarDosn() throws Exception {
        when(dosnService.obtenerDosnPorId(1L)).thenReturn(dosnDTO);

        mockMvc.perform(get("/api/dosn/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/dosn/listado - Obtener DOSN paginadas")
    void obtenerDosnPaginadas_debeRetornarPaginacion() throws Exception {
        DosnListadoDTO listadoDTO = new DosnListadoDTO();
        listadoDTO.setAnalisisID(1L);
        listadoDTO.setIdLote(100L);
        listadoDTO.setEstado(Estado.EN_PROCESO);

        Page<DosnListadoDTO> page = new PageImpl<>(Arrays.asList(listadoDTO), PageRequest.of(0, 10), 1);
        when(dosnService.obtenerDosnPaginadasConFiltros(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/dosn/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].analisisID").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/dosn/{id} - Actualizar DOSN")
    void actualizarDosn_debeRetornarDosnActualizado() throws Exception {
        dosnDTO.setComentarios("Actualizado");
        when(dosnService.actualizarDosn(eq(1L), any(DosnRequestDTO.class))).thenReturn(dosnDTO);

        mockMvc.perform(put("/api/dosn/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dosnRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios").value("Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/dosn/{id} - Eliminar DOSN")
    void eliminarDosn_debeRetornarNoContent() throws Exception {
        doNothing().when(dosnService).eliminarDosn(1L);

        mockMvc.perform(delete("/api/dosn/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/dosn/{id}/desactivar - Desactivar DOSN")
    void desactivarDosn_debeRetornarOk() throws Exception {
        doNothing().when(dosnService).desactivarDosn(1L);

        mockMvc.perform(put("/api/dosn/1/desactivar")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/dosn/{id}/reactivar - Reactivar DOSN")
    void reactivarDosn_debeRetornarDosnReactivado() throws Exception {
        when(dosnService.reactivarDosn(1L)).thenReturn(dosnDTO);

        mockMvc.perform(put("/api/dosn/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/dosn/lote/{idLote} - Obtener DOSN por lote")
    void obtenerDosnPorIdLote_debeRetornarLista() throws Exception {
        List<DosnDTO> lista = Arrays.asList(dosnDTO);
        when(dosnService.obtenerDosnPorIdLote(100)).thenReturn(lista);

        mockMvc.perform(get("/api/dosn/lote/100")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analisisID").value(1))
                .andExpect(jsonPath("$[0].idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/dosn/{id}/finalizar - Finalizar análisis")
    void finalizarAnalisis_debeRetornarDosnFinalizado() throws Exception {
        dosnDTO.setEstado(Estado.PENDIENTE_APROBACION);
        when(dosnService.finalizarAnalisis(1L)).thenReturn(dosnDTO);

        mockMvc.perform(put("/api/dosn/1/finalizar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_APROBACION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/dosn/{id}/aprobar - Aprobar análisis")
    void aprobarAnalisis_debeRetornarDosnAprobado() throws Exception {
        dosnDTO.setEstado(Estado.APROBADO);
        when(dosnService.aprobarAnalisis(1L)).thenReturn(dosnDTO);

        mockMvc.perform(put("/api/dosn/1/aprobar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/dosn/{id}/repetir - Marcar para repetir")
    void marcarParaRepetir_debeRetornarDosnParaRepetir() throws Exception {
        dosnDTO.setEstado(Estado.A_REPETIR);
        when(dosnService.marcarParaRepetir(1L)).thenReturn(dosnDTO);

        mockMvc.perform(put("/api/dosn/1/repetir")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("A_REPETIR"));
    }
}
