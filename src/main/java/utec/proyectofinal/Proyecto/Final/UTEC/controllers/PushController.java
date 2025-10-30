package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushNotificationRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushSubscriptionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PushNotificationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
@Slf4j
public class PushController {

    private final PushNotificationService pushNotificationService;

    /**
     * Suscribirse a notificaciones push
     * VERSIÓN PÚBLICA - NO requiere autenticación
     *
     * IMPORTANTE: En producción, considera agregar autenticación
     * o vincular la suscripción con el userId enviado desde el frontend
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody PushSubscriptionDTO subscriptionDTO) {
        try {
            log.info("📝 Recibiendo suscripción push");
            log.info("📍 Endpoint: {}...", subscriptionDTO.getEndpoint().substring(0, Math.min(50, subscriptionDTO.getEndpoint().length())));

            // Guardar suscripción sin usuario (para desarrollo)
            // La suscripción se guardará pero sin asociar a ningún usuario específico
            pushNotificationService.savePushSubscriptionPublic(subscriptionDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Suscripción guardada exitosamente");

            log.info("✅ Suscripción guardada correctamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error subscribing to push notifications", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("message", "Error al guardar la suscripción");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Suscribirse con autenticación (alternativa)
     * Este endpoint SÍ requiere autenticación
     */
    @PostMapping("/subscribe-authenticated")
    public ResponseEntity<?> subscribeAuthenticated(
            @RequestBody PushSubscriptionDTO subscriptionDTO,
            @AuthenticationPrincipal Usuario usuario
    ) {
        log.info("📝 Subscribing user {} to push notifications", usuario.getUsuarioID());

        try {
            pushNotificationService.savePushSubscription(usuario, subscriptionDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Suscripción guardada exitosamente");
            response.put("userId", usuario.getUsuarioID());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error subscribing to push notifications", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al guardar la suscripción");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Desuscribirse de notificaciones push
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(
            @RequestBody(required = false) Map<String, String> payload
    ) {
        try {
            String endpoint = payload != null ? payload.get("endpoint") : null;

            if (endpoint != null && !endpoint.isEmpty()) {
                log.info("🗑️ Desuscribiendo endpoint: {}...", endpoint.substring(0, Math.min(50, endpoint.length())));
                pushNotificationService.removePushSubscriptionByEndpoint(endpoint);
            } else {
                log.warn("⚠️ No se proporcionó endpoint para desuscribir");
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Endpoint requerido"
                ));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Suscripción eliminada exitosamente");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error unsubscribing from push notifications", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al eliminar la suscripción");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Endpoint de prueba PÚBLICO - sin autenticación
     * SOLO PARA TESTING - Eliminar en producción
     */
    @PostMapping("/test-broadcast")
    public ResponseEntity<?> testBroadcast() {
        try {
            log.info("🧪 Enviando broadcast de prueba...");

            Map<String, String> data = new HashMap<>();
            data.put("tipo", "TEST");
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));

            PushNotificationRequest notification = PushNotificationRequest.builder()
                    .title("🧪 Test desde Backend")
                    .body("Si ves esto, las notificaciones push funcionan completamente")
                    .icon("/icons/icon-192x192.png")
                    .url("/notificaciones")
                    .tag("test-backend")
                    .data(data)
                    .build();

            pushNotificationService.sendPushNotificationToAll(notification);

            long activeSubscriptions = pushNotificationService.getActiveSubscriptionsCount();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Broadcast enviado exitosamente");
            response.put("subscriptionsCount", activeSubscriptions);

            log.info("✅ Broadcast completado - {} suscripciones activas", activeSubscriptions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error en test broadcast", e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error enviando broadcast");
            error.put("details", e.getMessage());
            error.put("cause", e.getCause() != null ? e.getCause().getMessage() : "N/A");

            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Enviar notificación de prueba (requiere autenticación)
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestNotification(
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        log.info("🧪 Sending test notification to user {}", usuario.getUsuarioID());

        try {
            PushNotificationRequest notification = PushNotificationRequest.builder()
                    .title(request != null && request.containsKey("title")
                            ? request.get("title")
                            : "Notificación de Prueba")
                    .body(request != null && request.containsKey("body")
                            ? request.get("body")
                            : "Esta es una notificación de prueba del sistema INIA")
                    .url(request != null && request.containsKey("url")
                            ? request.get("url")
                            : "/notificaciones")
                    .tag("test-notification")
                    .build();

            pushNotificationService.sendPushNotificationToUser(
                    Long.valueOf(usuario.getUsuarioID()),
                    notification
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notificación de prueba enviada");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending test notification", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al enviar la notificación de prueba");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Obtener estadísticas de suscripciones (público)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        try {
            long activeSubscriptions = pushNotificationService.getActiveSubscriptionsCount();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activeSubscriptions", activeSubscriptions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting stats", e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}