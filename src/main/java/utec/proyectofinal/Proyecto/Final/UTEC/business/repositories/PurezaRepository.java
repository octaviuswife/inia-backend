package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

public interface PurezaRepository extends JpaRepository<Pureza, Long> {
    
    List<Pureza> findByEstadoNot(Estado estado);
    
    List<Pureza> findByEstado(Estado estado);
    
    @Query("SELECT p FROM Pureza p WHERE p.lote.loteID = :idLote")
    List<Pureza> findByIdLote(@Param("idLote") Integer idLote);
}
