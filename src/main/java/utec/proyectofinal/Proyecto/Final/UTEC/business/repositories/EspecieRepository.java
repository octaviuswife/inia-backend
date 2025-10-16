package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspecieRepository extends JpaRepository<Especie, Long> {
    
    List<Especie> findByActivoTrue();
    
    List<Especie> findByActivoFalse();
    
    List<Especie> findByNombreComunContainingIgnoreCaseAndActivoTrue(String nombreComun);
    
    List<Especie> findByNombreCientificoContainingIgnoreCaseAndActivoTrue(String nombreCientifico);
    
    Optional<Especie> findByNombreComun(String nombreComun);
}