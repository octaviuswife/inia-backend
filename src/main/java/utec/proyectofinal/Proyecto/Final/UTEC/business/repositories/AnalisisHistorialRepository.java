package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.AnalisisHistorial;

@Repository
public interface AnalisisHistorialRepository extends JpaRepository<AnalisisHistorial, Long> {

    /**
     * Obtener historial de un análisis específico
     */
    @Query("SELECT ah FROM AnalisisHistorial ah WHERE ah.analisis.analisisID = :analisisId ORDER BY ah.fechaHora DESC")
    List<AnalisisHistorial> findByAnalisisIdOrderByFechaHoraDesc(@Param("analisisId") Long analisisId);

    /**
     * Obtener historial de análisis por usuario
     */
    @Query("SELECT ah FROM AnalisisHistorial ah WHERE ah.usuario.usuarioID = :usuarioId ORDER BY ah.fechaHora DESC")
    List<AnalisisHistorial> findByUsuarioIdOrderByFechaHoraDesc(@Param("usuarioId") Integer usuarioId);
}