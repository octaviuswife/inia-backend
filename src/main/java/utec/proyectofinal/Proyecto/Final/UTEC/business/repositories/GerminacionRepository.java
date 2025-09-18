package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

public interface GerminacionRepository extends JpaRepository<Germinacion, Long> {
    
    List<Germinacion> findByEstadoNot(Estado estado);
    
    List<Germinacion> findByEstado(Estado estado);
    
    @Query("SELECT g FROM Germinacion g WHERE g.lote.loteID = :idLote")
    List<Germinacion> findByIdLote(@Param("idLote") Long idLote);
}