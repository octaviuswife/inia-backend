package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;

import java.util.List;

public interface RepPmsRepository extends JpaRepository<RepPms, Long> {

    @Query("SELECT r FROM RepPms r WHERE r.pms.analisisID = :pmsId")
    List<RepPms> findByPmsId(@Param("pmsId") Long pmsId);

    @Query("SELECT COUNT(r) FROM RepPms r WHERE r.pms.analisisID = :pmsId")
    Long countByPmsId(@Param("pmsId") Long pmsId);
}

