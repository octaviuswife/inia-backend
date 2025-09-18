package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;

public interface TablaGermRepository extends JpaRepository<TablaGerm, Long> {
    
    // Buscar tablas por ID de germinación
    @Query("SELECT t FROM TablaGerm t WHERE t.germinacion.analisisID = :germinacionId")
    List<TablaGerm> findByGerminacionId(@Param("germinacionId") Long germinacionId);
    
    // Contar tablas por germinación
    @Query("SELECT COUNT(t) FROM TablaGerm t WHERE t.germinacion.analisisID = :germinacionId")
    Long countByGerminacionId(@Param("germinacionId") Long germinacionId);
}