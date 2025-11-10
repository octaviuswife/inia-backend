package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.MalezasCatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.MalezasCatalogoService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MalezasCatalogoController.class)
@DisplayName("Tests de integración para MalezasCatalogoController")
class MalezasCatalogoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MalezasCatalogoService malezasCatalogoService;

    private MalezasCatalogoDTO malezasCatalogoDTO;
    private MalezasCatalogoRequestDTO malezasCatalogoRequestDTO;

    @BeforeEach
    void setUp() {
        malezasCatalogoDTO = new MalezasCatalogoDTO();
        malezasCatalogoDTO.setCatalogoID(1L);
        malezasCatalogoDTO.setNombreComun("Yuyo Colorado");
        malezasCatalogoDTO.setNombreCientifico("Amaranthus quitensis");
        malezasCatalogoDTO.setActivo(true);

        malezasCatalogoRequestDTO = new MalezasCatalogoRequestDTO();
        malezasCatalogoRequestDTO.setNombreComun("Yuyo Colorado");
        malezasCatalogoRequestDTO.setNombreCientifico("Amaranthus quitensis");
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/malezas - Obtener todas las malezas activas")
    void obtenerTodos_debeRetornarListaMalezas() throws Exception {
        List<MalezasCatalogoDTO> malezas = Arrays.asList(malezasCatalogoDTO);
        when(malezasCatalogoService.obtenerTodos()).thenReturn(malezas);

        mockMvc.perform(get("/api/malezas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].catalogoID").value(1))
                .andExpect(jsonPath("$[0].nombreComun").value("Yuyo Colorado"))
                .andExpect(jsonPath("$[0].nombreCientifico").value("Amaranthus quitensis"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/malezas/inactivos - Obtener malezas inactivas")
    void obtenerInactivos_debeRetornarListaMalezasInactivas() throws Exception {
        MalezasCatalogoDTO malezaInactiva = new MalezasCatalogoDTO();
        malezaInactiva.setCatalogoID(2L);
        malezaInactiva.setNombreComun("Maleza Inactiva");
        malezaInactiva.setNombreCientifico("Maleza inactiva");
        malezaInactiva.setActivo(false);

        List<MalezasCatalogoDTO> malezasInactivas = Arrays.asList(malezaInactiva);
        when(malezasCatalogoService.obtenerInactivos()).thenReturn(malezasInactivas);

        mockMvc.perform(get("/api/malezas/inactivos")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].catalogoID").value(2))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/malezas/buscar/comun - Buscar por nombre común")
    void buscarPorNombreComun_debeRetornarMalezasCoincidentes() throws Exception {
        List<MalezasCatalogoDTO> malezas = Arrays.asList(malezasCatalogoDTO);
        when(malezasCatalogoService.buscarPorNombreComun("Yuyo")).thenReturn(malezas);

        mockMvc.perform(get("/api/malezas/buscar/comun")
                .param("nombre", "Yuyo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreComun").value("Yuyo Colorado"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/malezas/buscar/cientifico - Buscar por nombre científico")
    void buscarPorNombreCientifico_debeRetornarMalezasCoincidentes() throws Exception {
        List<MalezasCatalogoDTO> malezas = Arrays.asList(malezasCatalogoDTO);
        when(malezasCatalogoService.buscarPorNombreCientifico("Amaranthus")).thenReturn(malezas);

        mockMvc.perform(get("/api/malezas/buscar/cientifico")
                .param("nombre", "Amaranthus")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreCientifico").value("Amaranthus quitensis"));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/malezas/{id} - Obtener maleza por ID")
    void obtenerPorId_debeRetornarMaleza() throws Exception {
        when(malezasCatalogoService.obtenerPorId(1L)).thenReturn(malezasCatalogoDTO);

        mockMvc.perform(get("/api/malezas/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogoID").value(1))
                .andExpect(jsonPath("$.nombreComun").value("Yuyo Colorado"));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/malezas/{id} - Retornar 404 cuando no existe")
    void obtenerPorId_debeRetornar404CuandoNoExiste() throws Exception {
        when(malezasCatalogoService.obtenerPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/api/malezas/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/malezas - Crear maleza exitosamente")
    void crear_debeRetornarMalezaCreada() throws Exception {
        when(malezasCatalogoService.crear(any(MalezasCatalogoRequestDTO.class))).thenReturn(malezasCatalogoDTO);

        mockMvc.perform(post("/api/malezas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(malezasCatalogoRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogoID").value(1))
                .andExpect(jsonPath("$.nombreComun").value("Yuyo Colorado"))
                .andExpect(jsonPath("$.nombreCientifico").value("Amaranthus quitensis"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/malezas/{id} - Actualizar maleza exitosamente")
    void actualizar_debeRetornarMalezaActualizada() throws Exception {
        MalezasCatalogoDTO actualizado = new MalezasCatalogoDTO();
        actualizado.setCatalogoID(1L);
        actualizado.setNombreComun("Yuyo Colorado Actualizado");
        actualizado.setNombreCientifico("Amaranthus quitensis");
        actualizado.setActivo(true);

        when(malezasCatalogoService.actualizar(eq(1L), any(MalezasCatalogoRequestDTO.class))).thenReturn(actualizado);

        MalezasCatalogoRequestDTO requestDTO = new MalezasCatalogoRequestDTO();
        requestDTO.setNombreComun("Yuyo Colorado Actualizado");
        requestDTO.setNombreCientifico("Amaranthus quitensis");

        mockMvc.perform(put("/api/malezas/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComun").value("Yuyo Colorado Actualizado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/malezas/{id} - Retornar 404 cuando no existe al actualizar")
    void actualizar_debeRetornar404CuandoNoExiste() throws Exception {
        when(malezasCatalogoService.actualizar(eq(999L), any(MalezasCatalogoRequestDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/malezas/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(malezasCatalogoRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/malezas/{id} - Eliminar maleza (soft delete)")
    void eliminar_debeRetornarOk() throws Exception {
        doNothing().when(malezasCatalogoService).eliminar(1L);

        mockMvc.perform(delete("/api/malezas/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/malezas/{id}/reactivar - Reactivar maleza")
    void reactivar_debeRetornarMalezaReactivada() throws Exception {
        when(malezasCatalogoService.reactivar(1L)).thenReturn(malezasCatalogoDTO);

        mockMvc.perform(put("/api/malezas/1/reactivar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogoID").value(1))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/malezas/{id}/reactivar - Retornar 404 cuando no existe al reactivar")
    void reactivar_debeRetornar404CuandoNoExiste() throws Exception {
        when(malezasCatalogoService.reactivar(999L)).thenReturn(null);

        mockMvc.perform(put("/api/malezas/999/reactivar")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/malezas/listado - Obtener malezas paginadas")
    void obtenerMalezasPaginadas_debeRetornarPaginacion() throws Exception {
        Page<MalezasCatalogoDTO> page = new PageImpl<>(
            Arrays.asList(malezasCatalogoDTO),
            PageRequest.of(0, 10),
            1
        );
        
        when(malezasCatalogoService.obtenerMalezasPaginadasConFiltros(any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/malezas/listado")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].catalogoID").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/malezas/listado - Obtener malezas paginadas con filtro de búsqueda")
    void obtenerMalezasPaginadas_conFiltroSearch_debeRetornarResultadosFiltrados() throws Exception {
        Page<MalezasCatalogoDTO> page = new PageImpl<>(
            Arrays.asList(malezasCatalogoDTO),
            PageRequest.of(0, 10),
            1
        );
        
        when(malezasCatalogoService.obtenerMalezasPaginadasConFiltros(any(), eq("Yuyo"), any())).thenReturn(page);

        mockMvc.perform(get("/api/malezas/listado")
                .param("page", "0")
                .param("size", "10")
                .param("search", "Yuyo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreComun").value("Yuyo Colorado"));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/malezas/listado - Obtener malezas paginadas filtradas por estado activo")
    void obtenerMalezasPaginadas_conFiltroActivo_debeRetornarSoloActivas() throws Exception {
        Page<MalezasCatalogoDTO> page = new PageImpl<>(
            Arrays.asList(malezasCatalogoDTO),
            PageRequest.of(0, 10),
            1
        );
        
        when(malezasCatalogoService.obtenerMalezasPaginadasConFiltros(any(), any(), eq(true))).thenReturn(page);

        mockMvc.perform(get("/api/malezas/listado")
                .param("page", "0")
                .param("size", "10")
                .param("activo", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].activo").value(true));
    }
}