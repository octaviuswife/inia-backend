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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPureza;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PurezaService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurezaController.class)
@DisplayName("Tests de integración para PurezaController")
class PurezaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurezaService purezaService;

    private PurezaDTO purezaDTO;
    private PurezaRequestDTO purezaRequestDTO;

    @BeforeEach
    void setUp() {
        purezaDTO = new PurezaDTO();
        purezaDTO.setAnalisisID(1L);
        purezaDTO.setIdLote(100L);
        purezaDTO.setComentarios("Test Pureza");
        purezaDTO.setFechaInicio(LocalDateTime.now());
        purezaDTO.setEstado(Estado.EN_PROCESO);
        purezaDTO.setActivo(true);

        purezaRequestDTO = new PurezaRequestDTO();
        purezaRequestDTO.setIdLote(100L);
        purezaRequestDTO.setComentarios("Test Pureza");
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/purezas - Crear Pureza exitosamente")
    void crearPureza_debeRetornarPurezaCreada() throws Exception {
        when(purezaService.crearPureza(any(PurezaRequestDTO.class))).thenReturn(purezaDTO);

        mockMvc.perform(post("/api/purezas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(purezaRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/purezas - Obtener todas las Purezas activas")
    void obtenerTodasPurezasActivas_debeRetornarRespuesta() throws Exception {
        ResponseListadoPureza response = new ResponseListadoPureza();
        response.setPurezas(Arrays.asList(purezaDTO));
        when(purezaService.obtenerTodasPurezasActivas()).thenReturn(response);

        mockMvc.perform(get("/api/purezas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purezas[0].analisisID").value(1))
                .andExpect(jsonPath("$.purezas[0].idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/purezas/{id} - Obtener Pureza por ID")
    void obtenerPurezaPorId_debeRetornarPureza() throws Exception {
        when(purezaService.obtenerPurezaPorId(1L)).thenReturn(purezaDTO);

        mockMvc.perform(get("/api/purezas/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.idLote").value(100))
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/purezas/{id} - Actualizar Pureza")
    void actualizarPureza_debeRetornarPurezaActualizada() throws Exception {
        purezaDTO.setComentarios("Actualizado");
        when(purezaService.actualizarPureza(eq(1L), any(PurezaRequestDTO.class))).thenReturn(purezaDTO);

        mockMvc.perform(put("/api/purezas/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(purezaRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1))
                .andExpect(jsonPath("$.comentarios").value("Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/purezas/{id} - Eliminar Pureza")
    void eliminarPureza_debeRetornarNoContent() throws Exception {
        doNothing().when(purezaService).eliminarPureza(1L);

        mockMvc.perform(delete("/api/purezas/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/purezas/{id}/desactivar - Desactivar Pureza")
    void desactivarPureza_debeRetornarOk() throws Exception {
        doNothing().when(purezaService).desactivarPureza(1L);

        mockMvc.perform(put("/api/purezas/1/desactivar")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/purezas/{id}/reactivar - Reactivar Pureza")
    void reactivarPureza_debeRetornarPurezaReactivada() throws Exception {
        when(purezaService.reactivarPureza(1L)).thenReturn(purezaDTO);

        mockMvc.perform(put("/api/purezas/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analisisID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/purezas/lote/{idLote} - Obtener Purezas por ID de lote")
    void obtenerPurezasPorIdLote_debeRetornarLista() throws Exception {
        List<PurezaDTO> lista = Arrays.asList(purezaDTO);
        when(purezaService.obtenerPurezasPorIdLote(100L)).thenReturn(lista);

        mockMvc.perform(get("/api/purezas/lote/100")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].analisisID").value(1))
                .andExpect(jsonPath("$[0].idLote").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/purezas/listado - Obtener Purezas paginadas")
    void obtenerPurezaPaginadas_debeRetornarPaginacion() throws Exception {
        PurezaListadoDTO listadoDTO = new PurezaListadoDTO();
        listadoDTO.setAnalisisID(1L);
        listadoDTO.setIdLote(100L);
        listadoDTO.setEstado(Estado.EN_PROCESO);

        Page<PurezaListadoDTO> page = new PageImpl<>(Arrays.asList(listadoDTO), PageRequest.of(0, 10), 1);
        when(purezaService.obtenerPurezaPaginadasConFiltros(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/purezas/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].analisisID").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/purezas/catalogos - Obtener catálogos")
    void obtenerTodosCatalogos_debeRetornarLista() throws Exception {
        MalezasCatalogoDTO catalogoDTO = new MalezasCatalogoDTO();
        catalogoDTO.setCatalogoID(1L);
        catalogoDTO.setNombreComun("Maleza 1");

        List<MalezasCatalogoDTO> catalogos = Arrays.asList(catalogoDTO);
        when(purezaService.obtenerTodosCatalogos()).thenReturn(catalogos);

        mockMvc.perform(get("/api/purezas/catalogos")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].catalogoID").value(1))
                .andExpect(jsonPath("$[0].nombreComun").value("Maleza 1"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/purezas/{id}/finalizar - Finalizar análisis de Pureza")
    void finalizarAnalisis_debeRetornarPurezaFinalizada() throws Exception {
        purezaDTO.setEstado(Estado.PENDIENTE_APROBACION);
        when(purezaService.finalizarAnalisis(1L)).thenReturn(purezaDTO);

        mockMvc.perform(put("/api/purezas/1/finalizar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_APROBACION"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/purezas/{id}/aprobar - Aprobar análisis de Pureza")
    void aprobarAnalisis_debeRetornarPurezaAprobada() throws Exception {
        purezaDTO.setEstado(Estado.APROBADO);
        when(purezaService.aprobarAnalisis(1L)).thenReturn(purezaDTO);

        mockMvc.perform(put("/api/purezas/1/aprobar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/purezas/{id}/repetir - Marcar análisis de Pureza para repetir")
    void marcarParaRepetir_debeRetornarPurezaParaRepetir() throws Exception {
        purezaDTO.setEstado(Estado.A_REPETIR);
        when(purezaService.marcarParaRepetir(1L)).thenReturn(purezaDTO);

        mockMvc.perform(put("/api/purezas/1/repetir")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("A_REPETIR"));
    }
}
