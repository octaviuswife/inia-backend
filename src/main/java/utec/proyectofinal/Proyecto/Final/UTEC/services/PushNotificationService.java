package utec.proyectofinal.Proyecto.Final.UTEC.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.proyectofinal.Proyecto.Final.UTEC.config.PushNotificationConfig;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushNotificationRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushSubscriptionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.PushSubscription;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PushSubscriptionRepository;

import jakarta.annotation.PostConstruct;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {
    
    private final PushSubscriptionRepository subscriptionRepository;
    private final PushNotificationConfig config;
    private final ObjectMapper objectMapper;
    
    private PushService pushService;
    
    @PostConstruct
    public void init() {
        try {
            // Registrar Bouncy Castle provider
            Security.addProvider(new BouncyCastleProvider());
            
            // Verificar si las VAPID keys están configuradas
            if (!config.isConfigured()) {
                log.warn("⚠️ Push Notification Service not configured - VAPID keys missing");
                log.warn("⚠️ Generate keys with: web-push generate-vapid-keys");
                log.warn("⚠️ Add them to application.properties:");
                log.warn("⚠️   push.vapid.public.key=YOUR_PUBLIC_KEY");
                log.warn("⚠️   push.vapid.private.key=YOUR_PRIVATE_KEY");
                return;
            }
            
            // Inicializar Push Service con las VAPID keys
            pushService = new PushService();
            pushService.setPublicKey(config.getPublicKey());
            pushService.setPrivateKey(config.getPrivateKey());
            pushService.setSubject(config.getSubject());
            
            log.info("✅ Push Notification Service initialized successfully");
        } catch (GeneralSecurityException e) {
            log.error("❌ Error initializing Push Notification Service", e);
        }
    }
    
    /**
     * Guardar suscripción de push para un usuario
     */
    @Transactional
    public PushSubscription savePushSubscription(Usuario usuario, PushSubscriptionDTO dto) {
        log.info("💾 Saving push subscription for user: {}", usuario.getUsuarioID());
        
        // Verificar si ya existe una suscripción con ese endpoint
        return subscriptionRepository.findByEndpoint(dto.getEndpoint())
            .map(existing -> {
                // Actualizar suscripción existente
                log.info("🔄 Updating existing subscription");
                existing.setP256dhKey(dto.getKeys().getP256dh());
                existing.setAuthKey(dto.getKeys().getAuth());
                existing.setLastUsedAt(LocalDateTime.now());
                existing.setIsActive(true);
                existing.setUsuario(usuario); // Actualizar usuario por si cambió
                return subscriptionRepository.save(existing);
            })
            .orElseGet(() -> {
                // Crear nueva suscripción
                log.info("✨ Creating new subscription");
                PushSubscription subscription = PushSubscription.builder()
                    .usuario(usuario)
                    .endpoint(dto.getEndpoint())
                    .p256dhKey(dto.getKeys().getP256dh())
                    .authKey(dto.getKeys().getAuth())
                    .isActive(true)
                    .build();
                return subscriptionRepository.save(subscription);
            });
    }
    
    /**
     * Eliminar suscripciones de un usuario
     */
    @Transactional
    public void removePushSubscription(Usuario usuario) {
        log.info("🗑️ Removing push subscriptions for user: {}", usuario.getUsuarioID());
        subscriptionRepository.deleteByUsuarioUsuarioID(usuario.getUsuarioID());
    }
    
    /**
     * Enviar notificación push a un usuario específico
     */
    public void sendPushNotificationToUser(Integer usuarioId, PushNotificationRequest request) {
        if (pushService == null) {
            log.warn("⚠️ Push service not initialized - skipping notification");
            return;
        }
        
        List<PushSubscription> subscriptions = 
            subscriptionRepository.findByUsuarioUsuarioIDAndIsActiveTrue(usuarioId);
        
        if (subscriptions.isEmpty()) {
            log.debug("ℹ️ No active push subscriptions for user: {}", usuarioId);
            return;
        }
        
        log.info("📤 Sending push notification to {} subscription(s) for user: {}", 
            subscriptions.size(), usuarioId);
        
        for (PushSubscription subscription : subscriptions) {
            sendPushNotification(subscription, request);
        }
    }
    
    /**
     * Enviar notificación push a una suscripción específica
     */
    public void sendPushNotification(PushSubscription subscription, PushNotificationRequest request) {
        if (pushService == null) {
            log.warn("⚠️ Push service not initialized - skipping notification");
            return;
        }
        
        try {
            // Construir payload de la notificación
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", request.getTitle());
            payload.put("body", request.getBody());
            payload.put("icon", request.getIcon() != null ? request.getIcon() : "/icons/icon-192x192.png");
            payload.put("badge", request.getBadge() != null ? request.getBadge() : "/icons/icon-72x72.png");
            
            if (request.getImage() != null) {
                payload.put("image", request.getImage());
            }
            
            if (request.getUrl() != null) {
                payload.put("url", request.getUrl());
            }
            
            if (request.getTag() != null) {
                payload.put("tag", request.getTag());
            }
            
            if (request.getNotificationId() != null) {
                payload.put("notificationId", request.getNotificationId());
            }
            
            if (request.getRequireInteraction() != null) {
                payload.put("requireInteraction", request.getRequireInteraction());
            }
            
            if (request.getActions() != null && !request.getActions().isEmpty()) {
                payload.put("actions", request.getActions());
            }
            
            // Convertir payload a JSON
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            // Crear objeto Notification con el constructor correcto
            // Constructor: Notification(endpoint, userPublicKey, userAuth, payload)
            // Las keys deben ser Strings en Base64, no PublicKey
            Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dhKey(),
                subscription.getAuthKey(),
                payloadJson.getBytes()
            );
            
            // Enviar notificación
            HttpResponse response = pushService.send(notification);
            
            // Actualizar lastUsedAt
            subscription.setLastUsedAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
            
            log.info("✅ Push notification sent successfully to endpoint: {}...", 
                subscription.getEndpoint().substring(0, Math.min(50, subscription.getEndpoint().length()))
            );
            
        } catch (Exception e) {
            log.error("❌ Error sending push notification", e);
            
            // Si el error es 410 (Gone), marcar la suscripción como inactiva
            if (e.getMessage() != null && (e.getMessage().contains("410") || e.getMessage().contains("Gone"))) {
                subscription.setIsActive(false);
                subscriptionRepository.save(subscription);
                log.warn("⚠️ Subscription marked as inactive due to 410 error");
            }
        }
    }
    
    /**
     * Enviar notificación a todos los usuarios activos (usar con cuidado)
     */
    public void broadcastPushNotification(PushNotificationRequest request) {
        if (pushService == null) {
            log.warn("⚠️ Push service not initialized - skipping broadcast");
            return;
        }
        
        List<PushSubscription> subscriptions = subscriptionRepository.findAllByIsActiveTrue();
        
        log.info("📢 Broadcasting push notification to {} subscriptions", subscriptions.size());
        
        for (PushSubscription subscription : subscriptions) {
            sendPushNotification(subscription, request);
        }
    }
}
