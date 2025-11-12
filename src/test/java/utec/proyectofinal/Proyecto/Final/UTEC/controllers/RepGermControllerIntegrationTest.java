package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepGermService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RepGermController.class)
@DisplayName("Tests de integración para RepGermController")
class RepGermControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RepGermService repGermService;

    private RepGermDTO repGermDTO;
    private RepGermRequestDTO repGermRequestDTO;
    private Long germinacionId = 1L;
    private Long tablaId = 1L;
    private Long repeticionId = 1L;

    @BeforeEach
    void setUp() {
        repGermDTO = new RepGermDTO();
        repGermDTO.setRepGermID(repeticionId);
        repGermDTO.setTablaGermId(tablaId);
        repGermDTO.setNormales(Arrays.asList(50, 45, 48, 52));
        repGermDTO.setAnormales(10);
        repGermDTO.setDuras(5);
        repGermDTO.setFrescas(3);
        repGermDTO.setMuertas(2);
        repGermDTO.setTotal(100);

        repGermRequestDTO = new RepGermRequestDTO();
        repGermRequestDTO.setNormales(Arrays.asList(50, 45, 48, 52));
        repGermRequestDTO.setAnormales(10);
        repGermRequestDTO.setDuras(5);
        repGermRequestDTO.setFrescas(3);
        repGermRequestDTO.setMuertas(2);
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion - Crear repetición exitosamente")
    void crearRepGerm_debeRetornarRepeticionCreada() throws Exception {
        when(repGermService.crearRepGerm(eq(tablaId), any(RepGermRequestDTO.class))).thenReturn(repGermDTO);

        mockMvc.perform(post("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion", germinacionId, tablaId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repGermRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.repGermID").value(repeticionId))
                .andExpect(jsonPath("$.tablaGermId").value(tablaId))
                .andExpect(jsonPath("$.normales[0]").value(50))
                .andExpect(jsonPath("$.anormales").value(10))
                .andExpect(jsonPath("$.total").value(100));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion - Error al superar máximo de repeticiones")
    void crearRepGerm_debeRetornarBadRequestCuandoSuperaMaximo() throws Exception {
        when(repGermService.crearRepGerm(eq(tablaId), any(RepGermRequestDTO.class)))
            .thenThrow(new RuntimeException("No se pueden agregar más repeticiones. El número máximo de repeticiones permitidas es: 4"));

        mockMvc.perform(post("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion", germinacionId, tablaId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repGermRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId} - Obtener repetición por ID")
    void obtenerRepGermPorId_debeRetornarRepeticion() throws Exception {
        when(repGermService.obtenerRepGermPorId(repeticionId)).thenReturn(repGermDTO);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId}", 
                germinacionId, tablaId, repeticionId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repGermID").value(repeticionId))
                .andExpect(jsonPath("$.normales[0]").value(50));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId} - Retornar 404 cuando no existe")
    void obtenerRepGermPorId_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        when(repGermService.obtenerRepGermPorId(999L))
            .thenThrow(new RuntimeException("Repetición no encontrada con ID: 999"));

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId}", 
                germinacionId, tablaId, 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId} - Actualizar repetición")
    void actualizarRepGerm_debeRetornarRepeticionActualizada() throws Exception {
        RepGermDTO actualizado = new RepGermDTO();
        actualizado.setRepGermID(repeticionId);
        actualizado.setNormales(Arrays.asList(60, 58, 62, 61));
        actualizado.setAnormales(15);
        actualizado.setDuras(3);
        actualizado.setFrescas(2);
        actualizado.setMuertas(0);
        actualizado.setTotal(100);

        when(repGermService.actualizarRepGerm(eq(repeticionId), any(RepGermRequestDTO.class))).thenReturn(actualizado);

        RepGermRequestDTO actualizarDTO = new RepGermRequestDTO();
        actualizarDTO.setNormales(Arrays.asList(60, 58, 62, 61));
        actualizarDTO.setAnormales(15);
        actualizarDTO.setDuras(3);
        actualizarDTO.setFrescas(2);
        actualizarDTO.setMuertas(0);

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId}", 
                germinacionId, tablaId, repeticionId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normales[0]").value(60));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId} - Error cuando no existe")
    void actualizarRepGerm_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        when(repGermService.actualizarRepGerm(eq(999L), any(RepGermRequestDTO.class)))
            .thenThrow(new RuntimeException("Repetición no encontrada con ID: 999"));

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId}", 
                germinacionId, tablaId, 999L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repGermRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId} - Eliminar repetición")
    void eliminarRepGerm_debeRetornarOk() throws Exception {
        doNothing().when(repGermService).eliminarRepGerm(repeticionId);

        mockMvc.perform(delete("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId}", 
                germinacionId, tablaId, repeticionId)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId} - Error cuando no existe")
    void eliminarRepGerm_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        doNothing().when(repGermService).eliminarRepGerm(999L);

        mockMvc.perform(delete("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId}", 
                germinacionId, tablaId, 999L)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion - Obtener todas las repeticiones de una tabla")
    void obtenerRepeticionesPorTabla_debeRetornarLista() throws Exception {
        RepGermDTO rep1 = new RepGermDTO();
        rep1.setRepGermID(1L);
        rep1.setNormales(Arrays.asList(50, 48, 52, 51));
        
        RepGermDTO rep2 = new RepGermDTO();
        rep2.setRepGermID(2L);
        rep2.setNormales(Arrays.asList(55, 53, 57, 54));

        List<RepGermDTO> repeticiones = Arrays.asList(rep1, rep2);
        when(repGermService.obtenerRepeticionesPorTabla(tablaId)).thenReturn(repeticiones);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion", germinacionId, tablaId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].repGermID").value(1))
                .andExpect(jsonPath("$[0].normales[0]").value(50))
                .andExpect(jsonPath("$[1].repGermID").value(2))
                .andExpect(jsonPath("$[1].normales[0]").value(55))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/contar - Contar repeticiones de una tabla")
    void contarRepeticionesPorTabla_debeRetornarConteo() throws Exception {
        when(repGermService.contarRepeticionesPorTabla(tablaId)).thenReturn(4L);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/contar", germinacionId, tablaId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion - Crear repetición con datos completos")
    void crearRepGerm_conDatosCompletos_debeRetornarRepeticionCreada() throws Exception {
        RepGermRequestDTO requestCompleto = new RepGermRequestDTO();
        requestCompleto.setNormales(Arrays.asList(80, 78, 82, 81));
        requestCompleto.setAnormales(5);
        requestCompleto.setDuras(2);
        requestCompleto.setFrescas(1);
        requestCompleto.setMuertas(0);

        RepGermDTO responseCompleto = new RepGermDTO();
        responseCompleto.setRepGermID(1L);
        responseCompleto.setTablaGermId(tablaId);
        responseCompleto.setNormales(Arrays.asList(80, 78, 82, 81));
        responseCompleto.setAnormales(5);
        responseCompleto.setDuras(2);
        responseCompleto.setFrescas(1);
        responseCompleto.setMuertas(0);
        responseCompleto.setTotal(100);

        when(repGermService.crearRepGerm(eq(tablaId), any(RepGermRequestDTO.class))).thenReturn(responseCompleto);

        mockMvc.perform(post("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion", germinacionId, tablaId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCompleto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.normales[0]").value(80))
                .andExpect(jsonPath("$.anormales").value(5));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion - Lista vacía cuando no hay repeticiones")
    void obtenerRepeticionesPorTabla_debeRetornarListaVaciaCuandoNoHayRepeticiones() throws Exception {
        when(repGermService.obtenerRepeticionesPorTabla(tablaId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion", germinacionId, tablaId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/contar - Contar cero repeticiones")
    void contarRepeticionesPorTabla_debeRetornarCeroCuandoNoHayRepeticiones() throws Exception {
        when(repGermService.contarRepeticionesPorTabla(tablaId)).thenReturn(0L);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/contar", germinacionId, tablaId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion - Error al obtener lista")
    void obtenerRepeticionesPorTabla_debeRetornarBadRequestConError() throws Exception {
        when(repGermService.obtenerRepeticionesPorTabla(tablaId))
            .thenThrow(new RuntimeException("Error al obtener repeticiones"));

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion", germinacionId, tablaId)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/contar - Error al contar")
    void contarRepeticionesPorTabla_debeRetornarBadRequestConError() throws Exception {
        when(repGermService.contarRepeticionesPorTabla(tablaId))
            .thenThrow(new RuntimeException("Error al contar repeticiones"));

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/contar", germinacionId, tablaId)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId} - Error al eliminar con excepción")
    void eliminarRepGerm_debeRetornarNotFoundConExcepcion() throws Exception {
        doThrow(new RuntimeException("Repetición no encontrada"))
            .when(repGermService).eliminarRepGerm(999L);

        mockMvc.perform(delete("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion/{repeticionId}", 
                germinacionId, tablaId, 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}

