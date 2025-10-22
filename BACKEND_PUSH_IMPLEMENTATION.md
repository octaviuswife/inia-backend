# Backend - Implementación de Push Notifications

## 📦 Dependencias (Maven)

Agrega a tu `pom.xml`:

```xml
<!-- Web Push para Java -->
<dependency>
    <groupId>nl.martijndwars</groupId>
    <artifactId>web-push</artifactId>
    <version>5.1.1</version>
</dependency>

<!-- Bouncy Castle (requerido por web-push) -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
    <version>1.70</version>
</dependency>
```

## 🔑 Generar VAPID Keys

```bash
# Opción 1: Usar web-push CLI (Node.js)
npm install -g web-push
web-push generate-vapid-keys

# Opción 2: Usar código Java
// Ver clase VapidKeyGenerator.java más abajo
```

## 📝 Entidades

### PushSubscription.java

```java
package utec.inia.backend.entities;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "push_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(nullable = false, length = 500)
    private String endpoint;
    
    @Column(name = "p256dh_key", nullable = false)
    private String p256dhKey;
    
    @Column(name = "auth_key", nullable = false)
    private String authKey;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }
}
```

## 📋 DTOs

### PushSubscriptionDTO.java

```java
package utec.inia.backend.dto;

import lombok.Data;

@Data
public class PushSubscriptionDTO {
    private String endpoint;
    private Long expirationTime;
    private Keys keys;
    
    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
```

### PushNotificationRequest.java

```java
package utec.inia.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PushNotificationRequest {
    private String title;
    private String body;
    private String icon;
    private String badge;
    private String image;
    private String url;
    private String tag;
    private Long notificationId;
    private Boolean requireInteraction;
    private List<Action> actions;
    
    @Data
    @Builder
    public static class Action {
        private String action;
        private String title;
        private String icon;
    }
}
```

## 🗄️ Repository

### PushSubscriptionRepository.java

```java
package utec.inia.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.inia.backend.entities.PushSubscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    
    List<PushSubscription> findByUsuarioIdAndIsActiveTrue(Long usuarioId);
    
    Optional<PushSubscription> findByEndpoint(String endpoint);
    
    void deleteByUsuarioId(Long usuarioId);
    
    List<PushSubscription> findAllByIsActiveTrue();
}
```

## ⚙️ Configuración

### application.properties

```properties
# Push Notifications Configuration
push.vapid.public.key=BNg...Tu-Clave-Publica-Aqui...
push.vapid.private.key=Abc...Tu-Clave-Privada-Aqui...
push.vapid.subject=mailto:admin@inia.org.uy

# TTL para notificaciones push (en segundos)
push.ttl=86400
```

### PushNotificationConfig.java

```java
package utec.inia.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class PushNotificationConfig {
    
    @Value("${push.vapid.public.key}")
    private String publicKey;
    
    @Value("${push.vapid.private.key}")
    private String privateKey;
    
    @Value("${push.vapid.subject}")
    private String subject;
    
    @Value("${push.ttl:86400}")
    private int ttl;
}
```

## 🔧 Servicios

### PushNotificationService.java

```java
package utec.inia.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.inia.backend.config.PushNotificationConfig;
import utec.inia.backend.dto.PushNotificationRequest;
import utec.inia.backend.dto.PushSubscriptionDTO;
import utec.inia.backend.entities.PushSubscription;
import utec.inia.backend.entities.Usuario;
import utec.inia.backend.repositories.PushSubscriptionRepository;

import javax.annotation.PostConstruct;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {
    
    private final PushSubscriptionRepository subscriptionRepository;
    private final PushNotificationConfig config;
    private final ObjectMapper objectMapper;
    
    private PushService pushService;
    
    @PostConstruct
    public void init() throws GeneralSecurityException {
        // Registrar Bouncy Castle provider
        Security.addProvider(new BouncyCastleProvider());
        
        // Inicializar Push Service con las VAPID keys
        pushService = new PushService();
        pushService.setPublicKey(config.getPublicKey());
        pushService.setPrivateKey(config.getPrivateKey());
        pushService.setSubject(config.getSubject());
        
        log.info("✅ Push Notification Service initialized");
    }
    
    /**
     * Guardar suscripción de push para un usuario
     */
    @Transactional
    public PushSubscription savePushSubscription(Usuario usuario, PushSubscriptionDTO dto) {
        // Verificar si ya existe una suscripción con ese endpoint
        return subscriptionRepository.findByEndpoint(dto.getEndpoint())
            .map(existing -> {
                // Actualizar suscripción existente
                existing.setP256dhKey(dto.getKeys().getP256dh());
                existing.setAuthKey(dto.getKeys().getAuth());
                existing.setLastUsedAt(LocalDateTime.now());
                existing.setIsActive(true);
                return subscriptionRepository.save(existing);
            })
            .orElseGet(() -> {
                // Crear nueva suscripción
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
        subscriptionRepository.deleteByUsuarioId(usuario.getId());
        log.info("🗑️ Removed push subscriptions for user: {}", usuario.getId());
    }
    
    /**
     * Enviar notificación push a un usuario específico
     */
    public void sendPushNotificationToUser(Long usuarioId, PushNotificationRequest request) {
        List<PushSubscription> subscriptions = 
            subscriptionRepository.findByUsuarioIdAndIsActiveTrue(usuarioId);
        
        if (subscriptions.isEmpty()) {
            log.warn("⚠️ No active push subscriptions for user: {}", usuarioId);
            return;
        }
        
        for (PushSubscription subscription : subscriptions) {
            sendPushNotification(subscription, request);
        }
    }
    
    /**
     * Enviar notificación push a una suscripción específica
     */
    public void sendPushNotification(PushSubscription subscription, PushNotificationRequest request) {
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
            
            // Crear objeto Notification
            Notification notification = new Notification(
                subscription.getEndpoint(),
                Utils.loadPublicKey(subscription.getP256dhKey()),
                Utils.loadPrivateKey(subscription.getAuthKey()),
                payloadJson.getBytes()
            );
            
            // Enviar notificación
            pushService.send(notification, config.getTtl());
            
            // Actualizar lastUsedAt
            subscription.setLastUsedAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
            
            log.info("✅ Push notification sent successfully to endpoint: {}...{}", 
                subscription.getEndpoint().substring(0, 50),
                subscription.getEndpoint().substring(subscription.getEndpoint().length() - 10)
            );
            
        } catch (GeneralSecurityException | ExecutionException | InterruptedException e) {
            log.error("❌ Error sending push notification", e);
            
            // Si el error es 410 (Gone), marcar la suscripción como inactiva
            if (e.getMessage() != null && e.getMessage().contains("410")) {
                subscription.setIsActive(false);
                subscriptionRepository.save(subscription);
                log.warn("⚠️ Subscription marked as inactive due to 410 error");
            }
        } catch (Exception e) {
            log.error("❌ Unexpected error sending push notification", e);
        }
    }
    
    /**
     * Enviar notificación a todos los usuarios activos (usar con cuidado)
     */
    public void broadcastPushNotification(PushNotificationRequest request) {
        List<PushSubscription> subscriptions = subscriptionRepository.findAllByIsActiveTrue();
        
        log.info("📢 Broadcasting push notification to {} subscriptions", subscriptions.size());
        
        for (PushSubscription subscription : subscriptions) {
            sendPushNotification(subscription, request);
        }
    }
}
```

