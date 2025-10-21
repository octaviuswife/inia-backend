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
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
        @RequestBody PushSubscriptionDTO subscriptionDTO,
        @AuthenticationPrincipal Usuario usuario
    ) {
        log.info("📝 Subscribing user {} to push notifications", usuario.getUsuarioID());
        
        try {
            pushNotificationService.savePushSubscription(usuario, subscriptionDTO);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Suscripción guardada exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error subscribing to push notifications", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al guardar la suscripción");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Desuscribirse de notificaciones push
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@AuthenticationPrincipal Usuario usuario) {
        log.info("🗑️ Unsubscribing user {} from push notifications", usuario.getUsuarioID());
        
        try {
            pushNotificationService.removePushSubscription(usuario);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Suscripción eliminada exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error unsubscribing from push notifications", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar la suscripción");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Enviar notificación de prueba (para desarrollo/testing)
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
            
            pushNotificationService.sendPushNotificationToUser(usuario.getUsuarioID(), notification);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notificación de prueba enviada");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending test notification", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al enviar la notificación de prueba");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
