package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Legado;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegadoRepository extends JpaRepository<Legado, Long>, JpaSpecificationExecutor<Legado> {
    
    // Buscar por lote
    List<Legado> findByLote_LoteID(Long loteID);
    
    // Buscar por archivo origen
    List<Legado> findByArchivoOrigen(String archivoOrigen);
    
    // Buscar por número de ficha (a través del lote)
    @Query("SELECT l FROM Legado l WHERE l.lote.ficha = :ficha AND l.activo = true")
    List<Legado> findByFicha(@Param("ficha") String ficha);
    
    // Verificar si ya existe un registro de una fila específica de un archivo
    Optional<Legado> findByArchivoOrigenAndFilaExcel(String archivoOrigen, Integer filaExcel);
    
    // Buscar todos los activos
    List<Legado> findByActivoTrue();
}
