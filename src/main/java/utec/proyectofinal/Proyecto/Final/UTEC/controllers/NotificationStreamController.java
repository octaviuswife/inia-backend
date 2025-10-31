package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.services.NotificationStreamService;

import java.io.IOException;

/**
 * Controlador para Server-Sent Events (SSE) de notificaciones en tiempo real
 */
@RestController
@RequestMapping("/v1/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "https://inia.duckdns.org"}, allowCredentials = "true")
public class NotificationStreamController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationStreamController.class);

    @Autowired
    private NotificationStreamService streamService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Endpoint SSE para recibir notificaciones en tiempo real
     * Mantiene una conexión abierta y envía eventos cuando hay nuevas notificaciones
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        Usuario usuario = usuarioRepository.findByNombre(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Integer usuarioId = usuario.getUsuarioID();

        logger.info("📡 Nueva conexión SSE para usuario: {} (ID: {})", usuario.getNombre(), usuarioId);

        // Crear emitter con timeout de 5 minutos
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        // Registrar el emitter para este usuario
        streamService.addEmitter(usuarioId, emitter);

        // Configurar handlers para limpieza
        emitter.onCompletion(() -> {
            logger.info("✅ Conexión SSE completada para usuario ID: {}", usuarioId);
            streamService.removeEmitter(usuarioId, emitter);
        });

        emitter.onTimeout(() -> {
            logger.warn("⏱️ Timeout de conexión SSE para usuario ID: {}", usuarioId);
            streamService.removeEmitter(usuarioId, emitter);
            emitter.complete();
        });

        emitter.onError((ex) -> {
            logger.error("❌ Error en conexión SSE para usuario ID: {}", usuarioId, ex);
            streamService.removeEmitter(usuarioId, emitter);
        });

        // Enviar evento inicial de conexión
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\": \"Conectado al stream de notificaciones\"}"));
        } catch (IOException e) {
            logger.error("Error al enviar evento de conexión", e);
            streamService.removeEmitter(usuarioId, emitter);
        }

        return emitter;
    }

    /**
     * Endpoint para obtener información de conexiones activas (solo para debugging)
     */
    @GetMapping("/stream/status")
    public ResponseEntity<?> getStreamStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        int activeConnections = streamService.getActiveConnectionsCount();
        
        return ResponseEntity.ok().body(new StreamStatus(activeConnections, "OK"));
    }

    // DTO para respuesta de status
    public static class StreamStatus {
        private int activeConnections;
        private String status;

        public StreamStatus(int activeConnections, String status) {
            this.activeConnections = activeConnections;
            this.status = status;
        }

        public int getActiveConnections() {
            return activeConnections;
        }

        public void setActiveConnections(int activeConnections) {
            this.activeConnections = activeConnections;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
