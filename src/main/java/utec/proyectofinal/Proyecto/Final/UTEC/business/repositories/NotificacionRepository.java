package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Notificacion;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    Page<Notificacion> findByUsuarioIdAndActivoTrueOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);
    
    List<Notificacion> findByUsuarioIdAndLeidoFalseAndActivoTrueOrderByFechaCreacionDesc(Long usuarioId);
    
    List<Notificacion> findByUsuarioIdAndLeidoFalseAndActivoTrue(Long usuarioId);
    
    Long countByUsuarioIdAndLeidoFalseAndActivoTrue(Long usuarioId);
    
    List<Notificacion> findByAnalisisIdAndActivoTrue(Long analisisId);
}