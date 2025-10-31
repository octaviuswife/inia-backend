package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import utec.proyectofinal.Proyecto.Final.UTEC.config.VapidConfig;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushNotificationRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushSubscriptionRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PushSubscriptionResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ApiResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PushNotificationService;

@RestController
@RequestMapping("/api/push")
public class PushNotificationController {

    @Autowired
    private PushNotificationService pushNotificationService;
    
    @Autowired
    private VapidConfig vapidConfig;
    
    /**
     * Obtiene la clave pública VAPID para configurar el cliente
     * GET /api/push/vapid-public-key
     */
    @GetMapping("/vapid-public-key")
    public ResponseEntity<ApiResponse<String>> obtenerClavePublica() {
        return ResponseEntity.ok(
            new ApiResponse<>(true, "Clave pública VAPID obtenida", vapidConfig.getPublicKey())
        );
    }
    
    /**
     * Registra una nueva suscripción push
     * POST /api/push/subscribe
     */
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<PushSubscriptionResponse>> suscribirse(
            @Valid @RequestBody PushSubscriptionRequest request) {
        try {
            PushSubscriptionResponse response = pushNotificationService.suscribirse(request);
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Suscripción registrada exitosamente", response)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al registrar suscripción: " + e.getMessage(), null));
        }
    }
    
    /**
     * Cancela una suscripción push
     * DELETE /api/push/unsubscribe
     */
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> desuscribirse(@RequestParam String endpoint) {
        try {
            pushNotificationService.desuscribirse(endpoint);
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Suscripción cancelada exitosamente", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al cancelar suscripción: " + e.getMessage(), null));
        }
    }
    
    /**
     * Obtiene todas las suscripciones del usuario actual
     * GET /api/push/subscriptions
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<List<PushSubscriptionResponse>>> obtenerMisSuscripciones() {
        try {
            List<PushSubscriptionResponse> subscriptions = pushNotificationService.obtenerMisSuscripciones();
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Suscripciones obtenidas exitosamente", subscriptions)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al obtener suscripciones: " + e.getMessage(), null));
        }
    }
    
    /**
     * Envía una notificación push de prueba (solo para administradores)
     * POST /api/push/send-test
     */
    @PostMapping("/send-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enviarNotificacionPrueba(
            @Valid @RequestBody PushNotificationRequest request) {
        try {
            pushNotificationService.enviarNotificacionATodos(request);
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Notificación de prueba enviada", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al enviar notificación: " + e.getMessage(), null));
        }
    }
    
    /**
     * Envía una notificación push a todos los administradores
     * POST /api/push/send-to-admins
     */
    @PostMapping("/send-to-admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enviarNotificacionAAdministradores(
            @Valid @RequestBody PushNotificationRequest request) {
        try {
            pushNotificationService.enviarNotificacionAAdministradores(request);
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Notificación enviada a administradores", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error al enviar notificación: " + e.getMessage(), null));
        }
    }
}
