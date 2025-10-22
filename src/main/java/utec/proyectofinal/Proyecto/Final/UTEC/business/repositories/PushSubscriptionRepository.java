package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.PushSubscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    
    List<PushSubscription> findByUsuarioUsuarioIDAndIsActiveTrue(Integer usuarioId);
    
    Optional<PushSubscription> findByEndpoint(String endpoint);
    
    void deleteByUsuarioUsuarioID(Integer usuarioId);
    
    List<PushSubscription> findAllByIsActiveTrue();
}
