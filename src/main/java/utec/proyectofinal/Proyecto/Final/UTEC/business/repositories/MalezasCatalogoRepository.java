package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;

import java.util.List;

@Repository
public interface MalezasCatalogoRepository extends JpaRepository<MalezasCatalogo, Long> {
    
    List<MalezasCatalogo> findByActivoTrue();
    
    List<MalezasCatalogo> findByActivoFalse();
    
    List<MalezasCatalogo> findByNombreComunContainingIgnoreCaseAndActivoTrue(String nombreComun);
    
    List<MalezasCatalogo> findByNombreCientificoContainingIgnoreCaseAndActivoTrue(String nombreCientifico);
}