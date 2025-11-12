package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.CatalogoService;

@WebMvcTest(CatalogoController.class)
@DisplayName("Tests de integración para CatalogoController")
class CatalogoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoService catalogoService;

    @Autowired
    private ObjectMapper objectMapper;

    private CatalogoRequestDTO catalogoRequest;
    private CatalogoDTO catalogoResponse;

    @BeforeEach
    void setUp() {
        catalogoRequest = new CatalogoRequestDTO();
        catalogoRequest.setTipo("DEPOSITO");
        catalogoRequest.setValor("Depósito Central");

        catalogoResponse = new CatalogoDTO();
        catalogoResponse.setId(1L);
        catalogoResponse.setTipo("DEPOSITO");
        catalogoResponse.setValor("Depósito Central");
        catalogoResponse.setActivo(true);
    }

    @Test
    @DisplayName("POST /api/catalogo - Debe crear un catálogo exitosamente")
    @WithMockUser(roles = "ADMIN")
    void crearCatalogo_conDatosValidos_debeRetornarCreado() throws Exception {
        when(catalogoService.crear(any(CatalogoRequestDTO.class))).thenReturn(catalogoResponse);

        mockMvc.perform(post("/api/catalogo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.valor").value("Depósito Central"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("GET /api/catalogo - Debe listar todos los catálogos")
    @WithMockUser(roles = "ANALISTA")
    void obtenerTodos_debeRetornarListaDeCatalogos() throws Exception {
        CatalogoDTO catalogo2 = new CatalogoDTO();
        catalogo2.setId(2L);
        catalogo2.setTipo("ORIGEN");
        catalogo2.setValor("Nacional");
        catalogo2.setActivo(true);

        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse, catalogo2);
        when(catalogoService.obtenerTodos()).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    @DisplayName("GET /api/catalogo/{id} - Debe obtener catálogo por ID")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdExistente_debeRetornarCatalogo() throws Exception {
        when(catalogoService.obtenerPorId(1L)).thenReturn(catalogoResponse);

        mockMvc.perform(get("/api/catalogo/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.valor").value("Depósito Central"));
    }

    @Test
    @DisplayName("GET /api/catalogo/tipo/{tipo} - Debe filtrar catálogos por tipo")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorTipo_conTipoValido_debeRetornarCatalogosFiltrados() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("DEPOSITO", null)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/tipo/DEPOSITO")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("DEPOSITO"));
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id} - Debe actualizar un catálogo existente")
    @WithMockUser(roles = "ADMIN")
    void actualizarCatalogo_conDatosValidos_debeRetornarActualizado() throws Exception {
        catalogoResponse.setValor("Depósito Actualizado");
        when(catalogoService.actualizar(eq(1L), any(CatalogoRequestDTO.class))).thenReturn(catalogoResponse);

        catalogoRequest.setValor("Depósito Actualizado");

        mockMvc.perform(put("/api/catalogo/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.valor").value("Depósito Actualizado"));
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id}/reactivar - Debe reactivar un catálogo")
    @WithMockUser(roles = "ADMIN")
    void reactivarCatalogo_conIdValido_debeRetornarReactivado() throws Exception {
        catalogoResponse.setActivo(true);
        when(catalogoService.reactivar(1L)).thenReturn(catalogoResponse);

        mockMvc.perform(put("/api/catalogo/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("DELETE /api/catalogo/{id} - Debe desactivar un catálogo")
    @WithMockUser(roles = "ADMIN")
    void eliminarCatalogo_conIdValido_debeDesactivar() throws Exception {
        doNothing().when(catalogoService).eliminar(1L);

        mockMvc.perform(delete("/api/catalogo/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/tipo/{tipo} - Con filtro activo=true")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorTipo_conFiltroActivo_debeRetornarSoloActivos() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("DEPOSITO", true)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/tipo/DEPOSITO")
                .param("activo", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @DisplayName("GET /api/catalogo/tipo/{tipo} - Debe retornar 400 con tipo inválido")
    @WithMockUser(roles = "ANALISTA")
    void obtenerPorTipo_conTipoInvalido_debeRetornar400() throws Exception {
        when(catalogoService.obtenerPorTipo("TIPO_INVALIDO", null))
                .thenThrow(new IllegalArgumentException("Tipo inválido"));

        mockMvc.perform(get("/api/catalogo/tipo/TIPO_INVALIDO")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/catalogo/{id} - Debe retornar 404 si no existe")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerPorId_conIdInexistente_debeRetornar404() throws Exception {
        when(catalogoService.obtenerPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/api/catalogo/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/catalogo - Debe retornar 400 con tipo inválido")
    @WithMockUser(roles = "ADMIN")
    void crearCatalogo_conTipoInvalido_debeRetornar400() throws Exception {
        when(catalogoService.crear(any(CatalogoRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Tipo inválido"));

        mockMvc.perform(post("/api/catalogo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/catalogo - Debe retornar 400 con datos duplicados")
    @WithMockUser(roles = "ADMIN")
    void crearCatalogo_conDatosDuplicados_debeRetornar400() throws Exception {
        when(catalogoService.crear(any(CatalogoRequestDTO.class)))
                .thenThrow(new RuntimeException("El valor ya existe"));

        mockMvc.perform(post("/api/catalogo")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id} - Debe retornar 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void actualizarCatalogo_conIdInexistente_debeRetornar404() throws Exception {
        when(catalogoService.actualizar(eq(999L), any(CatalogoRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/catalogo/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id} - Debe retornar 400 con tipo inválido")
    @WithMockUser(roles = "ADMIN")
    void actualizarCatalogo_conTipoInvalido_debeRetornar400() throws Exception {
        when(catalogoService.actualizar(eq(1L), any(CatalogoRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Tipo inválido"));

        mockMvc.perform(put("/api/catalogo/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id} - Debe retornar 400 con error de validación")
    @WithMockUser(roles = "ADMIN")
    void actualizarCatalogo_conErrorValidacion_debeRetornar400() throws Exception {
        when(catalogoService.actualizar(eq(1L), any(CatalogoRequestDTO.class)))
                .thenThrow(new RuntimeException("El valor ya existe"));

        mockMvc.perform(put("/api/catalogo/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(catalogoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/catalogo/{id}/reactivar - Debe retornar 404 si no existe")
    @WithMockUser(roles = "ADMIN")
    void reactivarCatalogo_conIdInexistente_debeRetornar404() throws Exception {
        when(catalogoService.reactivar(999L)).thenReturn(null);

        mockMvc.perform(put("/api/catalogo/999/reactivar")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/catalogo/{id}/fisico - Debe eliminar físicamente")
    @WithMockUser(roles = "ADMIN")
    void eliminarFisicamente_conIdValido_debeEliminar() throws Exception {
        doNothing().when(catalogoService).eliminarFisicamente(1L);

        mockMvc.perform(delete("/api/catalogo/1/fisico")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/humedad - Debe obtener tipos de humedad")
    @WithMockUser(roles = "ANALISTA")
    void obtenerTiposHumedad_debeRetornarCatalogos() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("HUMEDAD", true)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/humedad")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/articulos - Debe obtener números de artículo")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerNumerosArticulo_debeRetornarCatalogos() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("ARTICULO", true)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/articulos")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/origenes - Debe obtener orígenes")
    @WithMockUser(roles = "ADMIN")
    void obtenerOrigenes_debeRetornarCatalogos() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("ORIGEN", true)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/origenes")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/estados - Debe obtener estados")
    @WithMockUser(roles = "ANALISTA")
    void obtenerEstados_debeRetornarCatalogos() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("ESTADO", true)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/estados")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/depositos - Debe obtener depósitos")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerDepositos_debeRetornarCatalogos() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("DEPOSITO", true)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/depositos")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/unidades-embolsado - Debe obtener unidades de embolsado")
    @WithMockUser(roles = "ADMIN")
    void obtenerUnidadesEmbolsado_debeRetornarCatalogos() throws Exception {
        List<CatalogoDTO> catalogos = Arrays.asList(catalogoResponse);
        when(catalogoService.obtenerPorTipo("UNIDAD_EMBOLSADO", true)).thenReturn(catalogos);

        mockMvc.perform(get("/api/catalogo/unidades-embolsado")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/catalogo/listado - Debe obtener catálogos paginados sin filtros")
    @WithMockUser(roles = "ANALISTA")
    void obtenerCatalogosPaginados_sinFiltros_debeRetornarPagina() throws Exception {
        org.springframework.data.domain.Page<CatalogoDTO> page = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(catalogoResponse));
        
        when(catalogoService.obtenerCatalogosPaginadosConFiltros(
                any(org.springframework.data.domain.Pageable.class), 
                eq(null), 
                eq(null), 
                eq(null)))
            .thenReturn(page);

        mockMvc.perform(get("/api/catalogo/listado")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/catalogo/listado - Con búsqueda y filtros")
    @WithMockUser(roles = "ADMIN")
    void obtenerCatalogosPaginados_conFiltros_debeRetornarPaginaFiltrada() throws Exception {
        org.springframework.data.domain.Page<CatalogoDTO> page = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(catalogoResponse));
        
        when(catalogoService.obtenerCatalogosPaginadosConFiltros(
                any(org.springframework.data.domain.Pageable.class), 
                eq("Depósito"), 
                eq(true), 
                eq("DEPOSITO")))
            .thenReturn(page);

        mockMvc.perform(get("/api/catalogo/listado")
                .param("page", "0")
                .param("size", "10")
                .param("search", "Depósito")
                .param("activo", "true")
                .param("tipo", "DEPOSITO")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipo").value("DEPOSITO"));
    }

    @Test
    @DisplayName("GET /api/catalogo/listado - Con parámetros personalizados")
    @WithMockUser(roles = "OBSERVADOR")
    void obtenerCatalogosPaginados_conParametrosPersonalizados_debeRetornarPagina() throws Exception {
        org.springframework.data.domain.Page<CatalogoDTO> page = 
            new org.springframework.data.domain.PageImpl<>(Arrays.asList(catalogoResponse));
        
        when(catalogoService.obtenerCatalogosPaginadosConFiltros(
                any(org.springframework.data.domain.Pageable.class), 
                eq(null), 
                eq(false), 
                eq(null)))
            .thenReturn(page);

        mockMvc.perform(get("/api/catalogo/listado")
                .param("page", "1")
                .param("size", "20")
                .param("activo", "false")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
