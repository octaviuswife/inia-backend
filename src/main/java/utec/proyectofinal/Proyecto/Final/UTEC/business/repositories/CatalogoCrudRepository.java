package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoCrudRepository extends JpaRepository<Catalogo, Long> {
    
    // Buscar por tipo y activos
    List<Catalogo> findByTipoAndActivoTrue(TipoCatalogo tipo);
    
    // Buscar por tipo y inactivos
    List<Catalogo> findByTipoAndActivoFalse(TipoCatalogo tipo);
    
    // Buscar por tipo (incluyendo inactivos)
    List<Catalogo> findByTipo(TipoCatalogo tipo);
    
    // Buscar por tipo y valor específico
    Optional<Catalogo> findByTipoAndValorAndActivoTrue(TipoCatalogo tipo, String valor);
    
    // Buscar solo los activos
    List<Catalogo> findByActivoTrue();
    
    // Buscar por valor (para validar duplicados)
    @Query("SELECT c FROM Catalogo c WHERE c.tipo = :tipo AND c.valor = :valor")
    Optional<Catalogo> findByTipoAndValor(@Param("tipo") TipoCatalogo tipo, @Param("valor") String valor);
}