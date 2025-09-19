package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasYCultivosCatalogo;

import java.util.List;

@Repository
public interface MalezasYCultivosCatalogoRepository extends JpaRepository<MalezasYCultivosCatalogo, Long> {
    
    List<MalezasYCultivosCatalogo> findByActivoTrue();
    
    List<MalezasYCultivosCatalogo> findByActivoFalse();
    
    List<MalezasYCultivosCatalogo> findByMalezaAndActivoTrue(Boolean maleza);
    
    List<MalezasYCultivosCatalogo> findByNombreComunContainingIgnoreCaseAndActivoTrue(String nombreComun);
    
    List<MalezasYCultivosCatalogo> findByNombreCientificoContainingIgnoreCaseAndActivoTrue(String nombreCientifico);
}