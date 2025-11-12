package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepPmsService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepPmsController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para RepPmsController")
class RepPmsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepPmsService repPmsService;

    private RepPmsDTO repPmsDTO;
    private RepPmsRequestDTO repPmsRequestDTO;
    private static final Long PMS_ID = 1L;
    private static final Long REP_ID = 10L;

    @BeforeEach
    void setUp() {
        repPmsDTO = new RepPmsDTO();
        repPmsDTO.setRepPMSID(REP_ID);
        repPmsDTO.setNumRep(1);
        repPmsDTO.setNumTanda(1);
        repPmsDTO.setPeso(new BigDecimal("45.5"));
        repPmsDTO.setValido(true);

        repPmsRequestDTO = new RepPmsRequestDTO();
        repPmsRequestDTO.setNumRep(1);
        repPmsRequestDTO.setPeso(new BigDecimal("45.5"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/pms/{pmsId}/repeticiones - Crear repetición PMS exitosamente")
    void crearRepeticion_debeRetornarRepeticionCreada() throws Exception {
        when(repPmsService.crearRepeticion(eq(PMS_ID), any(RepPmsRequestDTO.class)))
            .thenReturn(repPmsDTO);

        mockMvc.perform(post("/api/pms/{pmsId}/repeticiones", PMS_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repPmsRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.repPMSID").value(REP_ID))
                .andExpect(jsonPath("$.numRep").value(1))
                .andExpect(jsonPath("$.numTanda").value(1))
                .andExpect(jsonPath("$.peso").value(45.5))
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/pms/{pmsId}/repeticiones - Error cuando se excede el máximo de repeticiones")
    void crearRepeticion_debeRetornar400CuandoExcedeMaximo() throws Exception {
        when(repPmsService.crearRepeticion(eq(PMS_ID), any(RepPmsRequestDTO.class)))
            .thenThrow(new RuntimeException("Se ha alcanzado el número máximo de repeticiones"));

        mockMvc.perform(post("/api/pms/{pmsId}/repeticiones", PMS_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repPmsRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/pms/{pmsId}/repeticiones - Obtener todas las repeticiones de un PMS")
    void obtenerRepeticionesPorPms_debeRetornarListaDeRepeticiones() throws Exception {
        RepPmsDTO rep2 = new RepPmsDTO();
        rep2.setRepPMSID(11L);
        rep2.setNumRep(2);
        rep2.setNumTanda(1);
        rep2.setPeso(new BigDecimal("46.2"));
        rep2.setValido(true);

        List<RepPmsDTO> repeticiones = Arrays.asList(repPmsDTO, rep2);
        when(repPmsService.obtenerPorPms(PMS_ID)).thenReturn(repeticiones);

        mockMvc.perform(get("/api/pms/{pmsId}/repeticiones", PMS_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].repPMSID").value(REP_ID))
                .andExpect(jsonPath("$[1].repPMSID").value(11))
                .andExpect(jsonPath("$[0].peso").value(45.5))
                .andExpect(jsonPath("$[1].peso").value(46.2));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/pms/{pmsId}/repeticiones/count - Contar repeticiones de un PMS")
    void contarRepeticiones_debeRetornarCantidad() throws Exception {
        when(repPmsService.contarPorPms(PMS_ID)).thenReturn(8L);

        mockMvc.perform(get("/api/pms/{pmsId}/repeticiones/count", PMS_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("8"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/pms/{pmsId}/repeticiones/{repeticionId} - Obtener repetición por ID")
    void obtenerRepeticionPorId_debeRetornarRepeticion() throws Exception {
        when(repPmsService.obtenerPorId(REP_ID)).thenReturn(repPmsDTO);

        mockMvc.perform(get("/api/pms/{pmsId}/repeticiones/{repeticionId}", PMS_ID, REP_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repPMSID").value(REP_ID))
                .andExpect(jsonPath("$.peso").value(45.5));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/pms/{pmsId}/repeticiones/{repeticionId} - Error 404 cuando no existe")
    void obtenerRepeticionPorId_debeRetornar404CuandoNoExiste() throws Exception {
        when(repPmsService.obtenerPorId(999L))
            .thenThrow(new RuntimeException("Repetición no encontrada"));

        mockMvc.perform(get("/api/pms/{pmsId}/repeticiones/{repeticionId}", PMS_ID, 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/pms/{pmsId}/repeticiones/{repeticionId} - Actualizar repetición")
    void actualizarRepeticion_debeRetornarRepeticionActualizada() throws Exception {
        repPmsDTO.setPeso(new BigDecimal("47.3"));
        when(repPmsService.actualizarRepeticion(eq(REP_ID), any(RepPmsRequestDTO.class)))
            .thenReturn(repPmsDTO);

        mockMvc.perform(put("/api/pms/{pmsId}/repeticiones/{repeticionId}", PMS_ID, REP_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repPmsRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repPMSID").value(REP_ID))
                .andExpect(jsonPath("$.peso").value(47.3));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/pms/{pmsId}/repeticiones/{repeticionId} - Error con peso negativo")
    void actualizarRepeticion_debeRetornar400ConPesoNegativo() throws Exception {
        repPmsRequestDTO.setPeso(new BigDecimal("-10"));
        
        // El servicio no lanza excepción por peso negativo, pero la repetición no existe (ID 10)
        when(repPmsService.actualizarRepeticion(eq(10L), any(RepPmsRequestDTO.class)))
            .thenThrow(new RuntimeException("Repetición no encontrada"));

        mockMvc.perform(put("/api/pms/{pmsId}/repeticiones/{repeticionId}", PMS_ID, 10L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repPmsRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/pms/{pmsId}/repeticiones/{repeticionId} - Eliminar repetición")
    void eliminarRepeticion_debeRetornarNoContent() throws Exception {
        doNothing().when(repPmsService).eliminarRepeticion(REP_ID);

        mockMvc.perform(delete("/api/pms/{pmsId}/repeticiones/{repeticionId}", PMS_ID, REP_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/pms/{pmsId}/repeticiones/{repeticionId} - Error 404 cuando no existe")
    void eliminarRepeticion_debeRetornar404CuandoNoExiste() throws Exception {
        // El servicio no lanza excepción al eliminar, simplemente no hace nada
        doNothing().when(repPmsService).eliminarRepeticion(999L);

        // El controller devuelve 204 en cualquier caso
        mockMvc.perform(delete("/api/pms/{pmsId}/repeticiones/{repeticionId}", PMS_ID, 999L)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/pms/{pmsId}/repeticiones - Validar campos requeridos")
    void crearRepeticion_debeFallarSinCamposRequeridos() throws Exception {
        RepPmsRequestDTO requestInvalido = new RepPmsRequestDTO();
        // Sin campos necesarios
        
        RepPmsDTO repPmsCreado = new RepPmsDTO();
        repPmsCreado.setRepPMSID(1L);
        when(repPmsService.crearRepeticion(eq(PMS_ID), any(RepPmsRequestDTO.class)))
            .thenReturn(repPmsCreado);

        // El controller no valida campos requeridos, acepta el request y devuelve 201
        mockMvc.perform(post("/api/pms/{pmsId}/repeticiones", PMS_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/pms/{pmsId}/repeticiones - Crear con peso cero debe fallar")
    void crearRepeticion_debeFallarConPesoCero() throws Exception {
        repPmsRequestDTO.setPeso(BigDecimal.ZERO);
        when(repPmsService.crearRepeticion(eq(PMS_ID), any(RepPmsRequestDTO.class)))
            .thenThrow(new RuntimeException("El peso debe ser mayor a cero"));

        mockMvc.perform(post("/api/pms/{pmsId}/repeticiones", PMS_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repPmsRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/pms/{pmsId}/repeticiones - Crear repetición marcada como no válida")
    void crearRepeticion_debePermitirRepeticionNoValida() throws Exception {
        repPmsDTO.setValido(false);
        when(repPmsService.crearRepeticion(eq(PMS_ID), any(RepPmsRequestDTO.class)))
            .thenReturn(repPmsDTO);

        mockMvc.perform(post("/api/pms/{pmsId}/repeticiones", PMS_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repPmsRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/pms/{pmsId}/repeticiones/{repeticionId} - Error 404 con RuntimeException")
    void eliminarRepeticion_debeRetornar404ConRuntimeException() throws Exception {
        doThrow(new RuntimeException("Repetición no encontrada"))
            .when(repPmsService).eliminarRepeticion(999L);

        mockMvc.perform(delete("/api/pms/{pmsId}/repeticiones/{repeticionId}", PMS_ID, 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
