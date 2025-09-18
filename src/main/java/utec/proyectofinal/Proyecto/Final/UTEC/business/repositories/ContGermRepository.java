package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ContGerm;

public interface ContGermRepository extends JpaRepository<ContGerm, Long> {
    
    // Buscar conteos por ID de germinación
    @Query("SELECT c FROM ContGerm c WHERE c.germinacion.analisisID = :germinacionId")
    List<ContGerm> findByGerminacionId(@Param("germinacionId") Long germinacionId);
    
    // Contar conteos por germinación
    @Query("SELECT COUNT(c) FROM ContGerm c WHERE c.germinacion.analisisID = :germinacionId")
    Long countByGerminacionId(@Param("germinacionId") Long germinacionId);
}