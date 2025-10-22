package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.CursorPageResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.KeysetCursor;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisPendienteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisPorAprobarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPendienteProjection;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPorAprobarProjection;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private LoteRepository loteRepository;
    
    @Autowired
    private AnalisisRepository analisisRepository;
    
    @Autowired
    private AnalisisPendienteRepository analisisPendienteRepository;
    
    @Autowired
    private AnalisisPorAprobarRepository analisisPorAprobarRepository;
    
    @Autowired
    private LoteService loteService;

    public DashboardStatsDTO obtenerEstadisticas() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 1. Lotes activos
        stats.setLotesActivos(loteRepository.countLotesActivos());
        
        // 2. Análisis pendientes (asignados pero no realizados o marcados como A_REPETIR)
        stats.setAnalisisPendientes(loteService.contarAnalisisPendientes());
        
        // 3. Completados hoy (finalizados en la fecha actual con estado APROBADO)
        stats.setCompletadosHoy(analisisRepository.countCompletadosEnFecha(LocalDate.now(), Estado.APROBADO));
        
        // 4. Análisis por aprobar (estado PENDIENTE_APROBACION)
        stats.setAnalisisPorAprobar(analisisRepository.countByEstado(Estado.PENDIENTE_APROBACION));
        
        return stats;
    }
    
    /**
     * Keyset pagination para análisis pendientes.
     * 
     * @param encodedCursor Cursor Base64 (null para primera página)
     * @param size Número de items por página
     * @return Página con items y nextCursor encoded
     */
    public CursorPageResponse<AnalisisPendienteDTO> listarAnalisisPendientesKeyset(
            String encodedCursor, int size) {
        
        List<AnalisisPendienteProjection> proyecciones;
        
        if (encodedCursor == null || encodedCursor.trim().isEmpty()) {
            // Primera página
            proyecciones = analisisPendienteRepository.findNextPageByCursor(
                0L, "", size + 1);
        } else {
            // Decodificar cursor (lanza InvalidCursorException si es inválido)
            KeysetCursor cursor = KeysetCursor.decode(encodedCursor);
            
            // Para analisis-pendientes, usamos lastId como loteId y lastFecha como tipo
            Long lastLoteId = cursor.getLastId();
            String lastTipo = cursor.getLastFecha() != null ? cursor.getLastFecha() : "";
            
            proyecciones = analisisPendienteRepository.findNextPageByCursor(
                lastLoteId, lastTipo, size + 1);
        }
        
        // Convertir a DTOs
        List<AnalisisPendienteDTO> items = proyecciones.stream()
            .limit(size)
            .map(p -> new AnalisisPendienteDTO(
                p.getLoteID(),
                p.getNomLote(),
                p.getFicha(),
                p.getEspecieNombre(),
                p.getCultivarNombre(),
                TipoAnalisis.valueOf(p.getTipoAnalisis())
            ))
            .collect(Collectors.toList());
        
        // Verificar si hay más resultados
        boolean hasMore = proyecciones.size() > size;
        
        if (hasMore && !items.isEmpty()) {
            AnalisisPendienteDTO lastItem = items.get(items.size() - 1);
            KeysetCursor cursor = new KeysetCursor(
                lastItem.getTipoAnalisis().name(),  // Guardar tipo como "fecha"
                lastItem.getLoteID()
            );
            return CursorPageResponse.of(items, cursor, size);
        } else {
            return CursorPageResponse.lastPage(items, size);
        }
    }
    
    /**
     * Keyset pagination para análisis por aprobar.
     * 
     * @param encodedCursor Cursor Base64 (null para primera página)
     * @param size Número de items por página
     * @return Página con items y nextCursor encoded
     */
    public CursorPageResponse<AnalisisPorAprobarDTO> listarAnalisisPorAprobarKeyset(
            String encodedCursor, int size) {
        
        List<AnalisisPorAprobarProjection> proyecciones;
        
        if (encodedCursor == null || encodedCursor.trim().isEmpty()) {
            // Primera página
            proyecciones = analisisPorAprobarRepository.findNextPageByCursor(
                "9999-12-31 23:59:59", Long.MAX_VALUE, size + 1);
        } else {
            // Decodificar cursor (lanza InvalidCursorException si es inválido)
            KeysetCursor cursor = KeysetCursor.decode(encodedCursor);
            
            String lastFecha = cursor.getLastFecha();
            Long lastId = cursor.getLastId();
            
            proyecciones = analisisPorAprobarRepository.findNextPageByCursor(
                lastFecha, lastId, size + 1);
        }
        
        // Convertir a DTOs
        List<AnalisisPorAprobarDTO> items = proyecciones.stream()
            .limit(size)
            .map(p -> {
                AnalisisPorAprobarDTO dto = new AnalisisPorAprobarDTO();
                dto.setAnalisisID(p.getAnalisisID());
                dto.setTipo(TipoAnalisis.valueOf(p.getTipoAnalisis()));
                dto.setLoteID(p.getLoteID());
                dto.setNomLote(p.getNomLote());
                dto.setFicha(p.getFicha());
                
                // Convertir fechas String a LocalDateTime
                if (p.getFechaInicio() != null) {
                    dto.setFechaInicio(LocalDateTime.parse(p.getFechaInicio(), 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                if (p.getFechaFin() != null) {
                    dto.setFechaFin(LocalDateTime.parse(p.getFechaFin(), 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        // Verificar si hay más resultados
        boolean hasMore = proyecciones.size() > size;
        
        if (hasMore && !items.isEmpty()) {
            AnalisisPorAprobarDTO lastItem = items.get(items.size() - 1);
            
            // Formatear fecha para cursor (mismo formato que en la query)
            String fechaStr = lastItem.getFechaInicio() != null 
                ? lastItem.getFechaInicio().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null;
            
            KeysetCursor cursor = new KeysetCursor(
                fechaStr,
                lastItem.getAnalisisID()
            );
            return CursorPageResponse.of(items, cursor, size);
        } else {
            return CursorPageResponse.lastPage(items, size);
        }
    }
}
