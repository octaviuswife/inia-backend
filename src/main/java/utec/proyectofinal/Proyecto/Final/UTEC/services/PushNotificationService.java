package utec.proyectofinal.Proyecto.Final.UTEC.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.PushSubscription;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PushSubscriptionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushNotificationRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushSubscriptionDTO;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {

    @Value("${push.vapid.public.key}")
    private String vapidPublicKey;

    @Value("${push.vapid.private.key}")
    private String vapidPrivateKey;

    @Value("${push.vapid.subject:mailto:admin@inia.org.uy}")
    private String vapidSubject;

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private PushService pushService;
    private boolean initialized = false;

    // Constructor con inicialización lazy
    public PushNotificationService(PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    /**
     * Inicializar PushService (lazy initialization)
     */
    private void initPushService() throws GeneralSecurityException {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    Security.addProvider(new BouncyCastleProvider());

                    pushService = new PushService();
                    pushService.setPublicKey(vapidPublicKey);
                    pushService.setPrivateKey(vapidPrivateKey);
                    pushService.setSubject(vapidSubject);

                    initialized = true;
                    log.info("✅ PushService inicializado correctamente");
                    log.info("📧 VAPID Subject: {}", vapidSubject);
                }
            }
        }
    }

    /**
     * Guardar una nueva suscripción push
     */
    @Transactional
    public void savePushSubscription(Usuario usuario, PushSubscriptionDTO subscriptionDTO) {
        log.info("💾 Guardando suscripción push para usuario: {}", usuario.getUsuarioID());

        // Verificar si ya existe una suscripción para este endpoint
        pushSubscriptionRepository.findByEndpoint(subscriptionDTO.getEndpoint())
                .ifPresent(existingSub -> {
                    log.info("🔄 Actualizando suscripción existente");
                    pushSubscriptionRepository.delete(existingSub);
                });

        PushSubscription subscription = new PushSubscription();
        subscription.setUsuario(usuario);
        subscription.setEndpoint(subscriptionDTO.getEndpoint());
        subscription.setP256dh(subscriptionDTO.getKeys().getP256dh());
        subscription.setAuth(subscriptionDTO.getKeys().getAuth());
        subscription.setIsActive(true);

        pushSubscriptionRepository.save(subscription);
        log.info("✅ Suscripción guardada exitosamente");
    }

    /**
     * Eliminar suscripción push de un usuario
     */
    @Transactional
    public void removePushSubscription(Usuario usuario) {
        log.info("🗑️ Eliminando suscripciones push para usuario: {}", usuario.getUsuarioID());

        Integer usuarioId = usuario.getUsuarioID().intValue();
        pushSubscriptionRepository.deleteByUsuarioUsuarioID(usuarioId);
        log.info("✅ Suscripciones eliminadas");
    }

    /**
     * Enviar notificación push a un usuario específico
     */
    public void sendPushNotificationToUser(Long usuarioId, PushNotificationRequest request) {
        try {
            // Inicializar PushService si no está inicializado
            initPushService();

            log.info("📤 Enviando notificación push a usuario: {}", usuarioId);

            Integer usuarioIdInt = usuarioId.intValue();
            List<PushSubscription> subscriptions = pushSubscriptionRepository
                    .findByUsuarioUsuarioIDAndIsActiveTrue(usuarioIdInt);

            if (subscriptions.isEmpty()) {
                log.warn("⚠️ Usuario {} no tiene suscripciones activas", usuarioId);
                return;
            }

            log.info("📱 Encontradas {} suscripciones activas", subscriptions.size());

            int successCount = 0;
            int failCount = 0;

            for (PushSubscription subscription : subscriptions) {
                try {
                    sendNotification(subscription, request);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("❌ Error enviando notificación a suscripción {}: {}",
                            subscription.getId(), e.getMessage());

                    if (e.getMessage() != null && e.getMessage().contains("410")) {
                        log.warn("🗑️ Desactivando suscripción inválida");
                        subscription.setIsActive(false);
                        pushSubscriptionRepository.save(subscription);
                    }
                }
            }

            log.info("📊 Resultado: {} exitosas, {} fallidas", successCount, failCount);
        } catch (Exception e) {
            log.error("❌ Error general enviando notificaciones", e);
            throw new RuntimeException("Error enviando notificación push", e);
        }
    }
    // Agregar estos métodos a tu PushNotificationService.java

    /**
     * Guardar suscripción sin usuario (versión pública)
     * Útil para desarrollo o cuando no se requiere autenticación
     */
    @Transactional
    public void savePushSubscriptionPublic(PushSubscriptionDTO subscriptionDTO) {
        log.info("💾 Guardando suscripción push pública");
        log.info("📍 Endpoint: {}...", subscriptionDTO.getEndpoint().substring(0, Math.min(50, subscriptionDTO.getEndpoint().length())));

        // Verificar si ya existe una suscripción para este endpoint
        pushSubscriptionRepository.findByEndpoint(subscriptionDTO.getEndpoint())
                .ifPresent(existingSub -> {
                    log.info("🔄 Actualizando suscripción existente ID: {}", existingSub.getId());
                    pushSubscriptionRepository.delete(existingSub);
                });

        PushSubscription subscription = new PushSubscription();
        subscription.setUsuario(null); // Sin usuario asociado
        subscription.setEndpoint(subscriptionDTO.getEndpoint());
        subscription.setP256dh(subscriptionDTO.getKeys().getP256dh());
        subscription.setAuth(subscriptionDTO.getKeys().getAuth());
        subscription.setIsActive(true);

        PushSubscription saved = pushSubscriptionRepository.save(subscription);
        log.info("✅ Suscripción guardada exitosamente con ID: {}", saved.getId());
    }

    /**
     * Eliminar suscripción por endpoint
     */
    @Transactional
    public void removePushSubscriptionByEndpoint(String endpoint) {
        log.info("🗑️ Eliminando suscripción por endpoint");

        pushSubscriptionRepository.findByEndpoint(endpoint)
                .ifPresentOrElse(
                        subscription -> {
                            pushSubscriptionRepository.delete(subscription);
                            log.info("✅ Suscripción eliminada: ID {}", subscription.getId());
                        },
                        () -> log.warn("⚠️ No se encontró suscripción con ese endpoint")
                );
    }

    /**
     * Enviar notificación a una suscripción específica
     */
    private void sendNotification(PushSubscription subscription, PushNotificationRequest request)
            throws Exception {

        // Crear payload JSON estructurado
        Map<String, Object> payload = new HashMap<>();

        // Información de la notificación
        Map<String, String> notification = new HashMap<>();
        notification.put("title", request.getTitle() != null ? request.getTitle() : "INIA");
        notification.put("body", request.getBody() != null ? request.getBody() : "");
        notification.put("icon", request.getIcon() != null ? request.getIcon() : "/icons/icon-192x192.png");
        notification.put("badge", "/icons/icon-72x72.png");

        payload.put("notification", notification);

        // Datos adicionales
        Map<String, String> data = new HashMap<>();
        data.put("url", request.getUrl() != null ? request.getUrl() : "/");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        data.put("tag", request.getTag() != null ? request.getTag() : "inia-notification");

        if (request.getData() != null) {
            data.putAll(request.getData());
        }

        payload.put("data", data);

        // Convertir a JSON String
        String jsonPayload = objectMapper.writeValueAsString(payload);

        log.info("📨 Payload JSON: {}", jsonPayload);
        log.info("📍 Endpoint: {}...", subscription.getEndpoint().substring(0, Math.min(50, subscription.getEndpoint().length())));

        // Crear notificación con el payload JSON
        Notification webPushNotification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                jsonPayload.getBytes("UTF-8")
        );

        // Enviar
        pushService.send(webPushNotification);
        log.info("✅ Notificación enviada correctamente");
    }

    /**
     * Enviar notificación a todos los usuarios suscritos
     */
    public void sendPushNotificationToAll(PushNotificationRequest request) {
        try {
            initPushService();

            log.info("📢 Enviando broadcast a todos los usuarios suscritos");

            List<PushSubscription> allSubscriptions = pushSubscriptionRepository.findAllByIsActiveTrue();

            if (allSubscriptions.isEmpty()) {
                log.warn("⚠️ No hay suscripciones activas en el sistema");
                return;
            }

            log.info("📱 Enviando a {} suscripciones", allSubscriptions.size());

            int successCount = 0;
            int failCount = 0;

            for (PushSubscription subscription : allSubscriptions) {
                try {
                    sendNotification(subscription, request);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error("❌ Error enviando a usuario {}: {}",
                            subscription.getUsuario().getUsuarioID(), e.getMessage());
                }
            }

            log.info("📊 Broadcast completado - Éxitos: {}, Fallos: {}", successCount, failCount);
        } catch (Exception e) {
            log.error("❌ Error en broadcast", e);
            throw new RuntimeException("Error en broadcast de notificaciones", e);
        }
    }

    /**
     * Obtener número de suscripciones activas
     */
    public long getActiveSubscriptionsCount() {
        return pushSubscriptionRepository.findAllByIsActiveTrue().size();
    }
}