package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.DatosConfigurables;

public interface DatosConfigurablesRepository extends JpaRepository<DatosConfigurables, Long> {
    
    // Buscar datos por tipo y que estén activos
    @Query("SELECT d FROM DatosConfigurables d WHERE d.tipo = :tipo AND d.activo = true ORDER BY d.valor ASC")
    List<DatosConfigurables> findByTipoAndActivoTrue(@Param("tipo") String tipo);
    
    // Buscar todos los datos de un tipo (incluidos inactivos)
    @Query("SELECT d FROM DatosConfigurables d WHERE d.tipo = :tipo ORDER BY d.valor ASC")
    List<DatosConfigurables> findByTipo(@Param("tipo") String tipo);
    
    // Buscar por tipo y valor específico
    @Query("SELECT d FROM DatosConfigurables d WHERE d.tipo = :tipo AND d.valor = :valor")
    DatosConfigurables findByTipoAndValor(@Param("tipo") String tipo, @Param("valor") String valor);
}