package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.util.List;

public interface PmsRepository extends JpaRepository<Pms, Long> {
    List<Pms> findByEstadoNot(Estado estado);
    List<Pms> findByEstado(Estado estado);

    @Query("SELECT p FROM Pms p WHERE p.lote.loteID = :idLote")
    List<Pms> findByIdLote(@Param("idLote") Integer idLote);
    
    List<Pms> findByLoteLoteID(Long loteID);

    // Pageable
    Page<Pms> findByEstadoNotOrderByFechaInicioDesc(Estado estado, Pageable pageable);

}
