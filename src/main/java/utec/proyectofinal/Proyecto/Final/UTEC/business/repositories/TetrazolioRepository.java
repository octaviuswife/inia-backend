package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

public interface TetrazolioRepository extends JpaRepository<Tetrazolio, Long> {
    
    List<Tetrazolio> findByEstadoNot(Estado estado);
    
    List<Tetrazolio> findByEstado(Estado estado);
    
    @Query("SELECT t FROM Tetrazolio t WHERE t.lote.loteID = :idLote")
    List<Tetrazolio> findByIdLote(@Param("idLote") Long idLote);
    
    List<Tetrazolio> findByLoteLoteID(Long loteID);

    
    // Métodos eficientes para validaciones
    boolean existsByLoteLoteID(Long loteID);
    boolean existsByLoteLoteIDAndEstado(Long loteID, Estado estado);


    // Pageable
    Page<Tetrazolio> findByEstadoNotOrderByFechaInicioDesc(Estado estado, Pageable pageable);

}