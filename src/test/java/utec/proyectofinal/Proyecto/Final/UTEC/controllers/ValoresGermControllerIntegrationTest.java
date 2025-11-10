package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ValoresGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ValoresGermService;

@WebMvcTest(ValoresGermController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para ValoresGermController")
class ValoresGermControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValoresGermService valoresGermService;

    @Autowired
    private ObjectMapper objectMapper;

    private ValoresGermRequestDTO requestDTO;
    private ValoresGermDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new ValoresGermRequestDTO();
        requestDTO.setNormales(new BigDecimal("80.00"));
        requestDTO.setAnormales(new BigDecimal("10.00"));
        requestDTO.setDuras(new BigDecimal("5.00"));
        requestDTO.setFrescas(new BigDecimal("3.00"));
        requestDTO.setMuertas(new BigDecimal("2.00"));
        requestDTO.setGerminacion(new BigDecimal("90.00"));

        responseDTO = new ValoresGermDTO();
        responseDTO.setValoresGermID(1L);
        responseDTO.setInstituto(Instituto.INIA);
        responseDTO.setNormales(new BigDecimal("80.00"));
        responseDTO.setAnormales(new BigDecimal("10.00"));
        responseDTO.setDuras(new BigDecimal("5.00"));
        responseDTO.setFrescas(new BigDecimal("3.00"));
        responseDTO.setMuertas(new BigDecimal("2.00"));
        responseDTO.setGerminacion(new BigDecimal("90.00"));
        responseDTO.setTablaGermId(1L);
    }

    @Test
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/valores/{valoresId} - Debe obtener valores por ID")
    @WithMockUser(roles = "ANALISTA")
    void obtenerValoresPorId_conIdExistente_debeRetornarValores() throws Exception {
        when(valoresGermService.obtenerValoresPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/germinacion/1/tabla/1/valores/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoresGermID").value(1L))
                .andExpect(jsonPath("$.instituto").value("INIA"))
                .andExpect(jsonPath("$.normales").value(80.00))
                .andExpect(jsonPath("$.tablaGermId").value(1L));
    }

    @Test
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/valores/{valoresId} - Debe retornar 404 cuando no existe")
    @WithMockUser(roles = "ANALISTA")
    void obtenerValoresPorId_conIdInexistente_debeRetornar404() throws Exception {
        when(valoresGermService.obtenerValoresPorId(999L))
            .thenThrow(new RuntimeException("Valores de germinación no encontrados con ID: 999"));

        mockMvc.perform(get("/api/germinacion/1/tabla/1/valores/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/valores/{valoresId} - Debe actualizar valores exitosamente")
    @WithMockUser(roles = "ADMIN")
    void actualizarValores_conDatosValidos_debeRetornarActualizado() throws Exception {
        responseDTO.setNormales(new BigDecimal("75.00"));
        when(valoresGermService.actualizarValores(eq(1L), any(ValoresGermRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/germinacion/1/tabla/1/valores/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valoresGermID").value(1L))
                .andExpect(jsonPath("$.instituto").value("INIA"));
    }

    @Test
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/valores/{valoresId} - Debe rechazar suma mayor a 100")
    @WithMockUser(roles = "ADMIN")
    void actualizarValores_conSumaSuperior100_debeRetornarError() throws Exception {
        when(valoresGermService.actualizarValores(eq(1L), any(ValoresGermRequestDTO.class)))
            .thenThrow(new RuntimeException("La suma de los valores no puede superar 100"));

        mockMvc.perform(put("/api/germinacion/1/tabla/1/valores/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/valores - Debe obtener todos los valores de una tabla")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerValoresPorTabla_debeRetornarListaDeValores() throws Exception {
        ValoresGermDTO valoresInase = new ValoresGermDTO();
        valoresInase.setValoresGermID(2L);
        valoresInase.setInstituto(Instituto.INASE);
        valoresInase.setTablaGermId(1L);

        List<ValoresGermDTO> valores = Arrays.asList(responseDTO, valoresInase);
        when(valoresGermService.obtenerValoresPorTabla(1L)).thenReturn(valores);

        mockMvc.perform(get("/api/germinacion/1/tabla/1/valores")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].instituto").value("INIA"))
                .andExpect(jsonPath("$[1].instituto").value("INASE"));
    }

    @Test
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/valores/inia - Debe obtener valores de INIA")
    @WithMockUser(roles = "ANALISTA")
    void obtenerValoresIniaPorTabla_debeRetornarValoresDeInia() throws Exception {
        when(valoresGermService.obtenerValoresIniaPorTabla(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/germinacion/1/tabla/1/valores/inia")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instituto").value("INIA"))
                .andExpect(jsonPath("$.tablaGermId").value(1L));
    }

    @Test
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/valores/inase - Debe obtener valores de INASE")
    @WithMockUser(roles = "ANALISTA")
    void obtenerValoresInasePorTabla_debeRetornarValoresDeInase() throws Exception {
        responseDTO.setInstituto(Instituto.INASE);
        when(valoresGermService.obtenerValoresInasePorTabla(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/germinacion/1/tabla/1/valores/inase")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instituto").value("INASE"))
                .andExpect(jsonPath("$.tablaGermId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/germinacion/{germinacionId}/tabla/{tablaId}/valores/{valoresId} - Debe eliminar valores")
    @WithMockUser(roles = "ADMIN")
    void eliminarValores_conIdValido_debeEliminar() throws Exception {
        doNothing().when(valoresGermService).eliminarValores(1L);

        mockMvc.perform(delete("/api/germinacion/1/tabla/1/valores/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
