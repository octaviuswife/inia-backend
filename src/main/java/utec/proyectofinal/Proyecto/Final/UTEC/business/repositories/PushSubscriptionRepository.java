package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.PushSubscription;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    
    List<PushSubscription> findByUsuarioUsuarioIDAndActivoTrue(Integer usuarioId);
    
    Optional<PushSubscription> findByEndpointAndActivoTrue(String endpoint);
    
    List<PushSubscription> findByActivoTrue();
    
    boolean existsByEndpointAndActivoTrue(String endpoint);
    
    void deleteByEndpoint(String endpoint);
}
