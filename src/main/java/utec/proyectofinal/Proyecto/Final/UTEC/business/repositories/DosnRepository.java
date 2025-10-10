package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.util.List;

public interface DosnRepository extends JpaRepository<Dosn, Long> {

    List<Dosn> findByEstadoNot(Estado estado);
    List<Dosn> findByEstado(Estado estado);

    @Query("SELECT d FROM Dosn d WHERE d.lote.loteID = :idLote")
    List<Dosn> findByIdLote(@Param("idLote") Integer idLote);
    
    List<Dosn> findByLoteLoteID(Long loteID);

}
