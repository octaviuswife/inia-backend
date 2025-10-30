package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.PushSubscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    /**
     * Buscar todas las suscripciones activas
     */
    List<PushSubscription> findAllByIsActiveTrue();

    /**
     * Buscar suscripciones activas de un usuario específico
     */
    List<PushSubscription> findByUsuarioUsuarioIDAndIsActiveTrue(Integer usuarioId);

    /**
     * Eliminar todas las suscripciones de un usuario
     */
    void deleteByUsuarioUsuarioID(Integer usuarioId);

    /**
     * Buscar suscripción por endpoint
     * Útil para evitar duplicados y para desuscripciones sin autenticación
     */
    Optional<PushSubscription> findByEndpoint(String endpoint);

    /**
     * Contar suscripciones activas
     */
    long countByIsActiveTrue();
}