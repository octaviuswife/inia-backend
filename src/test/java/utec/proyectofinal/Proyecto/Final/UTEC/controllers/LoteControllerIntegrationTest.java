package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;
import utec.proyectofinal.Proyecto.Final.UTEC.services.LoteService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de Controlador para LoteController
 * 
 * Usa @WebMvcTest en lugar de @SpringBootTest para evitar problemas
 * con la configuración completa de Spring Security.
 * 
 * Estos tests verifican:
 * - Endpoints REST funcionan correctamente
 * - Serialización/Deserialización JSON
 * - Códigos de respuesta HTTP
 * - Validación de permisos con @WithMockUser
 */
@WebMvcTest(LoteController.class)
@DisplayName("Tests de Controlador - LoteController")
class LoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoteService loteService;

    private LoteRequestDTO loteRequestDTO;
    private LoteDTO loteDTO;

    @BeforeEach
    void setUp() {
        loteRequestDTO = new LoteRequestDTO();
        loteRequestDTO.setNomLote("LOTE-TEST-001");
        loteRequestDTO.setFechaRecibo(LocalDate.now());
        loteRequestDTO.setFicha("F-001");
        loteRequestDTO.setCultivarID(1L);
        loteRequestDTO.setClienteID(1L);

        loteDTO = new LoteDTO();
        loteDTO.setLoteID(1L);
        loteDTO.setNomLote("LOTE-TEST-001");
        loteDTO.setActivo(true);
        loteDTO.setFechaRecibo(LocalDate.now());
        loteDTO.setFicha("F-001");
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/lotes - debe crear lote con autenticación")
    void crearLote_conAutenticacion_debeRetornar201() throws Exception {
        // ARRANGE
        when(loteService.crearLote(any(LoteRequestDTO.class))).thenReturn(loteDTO);

        // ACT & ASSERT
        mockMvc.perform(post("/api/lotes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loteID").value(1))
                .andExpect(jsonPath("$.nomLote").value("LOTE-TEST-001"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/lotes/{id} - debe retornar lote existente")
    void obtenerLotePorId_existente_debeRetornar200() throws Exception {
        // ARRANGE
        when(loteService.obtenerLotePorId(1L)).thenReturn(loteDTO);

        // ACT & ASSERT
        mockMvc.perform(get("/api/lotes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loteID").value(1))
                .andExpect(jsonPath("$.nomLote").value("LOTE-TEST-001"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/lotes/activos - debe retornar lista de lotes activos")
    void listarLotesActivos_debeRetornarLista() throws Exception {
        // ARRANGE
        ResponseListadoLoteSimple response = new ResponseListadoLoteSimple();
        when(loteService.obtenerTodosLotesActivos()).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(get("/api/lotes/activos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/lotes/{id} - debe actualizar lote")
    void actualizarLote_conDatosValidos_debeRetornar200() throws Exception {
        // ARRANGE
        loteRequestDTO.setNomLote("LOTE-ACTUALIZADO");
        loteDTO.setNomLote("LOTE-ACTUALIZADO");
        
        when(loteService.actualizarLote(eq(1L), any(LoteRequestDTO.class))).thenReturn(loteDTO);

        // ACT & ASSERT
        mockMvc.perform(put("/api/lotes/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomLote").value("LOTE-ACTUALIZADO"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/lotes/{id} - debe desactivar lote")
    void eliminarLote_conPermisos_debeRetornar200() throws Exception {
        // ARRANGE
        doNothing().when(loteService).eliminarLote(1L);
        
        // ACT & ASSERT
        mockMvc.perform(delete("/api/lotes/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/lotes/{id}/reactivar - debe reactivar lote")
    void reactivarLote_conPermisos_debeRetornar200() throws Exception {
        // ARRANGE
        loteDTO.setActivo(true);
        when(loteService.reactivarLote(1L)).thenReturn(loteDTO);

        // ACT & ASSERT
        mockMvc.perform(put("/api/lotes/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }
}
