package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPorAprobarProjection;

import java.util.List;

@Repository
public interface AnalisisPorAprobarRepository extends JpaRepository<Lote, Long> {
    
    /**
     * Query keyset (cursor-based) para análisis por aprobar.
     * Usa comparación de tupla (fecha_inicio DESC, analisisID DESC) para continuar después del cursor.
     * Más eficiente que OFFSET para deep pagination.
     */
    @Query(value = """
        SELECT * FROM (
            -- PMS
            SELECT 
                p.analisisid AS analisisID,
                'PMS' AS tipoAnalisis,
                l.loteid AS loteid,
                l.nom_lote AS nom_lote,
                l.ficha AS ficha,
                e.nombre_comun AS especieNombre,
                c.nombre AS cultivarNombre,
                TO_CHAR(a.fecha_inicio, 'YYYY-MM-DD HH24:MI:SS') AS fecha_inicio,
                TO_CHAR(a.fecha_fin, 'YYYY-MM-DD HH24:MI:SS') AS fecha_fin,
                a.fecha_inicio AS fecha_orden
            FROM pms p INNER JOIN analisis a ON p.analisisid = a.analisisid INNER JOIN lote l ON a.loteid = l.loteid
            INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
            INNER JOIN especie e ON c.especieid = e.especieid
            WHERE a.estado = 2 
            AND a.activo = true
            AND l.activo = true
            
            UNION ALL
            
            -- GERMINACION
            SELECT 
                g.analisisid AS analisisID,
                'GERMINACION' AS tipoAnalisis,
                l.loteid AS loteid,
                l.nom_lote AS nom_lote,
                l.ficha AS ficha,
                e.nombre_comun AS especieNombre,
                c.nombre AS cultivarNombre,
                TO_CHAR(a.fecha_inicio, 'YYYY-MM-DD HH24:MI:SS') AS fecha_inicio,
                TO_CHAR(a.fecha_fin, 'YYYY-MM-DD HH24:MI:SS') AS fecha_fin,
                a.fecha_inicio AS fecha_orden
            FROM germinacion g INNER JOIN analisis a ON g.analisisid = a.analisisid INNER JOIN lote l ON a.loteid = l.loteid
            INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
            INNER JOIN especie e ON c.especieid = e.especieid
            WHERE a.estado = 2 
            AND a.activo = true
            AND l.activo = true
            
            UNION ALL
            
            -- DOSN
            SELECT 
                d.analisisid AS analisisID,
                'DOSN' AS tipoAnalisis,
                l.loteid AS loteid,
                l.nom_lote AS nom_lote,
                l.ficha AS ficha,
                e.nombre_comun AS especieNombre,
                c.nombre AS cultivarNombre,
                TO_CHAR(a.fecha_inicio, 'YYYY-MM-DD HH24:MI:SS') AS fecha_inicio,
                TO_CHAR(a.fecha_fin, 'YYYY-MM-DD HH24:MI:SS') AS fecha_fin,
                a.fecha_inicio AS fecha_orden
            FROM dosn d INNER JOIN analisis a ON d.analisisid = a.analisisid INNER JOIN lote l ON a.loteid = l.loteid
            INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
            INNER JOIN especie e ON c.especieid = e.especieid
            WHERE a.estado = 2 
            AND a.activo = true
            AND l.activo = true
            
            UNION ALL
            
            -- TETRAZOLIO
            SELECT 
                t.analisisid AS analisisID,
                'TETRAZOLIO' AS tipoAnalisis,
                l.loteid AS loteid,
                l.nom_lote AS nom_lote,
                l.ficha AS ficha,
                e.nombre_comun AS especieNombre,
                c.nombre AS cultivarNombre,
                TO_CHAR(a.fecha_inicio, 'YYYY-MM-DD HH24:MI:SS') AS fecha_inicio,
                TO_CHAR(a.fecha_fin, 'YYYY-MM-DD HH24:MI:SS') AS fecha_fin,
                a.fecha_inicio AS fecha_orden
            FROM tetrazolio t INNER JOIN analisis a ON t.analisisid = a.analisisid INNER JOIN lote l ON a.loteid = l.loteid
            INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
            INNER JOIN especie e ON c.especieid = e.especieid
            WHERE a.estado = 2 
            AND a.activo = true
            AND l.activo = true
            
            UNION ALL
            
            -- PUREZA
            SELECT 
                pu.analisisid AS analisisID,
                'PUREZA' AS tipoAnalisis,
                l.loteid AS loteid,
                l.nom_lote AS nom_lote,
                l.ficha AS ficha,
                e.nombre_comun AS especieNombre,
                c.nombre AS cultivarNombre,
                TO_CHAR(a.fecha_inicio, 'YYYY-MM-DD HH24:MI:SS') AS fecha_inicio,
                TO_CHAR(a.fecha_fin, 'YYYY-MM-DD HH24:MI:SS') AS fecha_fin,
                a.fecha_inicio AS fecha_orden
            FROM pureza pu INNER JOIN analisis a ON pu.analisisid = a.analisisid INNER JOIN lote l ON a.loteid = l.loteid
            INNER JOIN cultivar c ON l.cultivarid = c.cultivarid
            INNER JOIN especie e ON c.especieid = e.especieid
            WHERE a.estado = 2 
            AND a.activo = true
            AND l.activo = true
        ) AS combined
        WHERE (fecha_orden, analisisID) < (CAST(:lastFecha AS timestamp), :lastId)
        ORDER BY fecha_orden DESC NULLS LAST, analisisID DESC
        LIMIT :size
        """,
        nativeQuery = true)
    List<AnalisisPorAprobarProjection> findNextPageByCursor(
            String lastFecha,
            Long lastId,
            int size);
}

