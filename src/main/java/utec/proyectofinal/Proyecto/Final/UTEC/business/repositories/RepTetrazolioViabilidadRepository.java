package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepTetrazolioViabilidad;

public interface RepTetrazolioViabilidadRepository extends JpaRepository<RepTetrazolioViabilidad, Long> {
    
    // Buscar repeticiones por ID de tetrazolio
    @Query("SELECT r FROM RepTetrazolioViabilidad r WHERE r.tetrazolio.analisisID = :tetrazolioId")
    List<RepTetrazolioViabilidad> findByTetrazolioId(@Param("tetrazolioId") Long tetrazolioId);
    
    // Contar repeticiones por tetrazolio
    @Query("SELECT COUNT(r) FROM RepTetrazolioViabilidad r WHERE r.tetrazolio.analisisID = :tetrazolioId")
    Long countByTetrazolioId(@Param("tetrazolioId") Long tetrazolioId);
}