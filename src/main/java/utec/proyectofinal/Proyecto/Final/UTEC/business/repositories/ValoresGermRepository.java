package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

public interface ValoresGermRepository extends JpaRepository<ValoresGerm, Long> {
    
    // Buscar valores por ID de conteo
    @Query("SELECT v FROM ValoresGerm v WHERE v.contGerm.contGermID = :contGermId")
    List<ValoresGerm> findByContGermId(@Param("contGermId") Long contGermId);
    
    // Buscar valores por conteo e instituto
    @Query("SELECT v FROM ValoresGerm v WHERE v.contGerm.contGermID = :contGermId AND v.instituto = :instituto")
    Optional<ValoresGerm> findByContGermIdAndInstituto(@Param("contGermId") Long contGermId, @Param("instituto") Instituto instituto);
}
