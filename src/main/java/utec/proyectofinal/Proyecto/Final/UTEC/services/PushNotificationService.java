package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.PushSubscription;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PushSubscriptionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushNotificationRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PushSubscriptionRequest;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PushSubscriptionResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.services.WebPushService.PushNotificationPayload;

@Service
@Transactional
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    
    @Autowired
    private PushSubscriptionRepository subscriptionRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private WebPushService webPushService;
    
    /**
     * Registra una nueva suscripción push para el usuario autenticado
     */
    public PushSubscriptionResponse suscribirse(PushSubscriptionRequest request) {
        Usuario usuario = obtenerUsuarioActual();
        
        // Verificar si ya existe una suscripción para este endpoint
        PushSubscription existingSub = subscriptionRepository
            .findByEndpointAndActivoTrue(request.getEndpoint())
            .orElse(null);
        
        if (existingSub != null) {
            // Actualizar la suscripción existente
            existingSub.setP256dh(request.getP256dh());
            existingSub.setAuth(request.getAuth());
            existingSub.setUserAgent(request.getUserAgent());
            existingSub.setUsuario(usuario);
            existingSub.setActivo(true);
            
            existingSub = subscriptionRepository.save(existingSub);
            logger.info("Suscripción actualizada para usuario: {}", usuario.getNombre());
            return convertToResponse(existingSub);
        }
        
        // Crear nueva suscripción
        PushSubscription subscription = new PushSubscription();
        subscription.setUsuario(usuario);
        subscription.setEndpoint(request.getEndpoint());
        subscription.setP256dh(request.getP256dh());
        subscription.setAuth(request.getAuth());
        subscription.setUserAgent(request.getUserAgent());
        subscription.setActivo(true);
        
        subscription = subscriptionRepository.save(subscription);
        logger.info("Nueva suscripción push creada para usuario: {}", usuario.getNombre());
        
        return convertToResponse(subscription);
    }
    
    /**
     * Cancela una suscripción push
     */
    public void desuscribirse(String endpoint) {
        Usuario usuario = obtenerUsuarioActual();
        
        PushSubscription subscription = subscriptionRepository
            .findByEndpointAndActivoTrue(endpoint)
            .orElseThrow(() -> new RuntimeException("Suscripción no encontrada"));
        
        // Verificar que la suscripción pertenece al usuario actual o que es admin
        if (!subscription.getUsuario().getUsuarioID().equals(usuario.getUsuarioID()) 
            && !usuario.esAdmin()) {
            throw new RuntimeException("No tiene permisos para eliminar esta suscripción");
        }
        
        subscription.setActivo(false);
        subscriptionRepository.save(subscription);
        
        logger.info("Suscripción cancelada para usuario: {}", usuario.getNombre());
    }
    
    /**
     * Obtiene todas las suscripciones del usuario actual
     */
    public List<PushSubscriptionResponse> obtenerMisSuscripciones() {
        Usuario usuario = obtenerUsuarioActual();
        
        List<PushSubscription> subscriptions = subscriptionRepository
            .findByUsuarioUsuarioIDAndActivoTrue(usuario.getUsuarioID());
        
        return subscriptions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Envía una notificación push a un usuario específico
     */
    public void enviarNotificacionAUsuario(Integer usuarioId, PushNotificationRequest request) {
        List<PushSubscription> subscriptions = subscriptionRepository
            .findByUsuarioUsuarioIDAndActivoTrue(usuarioId);
        
        if (subscriptions.isEmpty()) {
            logger.warn("No hay suscripciones activas para el usuario ID: {}", usuarioId);
            return;
        }
        
        PushNotificationPayload payload = new PushNotificationPayload();
        payload.setTitle(request.getTitle());
        payload.setBody(request.getBody());
        payload.setIcon(request.getIcon() != null ? request.getIcon() : "/icons/icon-192x192.png");
        payload.setBadge(request.getBadge() != null ? request.getBadge() : "/icons/badge-72x72.png");
        payload.setImage(request.getImage());
        payload.setUrl(request.getUrl());
        payload.setAnalisisId(request.getAnalisisId());
        
        for (PushSubscription subscription : subscriptions) {
            boolean success = webPushService.sendNotification(subscription, payload);
            
            if (!success) {
                logger.error("Error al enviar notificación push a suscripción ID: {}", subscription.getId());
                // Opcional: marcar la suscripción como inactiva si falla repetidamente
            }
        }
    }
    
    /**
     * Envía una notificación push a todos los usuarios (broadcast)
     */
    public void enviarNotificacionATodos(PushNotificationRequest request) {
        List<PushSubscription> subscriptions = subscriptionRepository.findByActivoTrue();
        
        if (subscriptions.isEmpty()) {
            logger.warn("No hay suscripciones activas para enviar notificación broadcast");
            return;
        }
        
        PushNotificationPayload payload = new PushNotificationPayload();
        payload.setTitle(request.getTitle());
        payload.setBody(request.getBody());
        payload.setIcon(request.getIcon() != null ? request.getIcon() : "/icons/icon-192x192.png");
        payload.setBadge(request.getBadge() != null ? request.getBadge() : "/icons/badge-72x72.png");
        payload.setImage(request.getImage());
        payload.setUrl(request.getUrl());
        payload.setAnalisisId(request.getAnalisisId());
        
        logger.info("Enviando notificación broadcast a {} suscripciones", subscriptions.size());
        
        for (PushSubscription subscription : subscriptions) {
            webPushService.sendNotification(subscription, payload);
        }
    }
    
    /**
     * Envía una notificación push a todos los administradores
     */
    public void enviarNotificacionAAdministradores(PushNotificationRequest request) {
        List<Usuario> admins = usuarioRepository
            .findByEstado(utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario.ACTIVO)
            .stream()
            .filter(u -> u.getRol() == utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
            .collect(Collectors.toList());
        
        for (Usuario admin : admins) {
            enviarNotificacionAUsuario(admin.getUsuarioID(), request);
        }
    }
    
    /**
     * Limpia suscripciones inactivas antiguas (opcional, para mantenimiento)
     */
    public void limpiarSuscripcionesInactivas(int diasAntiguedad) {
        // Implementación opcional para eliminar suscripciones muy antiguas
        logger.info("Limpieza de suscripciones inactivas ejecutada");
    }
    
    // === MÉTODOS PRIVADOS ===
    
    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioRepository.findByNombre(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en base de datos"));
    }
    
    private PushSubscriptionResponse convertToResponse(PushSubscription subscription) {
        PushSubscriptionResponse response = new PushSubscriptionResponse();
        response.setId(subscription.getId());
        response.setUsuarioId(subscription.getUsuario().getUsuarioID().longValue());
        response.setEndpoint(subscription.getEndpoint());
        response.setActivo(subscription.getActivo());
        response.setFechaCreacion(subscription.getFechaCreacion());
        response.setFechaActualizacion(subscription.getFechaActualizacion());
        return response;
    }
}
