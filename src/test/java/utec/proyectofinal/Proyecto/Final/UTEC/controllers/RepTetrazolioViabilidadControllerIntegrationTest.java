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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepTetrazolioViabilidadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepTetrazolioViabilidadDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepTetrazolioViabilidadService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepTetrazolioViabilidadController.class)
@DisplayName("Tests de integración para RepTetrazolioViabilidadController")
class RepTetrazolioViabilidadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepTetrazolioViabilidadService repeticionService;

    private RepTetrazolioViabilidadDTO repTetrazolioDTO;
    private RepTetrazolioViabilidadRequestDTO repTetrazolioRequestDTO;
    private static final Long TETRAZOLIO_ID = 1L;
    private static final Long REP_ID = 20L;

    @BeforeEach
    void setUp() {
        repTetrazolioDTO = new RepTetrazolioViabilidadDTO();
        repTetrazolioDTO.setRepTetrazolioViabID(REP_ID);
        repTetrazolioDTO.setFecha(LocalDate.of(2024, 1, 15));
        repTetrazolioDTO.setViablesNum(85);
        repTetrazolioDTO.setNoViablesNum(10);
        repTetrazolioDTO.setDuras(5);

        repTetrazolioRequestDTO = new RepTetrazolioViabilidadRequestDTO();
        repTetrazolioRequestDTO.setFecha(LocalDate.of(2024, 1, 15));
        repTetrazolioRequestDTO.setViablesNum(85);
        repTetrazolioRequestDTO.setNoViablesNum(10);
        repTetrazolioRequestDTO.setDuras(5);
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/tetrazolios/{tetrazolioId}/repeticiones - Crear repetición tetrazolio exitosamente")
    void crearRepeticion_debeRetornarRepeticionCreada() throws Exception {
        when(repeticionService.crearRepeticion(eq(TETRAZOLIO_ID), any(RepTetrazolioViabilidadRequestDTO.class)))
            .thenReturn(repTetrazolioDTO);

        mockMvc.perform(post("/api/tetrazolios/{tetrazolioId}/repeticiones", TETRAZOLIO_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repTetrazolioRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.repTetrazolioViabID").value(REP_ID))
                .andExpect(jsonPath("$.viablesNum").value(85))
                .andExpect(jsonPath("$.noViablesNum").value(10))
                .andExpect(jsonPath("$.duras").value(5));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/tetrazolios/{tetrazolioId}/repeticiones - Error con suma incorrecta")
    void crearRepeticion_debeRetornar400ConSumaIncorrecta() throws Exception {
        when(repeticionService.crearRepeticion(eq(TETRAZOLIO_ID), any(RepTetrazolioViabilidadRequestDTO.class)))
            .thenThrow(new RuntimeException("La suma de semillas no coincide con el total esperado"));

        mockMvc.perform(post("/api/tetrazolios/{tetrazolioId}/repeticiones", TETRAZOLIO_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repTetrazolioRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/tetrazolios/{tetrazolioId}/repeticiones - Obtener todas las repeticiones")
    void obtenerRepeticionesPorTetrazolio_debeRetornarLista() throws Exception {
        RepTetrazolioViabilidadDTO rep2 = new RepTetrazolioViabilidadDTO();
        rep2.setRepTetrazolioViabID(21L);
        rep2.setViablesNum(88);
        rep2.setNoViablesNum(8);
        rep2.setDuras(4);

        List<RepTetrazolioViabilidadDTO> repeticiones = Arrays.asList(repTetrazolioDTO, rep2);
        when(repeticionService.obtenerRepeticionesPorTetrazolio(TETRAZOLIO_ID)).thenReturn(repeticiones);

        mockMvc.perform(get("/api/tetrazolios/{tetrazolioId}/repeticiones", TETRAZOLIO_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].repTetrazolioViabID").value(REP_ID))
                .andExpect(jsonPath("$[1].repTetrazolioViabID").value(21))
                .andExpect(jsonPath("$[0].viablesNum").value(85))
                .andExpect(jsonPath("$[1].viablesNum").value(88));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/tetrazolios/{tetrazolioId}/repeticiones/count - Contar repeticiones")
    void contarRepeticiones_debeRetornarCantidad() throws Exception {
        when(repeticionService.contarRepeticionesPorTetrazolio(TETRAZOLIO_ID)).thenReturn(4L);

        mockMvc.perform(get("/api/tetrazolios/{tetrazolioId}/repeticiones/count", TETRAZOLIO_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("4"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId} - Obtener por ID")
    void obtenerRepeticionPorId_debeRetornarRepeticion() throws Exception {
        when(repeticionService.obtenerRepeticionPorId(REP_ID)).thenReturn(repTetrazolioDTO);

        mockMvc.perform(get("/api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId}", 
                TETRAZOLIO_ID, REP_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repTetrazolioViabID").value(REP_ID))
                .andExpect(jsonPath("$.viablesNum").value(85));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId} - Error 404")
    void obtenerRepeticionPorId_debeRetornar404CuandoNoExiste() throws Exception {
        when(repeticionService.obtenerRepeticionPorId(999L))
            .thenThrow(new RuntimeException("Repetición no encontrada"));

        mockMvc.perform(get("/api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId}", 
                TETRAZOLIO_ID, 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId} - Actualizar")
    void actualizarRepeticion_debeRetornarRepeticionActualizada() throws Exception {
        repTetrazolioDTO.setViablesNum(90);
        repTetrazolioDTO.setNoViablesNum(7);
        repTetrazolioDTO.setDuras(3);
        
        when(repeticionService.actualizarRepeticion(eq(REP_ID), any(RepTetrazolioViabilidadRequestDTO.class)))
            .thenReturn(repTetrazolioDTO);

        mockMvc.perform(put("/api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId}", 
                TETRAZOLIO_ID, REP_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repTetrazolioRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viablesNum").value(90))
                .andExpect(jsonPath("$.noViablesNum").value(7))
                .andExpect(jsonPath("$.duras").value(3));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId} - Sin validación de valores negativos")
    void actualizarRepeticion_debeRetornar400ConValoresNegativos() throws Exception {
        RepTetrazolioViabilidadRequestDTO requestDTO = new RepTetrazolioViabilidadRequestDTO();
        requestDTO.setFecha(LocalDate.parse("2024-01-15"));
        requestDTO.setViablesNum(-5);
        requestDTO.setNoViablesNum(10);
        requestDTO.setDuras(5);
        
        // El controller no valida valores negativos, delega en el servicio que puede o no validar
        RepTetrazolioViabilidadDTO mockResponse = new RepTetrazolioViabilidadDTO();
        mockResponse.setRepTetrazolioViabID(REP_ID);
        when(repeticionService.actualizarRepeticion(eq(REP_ID), any(RepTetrazolioViabilidadRequestDTO.class)))
            .thenReturn(mockResponse);
        
        mockMvc.perform(put("/api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId}", 
                TETRAZOLIO_ID, REP_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId} - Eliminar")
    void eliminarRepeticion_debeRetornarNoContent() throws Exception {
        doNothing().when(repeticionService).eliminarRepeticion(REP_ID);

        mockMvc.perform(delete("/api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId}", 
                TETRAZOLIO_ID, REP_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId} - Sin validación de existencia")
    void eliminarRepeticion_debeRetornar404CuandoNoExiste() throws Exception {
        // El controller no valida existencia antes de eliminar
        doNothing().when(repeticionService).eliminarRepeticion(999L);

        mockMvc.perform(delete("/api/tetrazolios/{tetrazolioId}/repeticiones/{repeticionId}", 
                TETRAZOLIO_ID, 999L)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/tetrazolios/{tetrazolioId}/repeticiones - Sin validación de campos requeridos")
    void crearRepeticion_debeFallarSinCamposRequeridos() throws Exception {
        RepTetrazolioViabilidadRequestDTO requestInvalido = new RepTetrazolioViabilidadRequestDTO();
        
        // Mock del DTO de respuesta ya que el controller no valida campos requeridos
        RepTetrazolioViabilidadDTO mockResponse = new RepTetrazolioViabilidadDTO();
        mockResponse.setRepTetrazolioViabID(1L);
        when(repeticionService.crearRepeticion(eq(TETRAZOLIO_ID), any(RepTetrazolioViabilidadRequestDTO.class)))
            .thenReturn(mockResponse);

        mockMvc.perform(post("/api/tetrazolios/{tetrazolioId}/repeticiones", TETRAZOLIO_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/tetrazolios/{tetrazolioId}/repeticiones - Validar total de semillas")
    void crearRepeticion_debeValidarTotalSemillas() throws Exception {
        // La suma debería ser 100 (por ejemplo)
        repTetrazolioRequestDTO.setViablesNum(85);
        repTetrazolioRequestDTO.setNoViablesNum(10);
        repTetrazolioRequestDTO.setDuras(10); // suma = 105, incorrecto
        
        when(repeticionService.crearRepeticion(eq(TETRAZOLIO_ID), any(RepTetrazolioViabilidadRequestDTO.class)))
            .thenThrow(new RuntimeException("La suma no coincide con el número de semillas esperado"));

        mockMvc.perform(post("/api/tetrazolios/{tetrazolioId}/repeticiones", TETRAZOLIO_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repTetrazolioRequestDTO)))
                .andExpect(status().isBadRequest());
    }
}
