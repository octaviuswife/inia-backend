package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.NotificacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para NotificacionService
 * 
 * Funcionalidades testeadas:
 * - Creación de notificaciones manuales
 * - Notificaciones automáticas (registro, aprobación, rechazo de usuarios)
 * - Notificaciones de análisis (finalizado, aprobado, a repetir)
 * - Listado de notificaciones
 * - Marcar como leídas
 * - Eliminar notificaciones
 * - Contar notificaciones no leídas
 * - Seguridad de acceso a notificaciones
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de NotificacionService")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @Mock
    private AnalisisHistorialRepository analisisHistorialRepository;

    @Mock
    private NotificationWebSocketService wsService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificacionService notificacionService;

    private Usuario usuarioTest;
    private Usuario adminTest;
    private Lote loteTest;
    private Analisis analisisTest;

    @BeforeEach
    void setUp() {
        // Usuario normal
        usuarioTest = new Usuario();
        usuarioTest.setUsuarioID(1);
        usuarioTest.setNombre("testuser");
        usuarioTest.setNombres("Test");
        usuarioTest.setApellidos("User");
        usuarioTest.setRol(Rol.ANALISTA);
        usuarioTest.setEstado(EstadoUsuario.ACTIVO);

        // Usuario administrador
        adminTest = new Usuario();
        adminTest.setUsuarioID(2);
        adminTest.setNombre("admin");
        adminTest.setNombres("Admin");
        adminTest.setApellidos("User");
        adminTest.setRol(Rol.ADMIN);
        adminTest.setEstado(EstadoUsuario.ACTIVO);

        // Lote de prueba
        loteTest = new Lote();
        loteTest.setLoteID(1L);
        loteTest.setFicha("LOTE-2024-001");

        // Análisis de prueba
        analisisTest = new Germinacion();
        analisisTest.setAnalisisID(1L);
        analisisTest.setLote(loteTest);
    }

    @Test
    @DisplayName("Crear notificación manual - debe crear correctamente")
    void crearNotificacion_debeCrearCorrectamente() {
        // ARRANGE
        NotificacionRequestDTO request = new NotificacionRequestDTO();
        request.setUsuarioId(1L);
        request.setNombre("Notificación de prueba");
        request.setMensaje("Mensaje de prueba");
        request.setTipo(TipoNotificacion.USUARIO_REGISTRO);

        Notificacion notificacionGuardada = new Notificacion();
        notificacionGuardada.setId(1L);
        notificacionGuardada.setNombre(request.getNombre());
        notificacionGuardada.setMensaje(request.getMensaje());
        notificacionGuardada.setUsuario(usuarioTest);
        notificacionGuardada.setTipo(request.getTipo());

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionGuardada);

        // ACT
        NotificacionDTO resultado = notificacionService.crearNotificacion(request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals("Notificación de prueba", resultado.getNombre());
        assertEquals("Mensaje de prueba", resultado.getMensaje());
        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Crear notificación - usuario no encontrado debe lanzar excepción")
    void crearNotificacion_usuarioNoEncontrado_debeLanzarExcepcion() {
        // ARRANGE
        NotificacionRequestDTO request = new NotificacionRequestDTO();
        request.setUsuarioId(999L);
        request.setNombre("Test");
        request.setMensaje("Test");

        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, 
            () -> notificacionService.crearNotificacion(request));
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Notificar nuevo usuario - debe notificar a todos los administradores")
    void notificarNuevoUsuario_debeNotificarATodosLosAdministradores() {
        // ARRANGE
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsuarioID(3);
        nuevoUsuario.setNombres("Nuevo");
        nuevoUsuario.setApellidos("Usuario");

        when(usuarioRepository.findById(3)).thenReturn(Optional.of(nuevoUsuario));
        when(usuarioRepository.findByEstado(EstadoUsuario.ACTIVO))
            .thenReturn(Arrays.asList(adminTest, usuarioTest)); // 1 admin, 1 analista
        when(notificacionRepository.save(any(Notificacion.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionRepository.countByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(anyInt()))
            .thenReturn(1L);

        // ACT
        notificacionService.notificarNuevoUsuario(3L);

        // ASSERT
        verify(notificacionRepository, times(1)).save(any(Notificacion.class)); // Solo al admin
        verify(wsService, times(1)).sendToUser(any(), any(NotificacionDTO.class));
    }

    @Test
    @DisplayName("Notificar usuario aprobado - debe enviar notificación al usuario")
    void notificarUsuarioAprobado_debeEnviarNotificacionAlUsuario() {
        // ARRANGE
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));
        when(notificacionRepository.save(any(Notificacion.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionRepository.countByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(1))
            .thenReturn(1L);

        // ACT
        notificacionService.notificarUsuarioAprobado(1L);

        // ASSERT
        verify(notificacionRepository).save(any(Notificacion.class));
        verify(wsService).sendToUser(eq(1), any(NotificacionDTO.class));
        verify(wsService).sendUnreadCount(eq(1), eq(1L));
    }

    @Test
    @DisplayName("Notificar usuario rechazado - debe enviar notificación al usuario")
    void notificarUsuarioRechazado_debeEnviarNotificacionAlUsuario() {
        // ARRANGE
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioTest));
        when(notificacionRepository.save(any(Notificacion.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionRepository.countByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(1))
            .thenReturn(1L);

        // ACT
        notificacionService.notificarUsuarioRechazado(1L);

        // ASSERT
        verify(notificacionRepository).save(any(Notificacion.class));
        verify(wsService).sendToUser(eq(1), any(NotificacionDTO.class));
    }

    @Test
    @DisplayName("Notificar análisis finalizado - debe notificar a todos los administradores")
    void notificarAnalisisFinalizado_debeNotificarATodosLosAdministradores() {
        // ARRANGE
        when(analisisRepository.findById(1L)).thenReturn(Optional.of(analisisTest));
        when(usuarioRepository.findByEstado(EstadoUsuario.ACTIVO))
            .thenReturn(Collections.singletonList(adminTest));
        when(notificacionRepository.save(any(Notificacion.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificacionRepository.countByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(2))
            .thenReturn(1L);

        // ACT
        notificacionService.notificarAnalisisFinalizado(1L);

        // ASSERT
        verify(notificacionRepository).save(any(Notificacion.class));
        verify(wsService).sendToUser(eq(2), any(NotificacionDTO.class));
    }

    @Test
    @DisplayName("Obtener notificaciones por usuario - debe retornar página de notificaciones")
    void obtenerNotificacionesPorUsuario_debeRetornarPagina() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        
        Notificacion notif1 = new Notificacion();
        notif1.setId(1L);
        notif1.setNombre("Notificación 1");
        notif1.setMensaje("Mensaje 1");
        notif1.setUsuario(usuarioTest);
        notif1.setLeido(false);
        notif1.setActivo(true);
        notif1.setTipo(TipoNotificacion.USUARIO_APROBADO);

        Notificacion notif2 = new Notificacion();
        notif2.setId(2L);
        notif2.setNombre("Notificación 2");
        notif2.setMensaje("Mensaje 2");
        notif2.setUsuario(usuarioTest);
        notif2.setLeido(true);
        notif2.setActivo(true);
        notif2.setTipo(TipoNotificacion.ANALISIS_APROBADO);

        Page<Notificacion> page = new PageImpl<>(Arrays.asList(notif1, notif2));

        when(notificacionRepository.findByUsuarioUsuarioIDAndActivoTrueOrderByFechaCreacionDesc(1, pageable))
            .thenReturn(page);

        // ACT
        Page<NotificacionDTO> resultado = notificacionService.obtenerNotificacionesPorUsuario(1L, pageable);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.getContent().size());
        assertEquals("Notificación 1", resultado.getContent().get(0).getNombre());
    }

    @Test
    @DisplayName("Obtener notificaciones no leídas - debe retornar solo las no leídas")
    void obtenerNotificacionesNoLeidas_debeRetornarSoloNoLeidas() {
        // ARRANGE
        Notificacion notif1 = new Notificacion();
        notif1.setId(1L);
        notif1.setNombre("No leída 1");
        notif1.setMensaje("Mensaje 1");
        notif1.setUsuario(usuarioTest);
        notif1.setLeido(false);
        notif1.setActivo(true);
        notif1.setTipo(TipoNotificacion.USUARIO_APROBADO);

        when(notificacionRepository.findByUsuarioUsuarioIDAndLeidoFalseAndActivoTrueOrderByFechaCreacionDesc(1))
            .thenReturn(Collections.singletonList(notif1));

        // ACT
        List<NotificacionDTO> resultado = notificacionService.obtenerNotificacionesNoLeidas(1L);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("No leída 1", resultado.get(0).getNombre());
        assertFalse(resultado.get(0).getLeido());
    }

    @Test
    @DisplayName("Marcar como leída - debe actualizar el estado")
    void marcarComoLeida_debeActualizarEstado() {
        // ARRANGE
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(usuarioRepository.findByNombre("testuser")).thenReturn(Optional.of(usuarioTest));

        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setNombre("Test");
        notificacion.setMensaje("Mensaje");
        notificacion.setUsuario(usuarioTest);
        notificacion.setLeido(false);
        notificacion.setActivo(true);
        notificacion.setTipo(TipoNotificacion.USUARIO_APROBADO);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(Notificacion.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        NotificacionDTO resultado = notificacionService.marcarComoLeida(1L);

        // ASSERT
        assertNotNull(resultado);
        assertTrue(notificacion.getLeido());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    @DisplayName("Marcar todas como leídas - debe actualizar todas las notificaciones")
    void marcarTodasComoLeidas_debeActualizarTodas() {
        // ARRANGE
        Notificacion notif1 = new Notificacion();
        notif1.setLeido(false);
        
        Notificacion notif2 = new Notificacion();
        notif2.setLeido(false);

        when(notificacionRepository.findByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(1))
            .thenReturn(Arrays.asList(notif1, notif2));

        // ACT
        notificacionService.marcarTodasComoLeidas(1L);

        // ASSERT
        assertTrue(notif1.getLeido());
        assertTrue(notif2.getLeido());
        verify(notificacionRepository).saveAll(any());
    }

    @Test
    @DisplayName("Eliminar notificación - debe marcar como inactiva")
    void eliminarNotificacion_debeMarcarComoInactiva() {
        // ARRANGE
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(usuarioRepository.findByNombre("testuser")).thenReturn(Optional.of(usuarioTest));

        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setUsuario(usuarioTest);
        notificacion.setActivo(true);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        // ACT
        notificacionService.eliminarNotificacion(1L);

        // ASSERT
        assertFalse(notificacion.getActivo());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    @DisplayName("Contar notificaciones no leídas - debe retornar cantidad correcta")
    void contarNotificacionesNoLeidas_debeRetornarCantidadCorrecta() {
        // ARRANGE
        when(notificacionRepository.countByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(1))
            .thenReturn(5L);

        // ACT
        Long count = notificacionService.contarNotificacionesNoLeidas(1L);

        // ASSERT
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("Eliminar notificación de otro usuario - debe lanzar excepción")
    void eliminarNotificacion_deOtroUsuario_debeLanzarExcepcion() {
        // ARRANGE
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(usuarioRepository.findByNombre("testuser")).thenReturn(Optional.of(usuarioTest));

        Usuario otroUsuario = new Usuario();
        otroUsuario.setUsuarioID(2);
        otroUsuario.setRol(Rol.ANALISTA);

        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setUsuario(otroUsuario);
        notificacion.setActivo(true);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, 
            () -> notificacionService.eliminarNotificacion(1L));
        verify(notificacionRepository, never()).save(any(Notificacion.class));
    }
}
