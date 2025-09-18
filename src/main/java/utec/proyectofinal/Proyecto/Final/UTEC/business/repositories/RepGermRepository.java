package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;

@Repository
public interface RepGermRepository extends JpaRepository<RepGerm, Long> {

    // Encontrar todas las repeticiones de un conteo específico
    @Query("SELECT r FROM RepGerm r WHERE r.contGerm.contGermID = :contGermId")
    List<RepGerm> findByContGermId(@Param("contGermId") Long contGermId);

    // Contar repeticiones de un conteo específico
    @Query("SELECT COUNT(r) FROM RepGerm r WHERE r.contGerm.contGermID = :contGermId")
    Long countByContGermId(@Param("contGermId") Long contGermId);

    // Encontrar por número de repetición en un conteo específico
    @Query("SELECT r FROM RepGerm r WHERE r.contGerm.contGermID = :contGermId AND r.numRep = :numRep")
    RepGerm findByContGermIdAndNumRep(@Param("contGermId") Long contGermId, @Param("numRep") Integer numRep);

    // Encontrar repeticiones ordenadas por número
    @Query("SELECT r FROM RepGerm r WHERE r.contGerm.contGermID = :contGermId ORDER BY r.numRep ASC")
    List<RepGerm> findByContGermIdOrderByNumRep(@Param("contGermId") Long contGermId);
}