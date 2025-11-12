package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.NotificacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion;
import utec.proyectofinal.Proyecto.Final.UTEC.services.NotificacionService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacionController.class)
@DisplayName("Tests de integración para NotificacionController")
class NotificacionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificacionService notificacionService;

    private NotificacionDTO notificacionDTO;
    private NotificacionRequestDTO notificacionRequestDTO;

    @BeforeEach
    void setUp() {
        notificacionDTO = new NotificacionDTO();
        notificacionDTO.setId(1L);
        notificacionDTO.setNombre("Test Notificación");
        notificacionDTO.setMensaje("Mensaje de prueba");
        notificacionDTO.setUsuarioId(1L);
        notificacionDTO.setLeido(false);
        notificacionDTO.setFechaCreacion(LocalDateTime.now());
        notificacionDTO.setTipo(TipoNotificacion.USUARIO_REGISTRO);
        notificacionDTO.setActivo(true);

        notificacionRequestDTO = new NotificacionRequestDTO();
        notificacionRequestDTO.setNombre("Test Notificación");
        notificacionRequestDTO.setMensaje("Mensaje de prueba");
        notificacionRequestDTO.setUsuarioId(1L);
        notificacionRequestDTO.setTipo(TipoNotificacion.USUARIO_REGISTRO);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/notificaciones - Crear notificación manual")
    void crearNotificacion_debeRetornarNotificacionCreada() throws Exception {
        when(notificacionService.crearNotificacion(any(NotificacionRequestDTO.class))).thenReturn(notificacionDTO);

        mockMvc.perform(post("/api/notificaciones")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacionRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Test Notificación"))
                .andExpect(jsonPath("$.mensaje").value("Mensaje de prueba"))
                .andExpect(jsonPath("$.leido").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/notificaciones/usuario/{usuarioId} - Obtener notificaciones paginadas de un usuario")
    void obtenerNotificacionesPorUsuario_debeRetornarPaginacion() throws Exception {
        Page<NotificacionDTO> page = new PageImpl<>(
            Arrays.asList(notificacionDTO),
            PageRequest.of(0, 10),
            1
        );
        
        when(notificacionService.obtenerNotificacionesPorUsuarioConValidacion(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/notificaciones/usuario/1")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].usuarioId").value(1));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/notificaciones/mis-notificaciones - Obtener mis notificaciones paginadas")
    void obtenerMisNotificaciones_debeRetornarPaginacion() throws Exception {
        Page<NotificacionDTO> page = new PageImpl<>(
            Arrays.asList(notificacionDTO),
            PageRequest.of(0, 10),
            1
        );
        
        when(notificacionService.obtenerMisNotificaciones(any())).thenReturn(page);

        mockMvc.perform(get("/api/notificaciones/mis-notificaciones")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/notificaciones/usuario/{usuarioId}/no-leidas - Obtener notificaciones no leídas de un usuario")
    void obtenerNotificacionesNoLeidas_debeRetornarLista() throws Exception {
        NotificacionDTO notificacion1 = new NotificacionDTO();
        notificacion1.setId(1L);
        notificacion1.setLeido(false);
        
        NotificacionDTO notificacion2 = new NotificacionDTO();
        notificacion2.setId(2L);
        notificacion2.setLeido(false);

        List<NotificacionDTO> noLeidas = Arrays.asList(notificacion1, notificacion2);
        when(notificacionService.obtenerNotificacionesNoLeidasConValidacion(1L)).thenReturn(noLeidas);

        mockMvc.perform(get("/api/notificaciones/usuario/1/no-leidas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leido").value(false))
                .andExpect(jsonPath("$[1].leido").value(false))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/notificaciones/mis-notificaciones/no-leidas - Obtener mis notificaciones no leídas")
    void obtenerMisNotificacionesNoLeidas_debeRetornarLista() throws Exception {
        List<NotificacionDTO> noLeidas = Arrays.asList(notificacionDTO);
        when(notificacionService.obtenerMisNotificacionesNoLeidas()).thenReturn(noLeidas);

        mockMvc.perform(get("/api/notificaciones/mis-notificaciones/no-leidas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leido").value(false))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/notificaciones/usuario/{usuarioId}/contar-no-leidas - Contar notificaciones no leídas")
    void contarNotificacionesNoLeidas_debeRetornarConteo() throws Exception {
        when(notificacionService.contarNotificacionesNoLeidasConValidacion(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/notificaciones/usuario/1/contar-no-leidas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/notificaciones/mis-notificaciones/contar-no-leidas - Contar mis notificaciones no leídas")
    void contarMisNotificacionesNoLeidas_debeRetornarConteo() throws Exception {
        when(notificacionService.contarMisNotificacionesNoLeidas()).thenReturn(3L);

        mockMvc.perform(get("/api/notificaciones/mis-notificaciones/contar-no-leidas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/notificaciones/{notificacionId}/marcar-leida - Marcar notificación como leída")
    void marcarComoLeida_debeRetornarNotificacionActualizada() throws Exception {
        NotificacionDTO notificacionLeida = new NotificacionDTO();
        notificacionLeida.setId(1L);
        notificacionLeida.setLeido(true);
        
        when(notificacionService.marcarComoLeida(1L)).thenReturn(notificacionLeida);

        mockMvc.perform(put("/api/notificaciones/1/marcar-leida")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.leido").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/notificaciones/usuario/{usuarioId}/marcar-todas-leidas - Marcar todas como leídas de un usuario")
    void marcarTodasComoLeidas_debeRetornarOk() throws Exception {
        doNothing().when(notificacionService).marcarTodasComoLeidasConValidacion(1L);

        mockMvc.perform(put("/api/notificaciones/usuario/1/marcar-todas-leidas")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("PUT /api/notificaciones/mis-notificaciones/marcar-todas-leidas - Marcar todas mis notificaciones como leídas")
    void marcarTodasMisNotificacionesComoLeidas_debeRetornarOk() throws Exception {
        doNothing().when(notificacionService).marcarTodasMisNotificacionesComoLeidas();

        mockMvc.perform(put("/api/notificaciones/mis-notificaciones/marcar-todas-leidas")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("DELETE /api/notificaciones/{notificacionId} - Eliminar notificación")
    void eliminarNotificacion_debeRetornarOk() throws Exception {
        doNothing().when(notificacionService).eliminarNotificacion(1L);

        mockMvc.perform(delete("/api/notificaciones/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/notificaciones/mis-notificaciones - Observador puede ver sus notificaciones")
    void obtenerMisNotificaciones_conRolObservador_debeRetornarOk() throws Exception {
        Page<NotificacionDTO> page = new PageImpl<>(
            Arrays.asList(notificacionDTO),
            PageRequest.of(0, 10),
            1
        );
        
        when(notificacionService.obtenerMisNotificaciones(any())).thenReturn(page);

        mockMvc.perform(get("/api/notificaciones/mis-notificaciones")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/notificaciones/mis-notificaciones - Con paginación personalizada")
    void obtenerMisNotificaciones_conPaginacionPersonalizada_debeRetornarOk() throws Exception {
        Page<NotificacionDTO> page = new PageImpl<>(
            Arrays.asList(notificacionDTO),
            PageRequest.of(2, 5),
            20
        );
        
        when(notificacionService.obtenerMisNotificaciones(any())).thenReturn(page);

        mockMvc.perform(get("/api/notificaciones/mis-notificaciones")
                .param("page", "2")
                .param("size", "5")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/notificaciones/usuario/{usuarioId} - Con paginación personalizada")
    void obtenerNotificacionesPorUsuario_conPaginacionPersonalizada_debeRetornarOk() throws Exception {
        Page<NotificacionDTO> page = new PageImpl<>(
            Arrays.asList(notificacionDTO),
            PageRequest.of(1, 15),
            30
        );
        
        when(notificacionService.obtenerNotificacionesPorUsuarioConValidacion(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/notificaciones/usuario/1")
                .param("page", "1")
                .param("size", "15")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/notificaciones/mis-notificaciones/contar-no-leidas - Observador puede contar sus notificaciones")
    void contarMisNotificacionesNoLeidas_conRolObservador_debeRetornarOk() throws Exception {
        when(notificacionService.contarMisNotificacionesNoLeidas()).thenReturn(2L);

        mockMvc.perform(get("/api/notificaciones/mis-notificaciones/contar-no-leidas")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("PUT /api/notificaciones/{notificacionId}/marcar-leida - Observador puede marcar como leída")
    void marcarComoLeida_conRolObservador_debeRetornarOk() throws Exception {
        NotificacionDTO notificacionLeida = new NotificacionDTO();
        notificacionLeida.setId(1L);
        notificacionLeida.setLeido(true);
        
        when(notificacionService.marcarComoLeida(1L)).thenReturn(notificacionLeida);

        mockMvc.perform(put("/api/notificaciones/1/marcar-leida")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leido").value(true));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("DELETE /api/notificaciones/{notificacionId} - Observador puede eliminar notificación")
    void eliminarNotificacion_conRolObservador_debeRetornarOk() throws Exception {
        doNothing().when(notificacionService).eliminarNotificacion(1L);

        mockMvc.perform(delete("/api/notificaciones/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
