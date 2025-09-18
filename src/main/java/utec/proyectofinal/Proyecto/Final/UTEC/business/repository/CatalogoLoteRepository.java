package utec.proyectofinal.Proyecto.Final.UTEC.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.CatalogoLote;

import java.util.List;

@Repository
public interface CatalogoLoteRepository extends JpaRepository<CatalogoLote, Long> {
    
    List<CatalogoLote> findByTipoAndActivoTrue(CatalogoLote.TipoCatalogo tipo);
    
    CatalogoLote findFirstByTipoAndActivoTrue(CatalogoLote.TipoCatalogo tipo);
}