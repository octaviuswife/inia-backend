package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ConfiguracionDatos;

public interface ConfiguracionDatosRepository extends JpaRepository<ConfiguracionDatos, Long> {
    
    // Buscar configuración por tipo
    @Query("SELECT c FROM ConfiguracionDatos c WHERE c.tipo = :tipo AND c.activo = true")
    Optional<ConfiguracionDatos> findByTipoAndActivoTrue(@Param("tipo") String tipo);
    
    // Obtener todas las configuraciones activas
    List<ConfiguracionDatos> findByActivoTrue();
}