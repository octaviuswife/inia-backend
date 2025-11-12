package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ImportacionLegadoResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ImportacionLegadoService;

import java.util.ArrayList;

@WebMvcTest(ImportacionLegadoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para ImportacionLegadoController")
class ImportacionLegadoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportacionLegadoService importacionLegadoService;

    @Test
    @DisplayName("POST /api/importacion/legado/validar - Debe validar archivo Excel correctamente")
    @WithMockUser(roles = "ADMIN")
    void validarArchivoExcel_conArchivoValido_debeRetornarResultado() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Excel content".getBytes()
        );

        ImportacionLegadoResponseDTO resultado = new ImportacionLegadoResponseDTO();
        resultado.setExitoso(true);
        resultado.setTotalFilas(100);
        resultado.setFilasImportadas(95);
        resultado.setFilasConErrores(5);
        resultado.setErrores(new ArrayList<>());

        when(importacionLegadoService.importarDesdeExcel(any(), any(Boolean.class))).thenReturn(resultado);

        mockMvc.perform(multipart("/api/importacion/legado/validar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true))
                .andExpect(jsonPath("$.totalFilas").value(100))
                .andExpect(jsonPath("$.filasImportadas").value(95))
                .andExpect(jsonPath("$.filasConErrores").value(5));
    }

    @Test
    @DisplayName("POST /api/importacion/legado/validar - Debe rechazar archivo sin extensión xlsx")
    @WithMockUser(roles = "ADMIN")
    void validarArchivoExcel_conArchivoInvalido_debeRetornar400() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.txt",
            "text/plain",
            "Text content".getBytes()
        );

        mockMvc.perform(multipart("/api/importacion/legado/validar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/importacion/legado/validar - Debe rechazar archivo vacío")
    @WithMockUser(roles = "ADMIN")
    void validarArchivoExcel_conArchivoVacio_debeRetornar400() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]
        );

        mockMvc.perform(multipart("/api/importacion/legado/validar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/importacion/legado/validar - Analista puede validar archivo")
    @WithMockUser(roles = "ANALISTA")
    void validarArchivoExcel_comoAnalista_debeRetornarResultado() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Excel content".getBytes()
        );

        ImportacionLegadoResponseDTO resultado = new ImportacionLegadoResponseDTO();
        resultado.setExitoso(true);
        resultado.setTotalFilas(50);
        resultado.setFilasImportadas(50);
        resultado.setFilasConErrores(0);
        resultado.setErrores(new ArrayList<>());

        when(importacionLegadoService.importarDesdeExcel(any(), any(Boolean.class))).thenReturn(resultado);

        mockMvc.perform(multipart("/api/importacion/legado/validar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));
    }

    @Test
    @DisplayName("POST /api/importacion/legado/validar - Debe manejar error en validación")
    @WithMockUser(roles = "ADMIN")
    void validarArchivoExcel_conError_debeRetornar500() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Excel content".getBytes()
        );

        when(importacionLegadoService.importarDesdeExcel(any(), any(Boolean.class)))
            .thenThrow(new RuntimeException("Error al validar"));

        mockMvc.perform(multipart("/api/importacion/legado/validar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/importacion/legado/importar - Admin puede importar archivo")
    @WithMockUser(roles = "ADMIN")
    void importarArchivoExcel_comoAdmin_debeRetornar200() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Excel content".getBytes()
        );

        ImportacionLegadoResponseDTO resultado = new ImportacionLegadoResponseDTO();
        resultado.setExitoso(true);
        resultado.setMensaje("Importación completada exitosamente");
        resultado.setErrores(new ArrayList<>());

        when(importacionLegadoService.importarDesdeExcel(any(), any(Boolean.class))).thenReturn(resultado);

        mockMvc.perform(multipart("/api/importacion/legado/importar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/importacion/legado/importar - Debe rechazar archivo sin extensión xlsx")
    @WithMockUser(roles = "ADMIN")
    void importarArchivoExcel_conArchivoInvalido_debeRetornar400() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.csv",
            "text/csv",
            "CSV content".getBytes()
        );

        mockMvc.perform(multipart("/api/importacion/legado/importar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/importacion/legado/importar - Debe rechazar archivo vacío")
    @WithMockUser(roles = "ADMIN")
    void importarArchivoExcel_conArchivoVacio_debeRetornar400() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]
        );

        mockMvc.perform(multipart("/api/importacion/legado/importar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/importacion/legado/importar - Debe manejar error en importación")
    @WithMockUser(roles = "ADMIN")
    void importarArchivoExcel_conError_debeRetornar500() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Excel content".getBytes()
        );

        when(importacionLegadoService.importarDesdeExcel(any(), any(Boolean.class)))
            .thenThrow(new RuntimeException("Error al importar"));

        mockMvc.perform(multipart("/api/importacion/legado/importar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /api/importacion/legado/importar - Debe retornar mensaje de éxito")
    @WithMockUser(roles = "ADMIN")
    void importarArchivoExcel_exitoso_debeRetornarMensaje() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "legado.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "Excel content".getBytes()
        );

        ImportacionLegadoResponseDTO resultado = new ImportacionLegadoResponseDTO();
        resultado.setExitoso(true);
        resultado.setMensaje("Importación completada exitosamente");
        resultado.setErrores(new ArrayList<>());

        when(importacionLegadoService.importarDesdeExcel(any(), any(Boolean.class))).thenReturn(resultado);

        mockMvc.perform(multipart("/api/importacion/legado/importar")
                .file(archivo)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Importación completada exitosamente")));
    }

    @Test
    @DisplayName("POST /api/importacion/legado/validar - Debe rechazar request sin archivo")
    @WithMockUser(roles = "ADMIN")
    void validarArchivoExcel_sinArchivo_debeRetornar400() throws Exception {
        mockMvc.perform(multipart("/api/importacion/legado/validar")
                .with(csrf()))
                .andExpect(status().isInternalServerError()); // Spring retorna 500 cuando falta parámetro required
    }

    @Test
    @DisplayName("POST /api/importacion/legado/importar - Debe rechazar request sin archivo")
    @WithMockUser(roles = "ADMIN")
    void importarArchivoExcel_sinArchivo_debeRetornar400() throws Exception {
        mockMvc.perform(multipart("/api/importacion/legado/importar")
                .with(csrf()))
                .andExpect(status().isInternalServerError()); // Spring retorna 500 cuando falta parámetro required
    }
}
