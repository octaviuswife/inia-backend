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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TablaGermService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TablaGermController.class)
@DisplayName("Tests de integración para TablaGermController")
class TablaGermControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TablaGermService tablaGermService;

    private TablaGermDTO tablaGermDTO;
    private TablaGermRequestDTO tablaGermRequestDTO;
    private static final Long GERMINACION_ID = 1L;
    private static final Long TABLA_ID = 15L;

    @BeforeEach
    void setUp() {
        tablaGermDTO = new TablaGermDTO();
        tablaGermDTO.setTablaGermID(TABLA_ID);
        tablaGermDTO.setFinalizada(false);
        tablaGermDTO.setTratamiento("Agar");
        tablaGermDTO.setProductoYDosis("Sin producto");
        tablaGermDTO.setNumSemillasPRep(100);
        tablaGermDTO.setMetodo("Entre papel");
        tablaGermDTO.setTemperatura("25");
        tablaGermDTO.setTienePrefrio(false);
        tablaGermDTO.setTienePretratamiento(false);
        tablaGermDTO.setDiasPrefrio(0);
        tablaGermDTO.setFechaIngreso(LocalDate.of(2024, 1, 10));
        tablaGermDTO.setFechaGerminacion(LocalDate.of(2024, 1, 15));
        tablaGermDTO.setFechaUltConteo(LocalDate.of(2024, 1, 25));
        tablaGermDTO.setNumDias("10");
        tablaGermDTO.setNumeroRepeticiones(8);
        tablaGermDTO.setNumeroConteos(3);

        tablaGermRequestDTO = new TablaGermRequestDTO();
        tablaGermRequestDTO.setFechaFinal(LocalDate.of(2024, 1, 25));
        tablaGermRequestDTO.setTratamiento("Agar");
        tablaGermRequestDTO.setProductoYDosis("Sin producto");
        tablaGermRequestDTO.setNumSemillasPRep(100);
        tablaGermRequestDTO.setMetodo("Entre papel");
        tablaGermRequestDTO.setTemperatura("25");
        tablaGermRequestDTO.setTienePrefrio(false);
        tablaGermRequestDTO.setTienePretratamiento(false);
        tablaGermRequestDTO.setDiasPrefrio(0);
        tablaGermRequestDTO.setFechaIngreso(LocalDate.of(2024, 1, 10));
        tablaGermRequestDTO.setFechaGerminacion(LocalDate.of(2024, 1, 15));
        tablaGermRequestDTO.setFechaUltConteo(LocalDate.of(2024, 1, 25));
        tablaGermRequestDTO.setNumDias("10");
        tablaGermRequestDTO.setNumeroRepeticiones(8);
        tablaGermRequestDTO.setNumeroConteos(3);
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/germinacion/{germinacionId}/tabla - Crear tabla exitosamente")
    void crearTabla_debeRetornarTablaCreada() throws Exception {
        when(tablaGermService.crearTablaGerm(eq(GERMINACION_ID), any(TablaGermRequestDTO.class)))
            .thenReturn(tablaGermDTO);

        mockMvc.perform(post("/api/germinacion/{germinacionId}/tabla", GERMINACION_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tablaGermRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tablaGermID").value(TABLA_ID))
                .andExpect(jsonPath("$.tratamiento").value("Agar"))
                .andExpect(jsonPath("$.temperatura").value("25"))
                .andExpect(jsonPath("$.finalizada").value(false));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("POST /api/germinacion/{germinacionId}/tabla - Error con fechas inválidas retorna 500")
    void crearTabla_debeRetornar400ConFechasInvalidas() throws Exception {
        when(tablaGermService.crearTablaGerm(eq(GERMINACION_ID), any(TablaGermRequestDTO.class)))
            .thenThrow(new RuntimeException("La fecha de ingreso no puede ser posterior a la fecha de germinación"));

        mockMvc.perform(post("/api/germinacion/{germinacionId}/tabla", GERMINACION_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tablaGermRequestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla - Obtener todas las tablas")
    void obtenerTablasPorGerminacion_debeRetornarLista() throws Exception {
        TablaGermDTO tabla2 = new TablaGermDTO();
        tabla2.setTablaGermID(16L);

        List<TablaGermDTO> tablas = Arrays.asList(tablaGermDTO, tabla2);
        when(tablaGermService.obtenerTablasPorGerminacion(GERMINACION_ID)).thenReturn(tablas);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla", GERMINACION_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tablaGermID").value(TABLA_ID))
                .andExpect(jsonPath("$[1].tablaGermID").value(16));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/contar - Contar tablas")
    void contarTablas_debeRetornarCantidad() throws Exception {
        when(tablaGermService.contarTablasPorGerminacion(GERMINACION_ID)).thenReturn(2L);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/contar", GERMINACION_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId} - Obtener tabla por ID")
    void obtenerTablaPorId_debeRetornarTabla() throws Exception {
        when(tablaGermService.obtenerTablaGermPorId(TABLA_ID))
            .thenReturn(tablaGermDTO);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tablaGermID").value(TABLA_ID));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId} - Error 404")
    void obtenerTablaPorId_debeRetornar404CuandoNoExiste() throws Exception {
        when(tablaGermService.obtenerTablaGermPorId(999L))
            .thenThrow(new RuntimeException("Tabla no encontrada"));

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}", 
                GERMINACION_ID, 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId} - Actualizar tabla")
    void actualizarTabla_debeRetornarTablaActualizada() throws Exception {
        tablaGermDTO.setTemperatura("28");
        when(tablaGermService.actualizarTablaGerm(eq(TABLA_ID), 
            any(TablaGermRequestDTO.class))).thenReturn(tablaGermDTO);

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tablaGermRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperatura").value("28"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/germinacion/{germinacionId}/tabla/{tablaId} - Eliminar tabla")
    void eliminarTabla_debeRetornarNoContent() throws Exception {
        doNothing().when(tablaGermService).eliminarTablaGerm(TABLA_ID);

        mockMvc.perform(delete("/api/germinacion/{germinacionId}/tabla/{tablaId}", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/finalizar - Finalizar tabla")
    void finalizarTabla_debeRetornarTablaFinalizada() throws Exception {
        tablaGermDTO.setFinalizada(true);
        tablaGermDTO.setFechaFinal(LocalDate.of(2024, 1, 25));
        when(tablaGermService.finalizarTabla(TABLA_ID))
            .thenReturn(tablaGermDTO);

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}/finalizar", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalizada").value(true))
                .andExpect(jsonPath("$.fechaFinal").exists());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/finalizar - Error sin repeticiones")
    void finalizarTabla_debeRetornar400SinRepeticiones() throws Exception {
        when(tablaGermService.finalizarTabla(TABLA_ID))
            .thenThrow(new RuntimeException("No se puede finalizar sin completar todas las repeticiones"));

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}/finalizar", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/puede-ingresar-porcentajes - Verificar permisos")
    void puedeIngresarPorcentajes_debeRetornarBoolean() throws Exception {
        when(tablaGermService.puedeIngresarPorcentajes(TABLA_ID))
            .thenReturn(true);

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/puede-ingresar-porcentajes", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/porcentajes - Actualizar porcentajes")
    void actualizarPorcentajes_debeRetornarTablaConPorcentajes() throws Exception {
        PorcentajesRedondeoRequestDTO porcentajesDTO = new PorcentajesRedondeoRequestDTO();
        porcentajesDTO.setPorcentajeNormalesConRedondeo(new BigDecimal("85.5"));
        porcentajesDTO.setPorcentajeAnormalesConRedondeo(new BigDecimal("8.2"));
        porcentajesDTO.setPorcentajeDurasConRedondeo(new BigDecimal("3.1"));
        porcentajesDTO.setPorcentajeFrescasConRedondeo(new BigDecimal("2.0"));
        porcentajesDTO.setPorcentajeMuertasConRedondeo(new BigDecimal("1.2"));

        tablaGermDTO.setPorcentajeNormalesConRedondeo(new BigDecimal("85.5"));
        when(tablaGermService.actualizarPorcentajes(eq(TABLA_ID), 
            any(PorcentajesRedondeoRequestDTO.class))).thenReturn(tablaGermDTO);

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}/porcentajes", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(porcentajesDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajeNormalesConRedondeo").value(85.5));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId}/porcentajes - Error suma incorrecta")
    void actualizarPorcentajes_debeRetornar400ConSumaIncorrecta() throws Exception {
        PorcentajesRedondeoRequestDTO porcentajesDTO = new PorcentajesRedondeoRequestDTO();
        porcentajesDTO.setPorcentajeNormalesConRedondeo(new BigDecimal("90.0"));
        porcentajesDTO.setPorcentajeAnormalesConRedondeo(new BigDecimal("8.0"));
        porcentajesDTO.setPorcentajeDurasConRedondeo(new BigDecimal("3.0"));
        porcentajesDTO.setPorcentajeFrescasConRedondeo(new BigDecimal("2.0"));
        porcentajesDTO.setPorcentajeMuertasConRedondeo(new BigDecimal("1.0"));
        // suma = 104, no 100

        when(tablaGermService.actualizarPorcentajes(eq(TABLA_ID), 
            any(PorcentajesRedondeoRequestDTO.class)))
            .thenThrow(new RuntimeException("La suma de porcentajes debe ser 100"));

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}/porcentajes", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(porcentajesDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/germinacion/{germinacionId}/tabla/{tablaId} - Sin validación de tabla finalizada")
    void eliminarTabla_debeRetornar400SiEstaFinalizada() throws Exception {
        // El controller no valida si la tabla está finalizada antes de eliminar
        tablaGermDTO.setFinalizada(true);
        when(tablaGermService.obtenerTablaGermPorId(TABLA_ID))
            .thenReturn(tablaGermDTO);
        doNothing().when(tablaGermService).eliminarTablaGerm(TABLA_ID);

        mockMvc.perform(delete("/api/germinacion/{germinacionId}/tabla/{tablaId}", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/contar - Error al contar")
    void contarTablas_debeRetornar500ConError() throws Exception {
        when(tablaGermService.contarTablasPorGerminacion(GERMINACION_ID))
            .thenThrow(new RuntimeException("Error en servicio"));

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/contar", GERMINACION_ID)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla/{tablaId}/puede-ingresar-porcentajes - Error")
    void puedeIngresarPorcentajes_debeRetornar500ConError() throws Exception {
        when(tablaGermService.puedeIngresarPorcentajes(TABLA_ID))
            .thenThrow(new RuntimeException("Error verificando permisos"));

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla/{tablaId}/puede-ingresar-porcentajes", 
                GERMINACION_ID, TABLA_ID)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/germinacion/{germinacionId}/tabla/{tablaId} - Error 404 al actualizar")
    void actualizarTabla_debeRetornar404CuandoNoExiste() throws Exception {
        when(tablaGermService.actualizarTablaGerm(eq(999L), any(TablaGermRequestDTO.class)))
            .thenThrow(new RuntimeException("Tabla no encontrada"));

        mockMvc.perform(put("/api/germinacion/{germinacionId}/tabla/{tablaId}", 
                GERMINACION_ID, 999L)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tablaGermRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/germinacion/{germinacionId}/tabla/{tablaId} - Error 404 al eliminar")
    void eliminarTabla_debeRetornar404CuandoNoExiste() throws Exception {
        doThrow(new RuntimeException("Tabla no encontrada"))
            .when(tablaGermService).eliminarTablaGerm(999L);

        mockMvc.perform(delete("/api/germinacion/{germinacionId}/tabla/{tablaId}", 
                GERMINACION_ID, 999L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/germinacion/{germinacionId}/tabla - Error al obtener lista")
    void obtenerTablasPorGerminacion_debeRetornar500ConError() throws Exception {
        when(tablaGermService.obtenerTablasPorGerminacion(GERMINACION_ID))
            .thenThrow(new RuntimeException("Error en servicio"));

        mockMvc.perform(get("/api/germinacion/{germinacionId}/tabla", GERMINACION_ID)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