## 🎮 Controller

### PushController.java

```java
package utec.inia.backend.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import utec.inia.backend.dto.PushNotificationRequest;
import utec.inia.backend.dto.PushSubscriptionDTO;
import utec.inia.backend.entities.Usuario;
import utec.inia.backend.services.PushNotificationService;

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
        log.info("📝 Subscribing user {} to push notifications", usuario.getId());
        
        pushNotificationService.savePushSubscription(usuario, subscriptionDTO);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Suscripción guardada exitosamente");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Desuscribirse de notificaciones push
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@AuthenticationPrincipal Usuario usuario) {
        log.info("🗑️ Unsubscribing user {} from push notifications", usuario.getId());
        
        pushNotificationService.removePushSubscription(usuario);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Suscripción eliminada exitosamente");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enviar notificación de prueba (solo para desarrollo/testing)
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestNotification(
        @RequestBody(required = false) Map<String, String> request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        log.info("🧪 Sending test notification to user {}", usuario.getId());
        
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
        
        pushNotificationService.sendPushNotificationToUser(usuario.getId(), notification);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notificación de prueba enviada");
        
        return ResponseEntity.ok(response);
    }
}
```

## 🔗 Integración con Notificaciones Existentes

### NotificacionService.java (actualizar)

```java
@Service
@RequiredArgsConstructor
public class NotificacionService {
    
    private final NotificacionRepository notificacionRepository;
    private final PushNotificationService pushNotificationService;
    
    /**
     * Crear notificación y enviar push
     */
    @Transactional
    public Notificacion crearNotificacion(NotificacionDTO dto) {
        // Guardar notificación en BD
        Notificacion notificacion = notificacionRepository.save(
            convertirAEntidad(dto)
        );
        
        // Enviar push notification
        enviarPushParaNotificacion(notificacion);
        
        return notificacion;
    }
    
    /**
     * Enviar push notification para una notificación
     */
    private void enviarPushParaNotificacion(Notificacion notificacion) {
        PushNotificationRequest pushRequest = PushNotificationRequest.builder()
            .title(notificacion.getTitulo())
            .body(notificacion.getMensaje())
            .url("/notificaciones/" + notificacion.getId())
            .tag("notif-" + notificacion.getId())
            .notificationId(notificacion.getId())
            .requireInteraction(false)
            .build();
        
        pushNotificationService.sendPushNotificationToUser(
            notificacion.getUsuario().getId(),
            pushRequest
        );
    }
}
```

## 🧪 Testing

### Test con Postman

1. **Suscribirse**:
```http
POST /api/push/subscribe
Authorization: Bearer {token}
Content-Type: application/json

{
  "endpoint": "https://fcm.googleapis.com/fcm/send/...",
  "keys": {
    "p256dh": "...",
    "auth": "..."
  }
}
```

2. **Enviar notificación de prueba**:
```http
POST /api/push/test
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Prueba",
  "body": "Mensaje de prueba",
  "url": "/dashboard"
}
```

## 📊 Base de Datos

### Migration SQL

```sql
CREATE TABLE push_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    p256dh_key VARCHAR(255) NOT NULL,
    auth_key VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    UNIQUE KEY unique_endpoint (endpoint),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario_active (usuario_id, is_active)
);
```

## 🚀 Próximos Pasos

1. ✅ Agregar dependencias al pom.xml
2. ✅ Generar VAPID keys y agregarlas al application.properties
3. ✅ Crear migración de base de datos
4. ✅ Implementar entidades, repos, DTOs
5. ✅ Implementar servicios y controllers
6. ✅ Integrar con sistema de notificaciones existente
7. ✅ Probar con frontend
