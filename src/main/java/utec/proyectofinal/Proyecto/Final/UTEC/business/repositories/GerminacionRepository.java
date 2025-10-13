package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

public interface GerminacionRepository extends JpaRepository<Germinacion, Long> {
    
    List<Germinacion> findByEstadoNot(Estado estado);
    
    List<Germinacion> findByEstado(Estado estado);
    
    // Método con paginado para listado
    Page<Germinacion> findByEstadoNotOrderByFechaInicioDesc(Estado estado, Pageable pageable);
    
    @Query("SELECT g FROM Germinacion g WHERE g.lote.loteID = :idLote")
    List<Germinacion> findByIdLote(@Param("idLote") Long idLote);
    
    List<Germinacion> findByLoteLoteID(Long loteID);
    
    // Métodos eficientes para validaciones
    boolean existsByLoteLoteID(Long loteID);
    boolean existsByLoteLoteIDAndEstado(Long loteID, Estado estado);
}