package utec.proyectofinal.Proyecto.Final.UTEC.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio para gestionar conexiones SSE y enviar notificaciones en tiempo real
 */
@Service
public class NotificationStreamService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationStreamService.class);

    // Map: usuarioId -> List de emitters (un usuario puede tener múltiples tabs/devices)
    private final Map<Integer, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Agrega un nuevo emitter para un usuario
     */
    public void addEmitter(Integer usuarioId, SseEmitter emitter) {
        userEmitters.computeIfAbsent(usuarioId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        logger.info("➕ Emitter agregado para usuario {}. Total emitters: {}", 
                usuarioId, userEmitters.get(usuarioId).size());
    }

    /**
     * Remueve un emitter de un usuario
     */
    public void removeEmitter(Integer usuarioId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(usuarioId);
        if (emitters != null) {
            emitters.remove(emitter);
            logger.info("➖ Emitter removido para usuario {}. Emitters restantes: {}", 
                    usuarioId, emitters.size());
            
            // Si no quedan emitters, remover el entry del map
            if (emitters.isEmpty()) {
                userEmitters.remove(usuarioId);
                logger.info("🗑️ No quedan emitters para usuario {}, removiendo del map", usuarioId);
            }
        }
    }

    /**
     * Envía una notificación a un usuario específico (todas sus conexiones)
     */
    public void sendNotificationToUser(Integer usuarioId, Object notificationData) {
        List<SseEmitter> emitters = userEmitters.get(usuarioId);
        
        if (emitters == null || emitters.isEmpty()) {
            logger.debug("No hay conexiones activas para usuario {}", usuarioId);
            return;
        }

        logger.info("📤 Enviando notificación a {} conexiones del usuario {}", 
                emitters.size(), usuarioId);

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                String json = objectMapper.writeValueAsString(notificationData);
                
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(json));
                
                logger.debug("✅ Notificación enviada exitosamente a un emitter del usuario {}", usuarioId);
                
            } catch (IOException e) {
                logger.error("❌ Error al enviar notificación al usuario {}", usuarioId, e);
                deadEmitters.add(emitter);
            } catch (Exception e) {
                logger.error("❌ Error inesperado al enviar notificación", e);
                deadEmitters.add(emitter);
            }
        }

        // Limpiar emitters muertos
        deadEmitters.forEach(emitter -> removeEmitter(usuarioId, emitter));
    }

    /**
     * Envía un evento de heartbeat para mantener la conexión viva
     */
    public void sendHeartbeat(Integer usuarioId) {
        List<SseEmitter> emitters = userEmitters.get(usuarioId);
        
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("{\"timestamp\": " + System.currentTimeMillis() + "}"));
            } catch (IOException e) {
                logger.debug("Emitter muerto detectado en heartbeat para usuario {}", usuarioId);
                deadEmitters.add(emitter);
            }
        }

        deadEmitters.forEach(emitter -> removeEmitter(usuarioId, emitter));
    }

    /**
     * Envía una notificación a todos los usuarios conectados (broadcast)
     */
    public void broadcastNotification(Object notificationData) {
        logger.info("📢 Broadcasting notificación a {} usuarios conectados", userEmitters.size());
        
        userEmitters.keySet().forEach(usuarioId -> 
            sendNotificationToUser(usuarioId, notificationData));
    }

    /**
     * Obtiene el número de conexiones activas
     */
    public int getActiveConnectionsCount() {
        return userEmitters.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Obtiene el número de usuarios conectados
     */
    public int getActiveUsersCount() {
        return userEmitters.size();
    }

    /**
     * Limpia todas las conexiones (útil para shutdown)
     */
    public void closeAllConnections() {
        logger.info("🔌 Cerrando todas las conexiones SSE...");
        
        userEmitters.forEach((usuarioId, emitters) -> {
            emitters.forEach(emitter -> {
                try {
                    emitter.complete();
                } catch (Exception e) {
                    logger.error("Error al cerrar emitter para usuario {}", usuarioId, e);
                }
            });
        });
        
        userEmitters.clear();
        logger.info("✅ Todas las conexiones SSE cerradas");
    }
}
