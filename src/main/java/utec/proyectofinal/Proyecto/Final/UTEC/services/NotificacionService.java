package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.AnalisisHistorial;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Notificacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisHistorialRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.NotificacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.NotificacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushNotificationRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;

import static utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion.*;

@Service
@Transactional
@Slf4j
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AnalisisRepository analisisRepository;

    @Autowired
    private AnalisisHistorialRepository analisisHistorialRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    /**
     * Crear notificación manual
     * Guarda en BD y envía push notification
     */
    public NotificacionDTO crearNotificacion(NotificacionRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId().intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Notificacion notificacion = new Notificacion();
        notificacion.setNombre(request.getNombre());
        notificacion.setMensaje(request.getMensaje());
        notificacion.setUsuario(usuario);
        notificacion.setAnalisisId(request.getAnalisisId());
        notificacion.setTipo(request.getTipo());

        notificacion = notificacionRepository.save(notificacion);

        log.info("📝 Notificación creada en BD: ID={}, Usuario={}, Tipo={}",
                notificacion.getId(), usuario.getUsuarioID(), request.getTipo());

        // Enviar push notification de forma asíncrona
        enviarPushParaNotificacion(notificacion);

        return convertToDTO(notificacion);
    }

    /**
     * Método auxiliar para enviar push notification cuando se crea una notificación
     * Este método NO lanza excepciones para no interrumpir el flujo principal
     */
    private void enviarPushParaNotificacion(Notificacion notificacion) {
        try {
            // Determinar URL según el tipo de notificación
            String url = determinarUrlNotificacion(notificacion);

            // Construir metadata adicional
            Map<String, String> data = new HashMap<>();
            data.put("notificacionId", notificacion.getId().toString());
            data.put("tipo", notificacion.getTipo() != null ? notificacion.getTipo().toString() : "GENERAL");
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));

            if (notificacion.getAnalisisId() != null) {
                data.put("analisisId", notificacion.getAnalisisId().toString());
            }

            // Crear request de push notification
            PushNotificationRequest pushRequest = PushNotificationRequest.builder()
                    .title(notificacion.getNombre())
                    .body(notificacion.getMensaje())
                    .icon("/icons/icon-192x192.png")
                    .url(url)
                    .tag("notif-" + notificacion.getId())
                    .data(data)
                    .build();

            // Enviar push notification
            pushNotificationService.sendPushNotificationToUser(
                    Long.valueOf(notificacion.getUsuario().getUsuarioID()),
                    pushRequest
            );

            log.info("📲 Push notification enviada para notificación ID={}", notificacion.getId());
        } catch (Exception e) {
            // No fallar si hay error en push notification
            // La notificación ya está guardada en BD
            log.warn("⚠️ Error enviando push notification para notificación {}: {}",
                    notificacion.getId(), e.getMessage());
        }
    }

    /**
     * Determinar URL de destino según el tipo de notificación
     */
    private String determinarUrlNotificacion(Notificacion notificacion) {
        // Si tiene análisis, ir al detalle del análisis
        if (notificacion.getAnalisisId() != null) {
            return "/listado/analisis/" + notificacion.getAnalisisId();
        }

        // Según el tipo de notificación
        if (notificacion.getTipo() != null) {
            switch (notificacion.getTipo()) {
                case USUARIO_REGISTRO:
                case USUARIO_APROBADO:
                case USUARIO_RECHAZADO:
                    return "/administracion/usuarios";

                case ANALISIS_FINALIZADO:
                case ANALISIS_REPETIR:
                    return "/dashboard/analisis-por-aprobar";

                case ANALISIS_APROBADO:
                    return "/listado/analisis";

                default:
                    return "/notificaciones";
            }
        }

        return "/notificaciones";
    }

    /**
     * Determinar si una notificación es urgente (requiere interacción)
     */
    private boolean esNotificacionUrgente(Notificacion notificacion) {
        if (notificacion.getTipo() == null) {
            return false;
        }

        // Solo las notificaciones de análisis para repetir son urgentes
        return notificacion.getTipo() == ANALISIS_REPETIR;
    }

    /**
     * Notificación cuando se registra un nuevo usuario
     */
    public void notificarNuevoUsuario(Long usuarioId) {
        log.info("👤 Creando notificación de nuevo usuario: {}", usuarioId);

        // Buscar todos los administradores activos
        List<Usuario> administradores = usuarioRepository
                .findByEstado(utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario.ACTIVO)
                .stream()
                .filter(u -> u.getRol() == utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .collect(Collectors.toList());

        Usuario usuarioNuevo = usuarioRepository.findById(usuarioId.intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        for (Usuario admin : administradores) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Nuevo usuario registrado");
            notificacion.setMensaje("El usuario " + usuarioNuevo.getNombres() + " " + usuarioNuevo.getApellidos() +
                    " se ha registrado y requiere aprobación.");
            notificacion.setUsuario(admin);
            notificacion.setTipo(USUARIO_REGISTRO);

            notificacion = notificacionRepository.save(notificacion);
            enviarPushParaNotificacion(notificacion);
        }

        log.info("✅ Notificaciones de nuevo usuario enviadas a {} administradores", administradores.size());
    }

    /**
     * Notificación cuando se aprueba un usuario
     */
    public void notificarUsuarioAprobado(Long usuarioId) {
        log.info("✅ Creando notificación de usuario aprobado: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId.intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Notificacion notificacion = new Notificacion();
        notificacion.setNombre("Cuenta aprobada");
        notificacion.setMensaje("Su cuenta ha sido aprobada por el administrador. Ya puede iniciar sesión en el sistema.");
        notificacion.setUsuario(usuario);
        notificacion.setTipo(USUARIO_APROBADO);

        notificacion = notificacionRepository.save(notificacion);
        enviarPushParaNotificacion(notificacion);
    }

    /**
     * Notificación cuando se rechaza un usuario
     */
    public void notificarUsuarioRechazado(Long usuarioId) {
        log.info("❌ Creando notificación de usuario rechazado: {}", usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId.intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Notificacion notificacion = new Notificacion();
        notificacion.setNombre("Solicitud rechazada");
        notificacion.setMensaje("Su solicitud de registro ha sido rechazada por el administrador.");
        notificacion.setUsuario(usuario);
        notificacion.setTipo(USUARIO_RECHAZADO);

        notificacion = notificacionRepository.save(notificacion);
        enviarPushParaNotificacion(notificacion);
    }

    /**
     * Notificación cuando se finaliza un análisis
     */
    public void notificarAnalisisFinalizado(Long analisisId) {
        log.info("🔬 Creando notificación de análisis finalizado: {}", analisisId);

        // Buscar todos los administradores activos
        List<Usuario> administradores = usuarioRepository
                .findByEstado(utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario.ACTIVO)
                .stream()
                .filter(u -> u.getRol() == utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .collect(Collectors.toList());

        Analisis analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado"));

        for (Usuario admin : administradores) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Análisis finalizado");
            notificacion.setMensaje("El análisis ID " + analisisId + " del lote " + analisis.getLote().getFicha() +
                    " ha sido finalizado y requiere aprobación.");
            notificacion.setUsuario(admin);
            notificacion.setAnalisisId(analisisId);
            notificacion.setTipo(ANALISIS_FINALIZADO);

            notificacion = notificacionRepository.save(notificacion);
            enviarPushParaNotificacion(notificacion);
        }

        log.info("✅ Notificaciones de análisis finalizado enviadas a {} administradores", administradores.size());
    }

    /**
     * Notificación cuando se aprueba un análisis
     */
    public void notificarAnalisisAprobado(Long analisisId) {
        log.info("✅ Creando notificación de análisis aprobado: {}", analisisId);

        // Buscar usuarios que modificaron el análisis (excluyendo administradores)
        List<AnalisisHistorial> historial = analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(analisisId);
        List<Usuario> usuariosInvolucrados = historial.stream()
                .map(AnalisisHistorial::getUsuario)
                .filter(usuario -> usuario.getRol() != utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .distinct()
                .collect(Collectors.toList());

        Analisis analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado"));

        for (Usuario usuario : usuariosInvolucrados) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Análisis aprobado");
            notificacion.setMensaje("El análisis ID " + analisisId + " del lote " + analisis.getLote().getFicha() +
                    " ha sido aprobado por el administrador.");
            notificacion.setUsuario(usuario);
            notificacion.setAnalisisId(analisisId);
            notificacion.setTipo(ANALISIS_APROBADO);

            notificacion = notificacionRepository.save(notificacion);
            enviarPushParaNotificacion(notificacion);
        }

        log.info("✅ Notificaciones de análisis aprobado enviadas a {} usuarios", usuariosInvolucrados.size());
    }

    /**
     * Notificación cuando se marca un análisis para repetir
     */
    public void notificarAnalisisRepetir(Long analisisId) {
        log.info("🔄 Creando notificación de análisis para repetir: {}", analisisId);

        // Buscar usuarios que modificaron el análisis (excluyendo administradores)
        List<AnalisisHistorial> historial = analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(analisisId);
        List<Usuario> usuariosInvolucrados = historial.stream()
                .map(AnalisisHistorial::getUsuario)
                .filter(usuario -> usuario.getRol() != utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .distinct()
                .collect(Collectors.toList());

        Analisis analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado"));

        for (Usuario usuario : usuariosInvolucrados) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("⚠️ Análisis marcado para repetir");
            notificacion.setMensaje("El análisis ID " + analisisId + " del lote " + analisis.getLote().getFicha() +
                    " ha sido marcado para repetir por el administrador.");
            notificacion.setUsuario(usuario);
            notificacion.setAnalisisId(analisisId);
            notificacion.setTipo(ANALISIS_REPETIR);

            notificacion = notificacionRepository.save(notificacion);
            enviarPushParaNotificacion(notificacion);
        }

        log.info("✅ Notificaciones de análisis para repetir enviadas a {} usuarios", usuariosInvolucrados.size());
    }

    /**
     * Notificación cuando un análisis editado vuelve a estado pendiente de aprobación
     */
    public void notificarAnalisisPendienteAprobacion(Long analisisId) {
        log.info("🔄 Creando notificación de análisis pendiente de aprobación: {}", analisisId);

        // Buscar todos los administradores activos
        List<Usuario> administradores = usuarioRepository
                .findByEstado(utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario.ACTIVO)
                .stream()
                .filter(u -> u.getRol() == utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .collect(Collectors.toList());

        Analisis analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado"));

        for (Usuario admin : administradores) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Análisis modificado - Requiere nueva aprobación");
            notificacion.setMensaje("El análisis ID " + analisisId + " del lote " + analisis.getLote().getFicha() +
                    " ha sido modificado y requiere nueva aprobación.");
            notificacion.setUsuario(admin);
            notificacion.setAnalisisId(analisisId);
            notificacion.setTipo(ANALISIS_FINALIZADO);

            notificacion = notificacionRepository.save(notificacion);
            enviarPushParaNotificacion(notificacion);
        }

        log.info("✅ Notificaciones de análisis modificado enviadas a {} administradores", administradores.size());
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    /**
     * Obtener notificaciones de un usuario con paginación
     */
    public Page<NotificacionDTO> obtenerNotificacionesPorUsuario(Long usuarioId, Pageable pageable) {
        Page<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioUsuarioIDAndActivoTrueOrderByFechaCreacionDesc(usuarioId.intValue(), pageable);
        return notificaciones.map(this::convertToDTO);
    }

    /**
     * Obtener notificaciones no leídas de un usuario
     */
    public List<NotificacionDTO> obtenerNotificacionesNoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioUsuarioIDAndLeidoFalseAndActivoTrueOrderByFechaCreacionDesc(usuarioId.intValue());
        return notificaciones.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Marcar notificación como leída
     */
    public NotificacionDTO marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        // Validar que el usuario actual puede marcar esta notificación
        Usuario usuarioActual = obtenerUsuarioActual();
        if (!usuarioActual.esAdmin() && !notificacion.getUsuario().getUsuarioID().equals(usuarioActual.getUsuarioID())) {
            throw new RuntimeException("No tiene permisos para marcar esta notificación como leída");
        }

        notificacion.setLeido(true);
        notificacion = notificacionRepository.save(notificacion);

        log.info("✅ Notificación {} marcada como leída por usuario {}",
                notificacionId, usuarioActual.getUsuarioID());

        return convertToDTO(notificacion);
    }

    /**
     * Marcar todas las notificaciones de un usuario como leídas
     */
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(usuarioId.intValue());
        notificaciones.forEach(n -> n.setLeido(true));
        notificacionRepository.saveAll(notificaciones);

        log.info("✅ {} notificaciones marcadas como leídas para usuario {}",
                notificaciones.size(), usuarioId);
    }

    /**
     * Eliminar notificación (marcar como inactiva)
     */
    public void eliminarNotificacion(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));

        // Validar que el usuario actual puede eliminar esta notificación
        Usuario usuarioActual = obtenerUsuarioActual();
        if (!usuarioActual.esAdmin() && !notificacion.getUsuario().getUsuarioID().equals(usuarioActual.getUsuarioID())) {
            throw new RuntimeException("No tiene permisos para eliminar esta notificación");
        }

        notificacion.setActivo(false);
        notificacionRepository.save(notificacion);

        log.info("🗑️ Notificación {} eliminada por usuario {}",
                notificacionId, usuarioActual.getUsuarioID());
    }

    /**
     * Contar notificaciones no leídas
     */
    public Long contarNotificacionesNoLeidas(Long usuarioId) {
        return notificacionRepository
                .countByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(usuarioId.intValue());
    }

    // ==================== MÉTODOS SEGUROS CON USUARIO AUTENTICADO ====================

    /**
     * Obtener usuario actual autenticado
     */
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioRepository.findByNombre(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en base de datos"));
    }

    /**
     * Validar que el usuario actual puede acceder a las notificaciones del usuarioId especificado
     */
    private void validarAccesoANotificaciones(Integer usuarioId) {
        Usuario usuarioActual = obtenerUsuarioActual();

        // Los administradores pueden ver las notificaciones de cualquier usuario
        if (usuarioActual.esAdmin()) {
            return;
        }

        // Los demás usuarios solo pueden ver sus propias notificaciones
        if (!Objects.equals(usuarioActual.getUsuarioID(), usuarioId)) {
            throw new RuntimeException("No tiene permisos para acceder a las notificaciones de este usuario");
        }
    }

    /**
     * Obtener mis notificaciones (usuario autenticado)
     */
    public Page<NotificacionDTO> obtenerMisNotificaciones(Pageable pageable) {
        Usuario usuarioActual = obtenerUsuarioActual();
        Page<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioUsuarioIDAndActivoTrueOrderByFechaCreacionDesc(usuarioActual.getUsuarioID(), pageable);
        return notificaciones.map(this::convertToDTO);
    }

    /**
     * Obtener mis notificaciones no leídas (usuario autenticado)
     */
    public List<NotificacionDTO> obtenerMisNotificacionesNoLeidas() {
        Usuario usuarioActual = obtenerUsuarioActual();
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioUsuarioIDAndLeidoFalseAndActivoTrueOrderByFechaCreacionDesc(usuarioActual.getUsuarioID());
        return notificaciones.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Contar mis notificaciones no leídas (usuario autenticado)
     */
    public Long contarMisNotificacionesNoLeidas() {
        Usuario usuarioActual = obtenerUsuarioActual();
        return notificacionRepository
                .countByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(usuarioActual.getUsuarioID());
    }

    /**
     * Marcar todas mis notificaciones como leídas (usuario autenticado)
     */
    public void marcarTodasMisNotificacionesComoLeidas() {
        Usuario usuarioActual = obtenerUsuarioActual();
        List<Notificacion> notificaciones = notificacionRepository
                .findByUsuarioUsuarioIDAndLeidoFalseAndActivoTrue(usuarioActual.getUsuarioID());
        notificaciones.forEach(n -> n.setLeido(true));
        notificacionRepository.saveAll(notificaciones);

        log.info("✅ Todas las notificaciones marcadas como leídas para usuario {}",
                usuarioActual.getUsuarioID());
    }

    /**
     * Métodos con validación de acceso (para administradores o acceso propio)
     */
    public Page<NotificacionDTO> obtenerNotificacionesPorUsuarioConValidacion(Long usuarioId, Pageable pageable) {
        validarAccesoANotificaciones(usuarioId.intValue());
        return obtenerNotificacionesPorUsuario(usuarioId, pageable);
    }

    public List<NotificacionDTO> obtenerNotificacionesNoLeidasConValidacion(Long usuarioId) {
        validarAccesoANotificaciones(usuarioId.intValue());
        return obtenerNotificacionesNoLeidas(usuarioId);
    }

    public Long contarNotificacionesNoLeidasConValidacion(Long usuarioId) {
        validarAccesoANotificaciones(usuarioId.intValue());
        return contarNotificacionesNoLeidas(usuarioId);
    }

    public void marcarTodasComoLeidasConValidacion(Long usuarioId) {
        validarAccesoANotificaciones(usuarioId.intValue());
        marcarTodasComoLeidas(usuarioId);
    }

    /**
     * Convertir entidad a DTO
     */
    private NotificacionDTO convertToDTO(Notificacion notificacion) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(notificacion.getId());
        dto.setNombre(notificacion.getNombre());
        dto.setMensaje(notificacion.getMensaje());
        dto.setLeido(notificacion.getLeido());
        dto.setActivo(notificacion.getActivo());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        dto.setUsuarioId(notificacion.getUsuario().getUsuarioID().longValue());
        dto.setUsuarioNombre(notificacion.getUsuario().getNombres() + " " + notificacion.getUsuario().getApellidos());
        dto.setAnalisisId(notificacion.getAnalisisId());
        dto.setTipo(notificacion.getTipo());
        return dto;
    }
}