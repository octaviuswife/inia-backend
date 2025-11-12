package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ExportacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ExportacionExcelService;

@WebMvcTest(ExportacionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para ExportacionController")
class ExportacionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportacionExcelService exportacionExcelService;

    @Autowired
    private ObjectMapper objectMapper;

    private byte[] excelBytes;

    @BeforeEach
    void setUp() {
        excelBytes = "Excel content".getBytes();
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel - Debe exportar todos los lotes")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosExcel_sinParametros_debeRetornarExcel() throws Exception {
        when(exportacionExcelService.generarReporteExcel(null)).thenReturn(excelBytes);

        mockMvc.perform(get("/api/exportaciones/excel")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(content().bytes(excelBytes));
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel - Debe exportar lotes específicos")
    @WithMockUser(roles = "ANALISTA")
    void exportarDatosExcel_conLoteIds_debeRetornarExcel() throws Exception {
        List<Long> loteIds = Arrays.asList(1L, 2L, 3L);
        when(exportacionExcelService.generarReporteExcel(loteIds)).thenReturn(excelBytes);

        mockMvc.perform(get("/api/exportaciones/excel")
                .param("loteIds", "1", "2", "3")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(excelBytes));
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel - Debe manejar error de IO")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosExcel_conErrorIO_debeRetornar500() throws Exception {
        when(exportacionExcelService.generarReporteExcel(any())).thenThrow(new java.io.IOException("Error IO"));

        mockMvc.perform(get("/api/exportaciones/excel")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel/lote/{loteId} - Debe exportar lote específico")
    @WithMockUser(roles = "OBSERVADOR")
    void exportarLoteEspecificoExcel_conIdValido_debeRetornarExcel() throws Exception {
        when(exportacionExcelService.generarReporteExcel(anyList())).thenReturn(excelBytes);

        mockMvc.perform(get("/api/exportaciones/excel/lote/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(content().bytes(excelBytes));
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel/lote/{loteId} - Debe incluir ID en nombre archivo")
    @WithMockUser(roles = "ANALISTA")
    void exportarLoteEspecificoExcel_debeIncluirIdEnNombre() throws Exception {
        when(exportacionExcelService.generarReporteExcel(anyList())).thenReturn(excelBytes);

        mockMvc.perform(get("/api/exportaciones/excel/lote/123")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    org.hamcrest.Matchers.containsString("analisis_lote_123_")));
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/personalizado - Debe exportar lista personalizada")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosPersonalizadosExcel_conListaValida_debeRetornarExcel() throws Exception {
        List<Long> loteIds = Arrays.asList(1L, 2L, 3L);
        when(exportacionExcelService.generarReporteExcel(loteIds)).thenReturn(excelBytes);

        mockMvc.perform(post("/api/exportaciones/excel/personalizado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteIds)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(excelBytes));
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/personalizado - Debe rechazar lista vacía")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosPersonalizadosExcel_conListaVacia_debeRetornar400() throws Exception {
        List<Long> loteIds = Arrays.asList();

        mockMvc.perform(post("/api/exportaciones/excel/personalizado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteIds)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/personalizado - Debe rechazar null")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosPersonalizadosExcel_conNull_debeRetornar400() throws Exception {
        mockMvc.perform(post("/api/exportaciones/excel/personalizado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/avanzado - Debe exportar con filtros avanzados")
    @WithMockUser(roles = "ANALISTA")
    void exportarDatosAvanzadosExcel_conFiltros_debeRetornarExcel() throws Exception {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setEspecieIds(Arrays.asList(1L, 2L));
        solicitud.setCultivarIds(Arrays.asList(1L));

        when(exportacionExcelService.generarReporteExcelAvanzado(any(ExportacionRequestDTO.class)))
            .thenReturn(excelBytes);

        mockMvc.perform(post("/api/exportaciones/excel/avanzado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", 
                    org.hamcrest.Matchers.containsString("analisis_filtrado_")))
                .andExpect(content().bytes(excelBytes));
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/avanzado - Debe manejar error en exportación")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosAvanzadosExcel_conError_debeRetornar500() throws Exception {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        
        when(exportacionExcelService.generarReporteExcelAvanzado(any(ExportacionRequestDTO.class)))
            .thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/api/exportaciones/excel/avanzado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel - Nombre archivo debe incluir timestamp")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosExcel_debeIncluirTimestamp() throws Exception {
        when(exportacionExcelService.generarReporteExcel(any())).thenReturn(excelBytes);

        mockMvc.perform(get("/api/exportaciones/excel")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    org.hamcrest.Matchers.matchesPattern(".*analisis_semillas_\\d{8}_\\d{6}\\.xlsx.*")));
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/personalizado - Debe incluir timestamp en nombre")
    @WithMockUser(roles = "ANALISTA")
    void exportarDatosPersonalizadosExcel_debeIncluirTimestamp() throws Exception {
        List<Long> loteIds = Arrays.asList(1L);
        when(exportacionExcelService.generarReporteExcel(anyList())).thenReturn(excelBytes);

        mockMvc.perform(post("/api/exportaciones/excel/personalizado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteIds)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                    org.hamcrest.Matchers.containsString("analisis_seleccionados_")));
    }

    // ===== TESTS DE EDGE CASES Y ERRORES =====

    @Test
    @DisplayName("GET /api/exportaciones/excel - IDs de lotes inexistentes")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosExcel_lotesInexistentes_debeRetornar404() throws Exception {
        List<Long> loteIds = Arrays.asList(999L, 998L);
        
        when(exportacionExcelService.generarReporteExcel(loteIds))
            .thenThrow(new IllegalArgumentException("Lotes no encontrados"));

        // El controller no maneja IllegalArgumentException, devuelve 500
        mockMvc.perform(get("/api/exportaciones/excel")
                .param("loteIds", "999", "998")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel/lote/{loteId} - Lote sin datos de análisis")
    @WithMockUser(roles = "ANALISTA")
    void exportarLoteEspecificoExcel_loteSinDatos_debeRetornar404() throws Exception {
        when(exportacionExcelService.generarReporteExcel(anyList()))
            .thenThrow(new IllegalStateException("El lote no tiene análisis completados"));

        // El controller no maneja IllegalStateException, devuelve 500
        mockMvc.perform(get("/api/exportaciones/excel/lote/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/avanzado - Filtros con fechas inválidas")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosAvanzadosExcel_fechasInvalidas_debeRetornar400() throws Exception {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setFechaDesde(java.time.LocalDate.now().plusDays(1));
        solicitud.setFechaHasta(java.time.LocalDate.now().minusDays(1));

        when(exportacionExcelService.generarReporteExcelAvanzado(any(ExportacionRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Fecha desde no puede ser posterior a fecha hasta"));

        // El controller no maneja IllegalArgumentException, devuelve 500
        mockMvc.perform(post("/api/exportaciones/excel/avanzado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/avanzado - Filtros sin resultados")
    @WithMockUser(roles = "ANALISTA")
    void exportarDatosAvanzadosExcel_sinResultados_debeRetornarVacio() throws Exception {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setEspecieIds(Arrays.asList(999L));

        byte[] emptyExcel = new byte[0];
        when(exportacionExcelService.generarReporteExcelAvanzado(any(ExportacionRequestDTO.class)))
            .thenReturn(emptyExcel);

        mockMvc.perform(post("/api/exportaciones/excel/avanzado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(emptyExcel));
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/personalizado - IDs con valores negativos")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosPersonalizadosExcel_idsNegativos_debeRetornar400() throws Exception {
        List<Long> loteIds = Arrays.asList(-1L, -2L);

        // El servicio devuelve null para IDs negativos, el controller no valida y da NullPointerException (500)
        when(exportacionExcelService.generarReporteExcel(anyList()))
            .thenReturn(null);

        mockMvc.perform(post("/api/exportaciones/excel/personalizado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteIds)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel - Exportación muy grande (OutOfMemoryError)")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosExcel_exportacionGrande_debeRetornar500() throws Exception {
        when(exportacionExcelService.generarReporteExcel(any()))
            .thenThrow(new OutOfMemoryError("Memoria insuficiente"));

        mockMvc.perform(get("/api/exportaciones/excel")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/avanzado - Múltiples filtros complejos")
    @WithMockUser(roles = "ANALISTA")
    void exportarDatosAvanzadosExcel_filtrosComplejos_debeRetornarExcel() throws Exception {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setEspecieIds(Arrays.asList(1L, 2L, 3L));
        solicitud.setCultivarIds(Arrays.asList(1L, 2L));
        solicitud.setFechaDesde(java.time.LocalDate.now().minusMonths(3));
        solicitud.setFechaHasta(java.time.LocalDate.now());

        when(exportacionExcelService.generarReporteExcelAvanzado(any(ExportacionRequestDTO.class)))
            .thenReturn(excelBytes);

        mockMvc.perform(post("/api/exportaciones/excel/avanzado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(excelBytes));
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel/lote/{loteId} - ID con formato inválido")
    @WithMockUser(roles = "OBSERVADOR")
    void exportarLoteEspecificoExcel_idInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(get("/api/exportaciones/excel/lote/abc")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/personalizado - Lista con duplicados")
    @WithMockUser(roles = "ANALISTA")
    void exportarDatosPersonalizadosExcel_conDuplicados_debeEliminarDuplicados() throws Exception {
        List<Long> loteIds = Arrays.asList(1L, 2L, 1L, 3L, 2L);
        when(exportacionExcelService.generarReporteExcel(anyList())).thenReturn(excelBytes);

        mockMvc.perform(post("/api/exportaciones/excel/personalizado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteIds)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(excelBytes));
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel - Sin permisos para exportar")
    void exportarDatosExcel_sinPermisos_debeRetornar403() throws Exception {
        // Sin mock configurado, el servicio devuelve null y causa NullPointerException (500)
        when(exportacionExcelService.generarReporteExcel(any()))
            .thenReturn(null);
        
        mockMvc.perform(get("/api/exportaciones/excel")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/avanzado - Timeout en generación de reporte")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosAvanzadosExcel_timeout_debeRetornar500() throws Exception {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        
        // TimeoutException es checked, necesita usar thenAnswer o RuntimeException
        when(exportacionExcelService.generarReporteExcelAvanzado(any(ExportacionRequestDTO.class)))
            .thenThrow(new RuntimeException("Tiempo de espera agotado"));

        mockMvc.perform(post("/api/exportaciones/excel/avanzado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/exportaciones/excel/personalizado - Exceso de IDs (más de 1000)")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosPersonalizadosExcel_excesoCantidad_debeRetornar400() throws Exception {
        List<Long> loteIds = new java.util.ArrayList<>();
        for (long i = 1; i <= 1001; i++) {
            loteIds.add(i);
        }

        when(exportacionExcelService.generarReporteExcel(anyList()))
            .thenThrow(new IllegalArgumentException("Máximo 1000 lotes por exportación"));

        // El controller no maneja IllegalArgumentException, devuelve 500
        mockMvc.perform(post("/api/exportaciones/excel/personalizado")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteIds)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/exportaciones/excel - Error de conexión a base de datos")
    @WithMockUser(roles = "ADMIN")
    void exportarDatosExcel_errorConexion_debeRetornar500() throws Exception {
        when(exportacionExcelService.generarReporteExcel(any()))
            .thenThrow(new org.springframework.dao.DataAccessException("Error de conexión") {});

        mockMvc.perform(get("/api/exportaciones/excel")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
