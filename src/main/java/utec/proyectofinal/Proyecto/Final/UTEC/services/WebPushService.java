package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.security.Security;
import java.util.concurrent.ExecutionException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.PushSubscription;
import utec.proyectofinal.Proyecto.Final.UTEC.config.VapidConfig;

@Service
public class WebPushService {

    private static final Logger logger = LoggerFactory.getLogger(WebPushService.class);
    
    private final PushService pushService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public WebPushService(VapidConfig vapidConfig, ObjectMapper objectMapper) throws Exception {
        // Registrar el proveedor de seguridad BouncyCastle
        Security.addProvider(new BouncyCastleProvider());
        
        this.objectMapper = objectMapper;
        this.pushService = new PushService();
        
        // Configurar las claves VAPID
        this.pushService.setPublicKey(vapidConfig.getPublicKey());
        this.pushService.setPrivateKey(vapidConfig.getPrivateKey());
        this.pushService.setSubject(vapidConfig.getSubject());
        
        logger.info("WebPushService inicializado correctamente");
    }
    
    /**
     * Envía una notificación push a una suscripción específica
     */
    public boolean sendNotification(PushSubscription subscription, PushNotificationPayload payload) {
        try {
            // Crear objeto de suscripción compatible con la librería
            Subscription.Keys keys = new Subscription.Keys(
                subscription.getP256dh(),
                subscription.getAuth()
            );
            
            Subscription sub = new Subscription(
                subscription.getEndpoint(),
                keys
            );
            
            // Convertir el payload a JSON
            String payloadJson = objectMapper.writeValueAsString(payload);
            
            // Crear y enviar la notificación
            Notification notification = new Notification(sub, payloadJson);
            pushService.send(notification);
            
            logger.info("Notificación push enviada exitosamente al endpoint: {}", 
                subscription.getEndpoint().substring(0, 50) + "...");
            
            return true;
            
        } catch (JsonProcessingException e) {
            logger.error("Error al serializar el payload: {}", e.getMessage());
            return false;
        } catch (JoseException e) {
            logger.error("Error de encriptación JOSE: {}", e.getMessage());
            return false;
        } catch (ExecutionException e) {
            logger.error("Error al ejecutar el envío: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.error("Envío interrumpido: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            logger.error("Error inesperado al enviar notificación push: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Clase interna para representar el payload de la notificación
     */
    public static class PushNotificationPayload {
        private String title;
        private String body;
        private String icon;
        private String badge;
        private String image;
        private String url;
        private Long analisisId;
        
        public PushNotificationPayload() {}
        
        public PushNotificationPayload(String title, String body) {
            this.title = title;
            this.body = body;
        }
        
        // Getters y Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getBadge() { return badge; }
        public void setBadge(String badge) { this.badge = badge; }
        
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public Long getAnalisisId() { return analisisId; }
        public void setAnalisisId(Long analisisId) { this.analisisId = analisisId; }
    }
}
