package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private LoteRepository loteRepository;
    
    @Autowired
    private AnalisisRepository analisisRepository;
    
    @Autowired
    private LoteService loteService;
    
    @Autowired
    private PmsRepository pmsRepository;
    
    @Autowired
    private GerminacionRepository germinacionRepository;
    
    @Autowired
    private DosnRepository dosnRepository;
    
    @Autowired
    private TetrazolioRepository tetrazolioRepository;
    
    @Autowired
    private PurezaRepository purezaRepository;

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
    
    public List<AnalisisPendienteDTO> listarAnalisisPendientes() {
        List<AnalisisPendienteDTO> pendientes = new ArrayList<>();
        List<Lote> lotesActivos = loteRepository.findByActivoTrue();
        
        for (Lote lote : lotesActivos) {
            if (lote.getTiposAnalisisAsignados() == null) continue;
            
            for (TipoAnalisis tipo : lote.getTiposAnalisisAsignados()) {
                // Verificar si el análisis no existe o todos están marcados como A_REPETIR
                boolean pendiente = switch (tipo) {
                    case PMS -> {
                        // No existe ningún análisis
                        if (!pmsRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        // Existen análisis, verificar si TODOS están en A_REPETIR
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms> analisis = 
                            pmsRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case GERMINACION -> {
                        if (!germinacionRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion> analisis = 
                            germinacionRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case DOSN -> {
                        if (!dosnRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn> analisis = 
                            dosnRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case TETRAZOLIO -> {
                        if (!tetrazolioRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio> analisis = 
                            tetrazolioRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case PUREZA -> {
                        if (!purezaRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza> analisis = 
                            purezaRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    default -> false;
                };
                
                if (pendiente) {
                    // Crear DTO con información del lote y tipo de análisis
                    AnalisisPendienteDTO dto = new AnalisisPendienteDTO();
                    dto.setLoteID(lote.getLoteID());
                    dto.setNomLote(lote.getNomLote());
                    dto.setFicha(lote.getFicha());
                    dto.setEspecieNombre(lote.getCultivar() != null && lote.getCultivar().getEspecie() != null 
                        ? lote.getCultivar().getEspecie().getNombreComun() : "N/A");
                    dto.setCultivarNombre(lote.getCultivar() != null 
                        ? lote.getCultivar().getNombre() : "N/A");
                    dto.setTipoAnalisis(tipo);
                    
                    pendientes.add(dto);
                }
            }
        }
        
        return pendientes;
    }
    
    public List<AnalisisPorAprobarDTO> listarAnalisisPorAprobar() {
        List<AnalisisPorAprobarDTO> porAprobar = new ArrayList<>();
        
        // Buscar en cada repositorio de análisis los que estén en estado PENDIENTE_APROBACION
        
        // PMS
        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms> pmsList = 
            pmsRepository.findByEstado(Estado.PENDIENTE_APROBACION);
        for (var pms : pmsList) {
            AnalisisPorAprobarDTO dto = new AnalisisPorAprobarDTO();
            dto.setTipo(TipoAnalisis.PMS);
            dto.setAnalisisID(pms.getAnalisisID());
            dto.setLoteID(pms.getLote().getLoteID());
            dto.setNomLote(pms.getLote().getNomLote());
            dto.setFicha(pms.getLote().getFicha());
            dto.setFechaInicio(pms.getFechaInicio());
            dto.setFechaFin(pms.getFechaFin());
            porAprobar.add(dto);
        }
        
        // GERMINACION
        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion> germinacionList = 
            germinacionRepository.findByEstado(Estado.PENDIENTE_APROBACION);
        for (var germinacion : germinacionList) {
            AnalisisPorAprobarDTO dto = new AnalisisPorAprobarDTO();
            dto.setTipo(TipoAnalisis.GERMINACION);
            dto.setAnalisisID(germinacion.getAnalisisID());
            dto.setLoteID(germinacion.getLote().getLoteID());
            dto.setNomLote(germinacion.getLote().getNomLote());
            dto.setFicha(germinacion.getLote().getFicha());
            dto.setFechaInicio(germinacion.getFechaInicio());
            dto.setFechaFin(germinacion.getFechaFin());
            porAprobar.add(dto);
        }
        
        // DOSN
        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn> dosnList = 
            dosnRepository.findByEstado(Estado.PENDIENTE_APROBACION);
        for (var dosn : dosnList) {
            AnalisisPorAprobarDTO dto = new AnalisisPorAprobarDTO();
            dto.setTipo(TipoAnalisis.DOSN);
            dto.setAnalisisID(dosn.getAnalisisID());
            dto.setLoteID(dosn.getLote().getLoteID());
            dto.setNomLote(dosn.getLote().getNomLote());
            dto.setFicha(dosn.getLote().getFicha());
            dto.setFechaInicio(dosn.getFechaInicio());
            dto.setFechaFin(dosn.getFechaFin());
            porAprobar.add(dto);
        }
        
        // TETRAZOLIO
        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio> tetrazolioList = 
            tetrazolioRepository.findByEstado(Estado.PENDIENTE_APROBACION);
        for (var tetrazolio : tetrazolioList) {
            AnalisisPorAprobarDTO dto = new AnalisisPorAprobarDTO();
            dto.setTipo(TipoAnalisis.TETRAZOLIO);
            dto.setAnalisisID(tetrazolio.getAnalisisID());
            dto.setLoteID(tetrazolio.getLote().getLoteID());
            dto.setNomLote(tetrazolio.getLote().getNomLote());
            dto.setFicha(tetrazolio.getLote().getFicha());
            dto.setFechaInicio(tetrazolio.getFechaInicio());
            dto.setFechaFin(tetrazolio.getFechaFin());
            porAprobar.add(dto);
        }
        
        // PUREZA
        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza> purezaList = 
            purezaRepository.findByEstado(Estado.PENDIENTE_APROBACION);
        for (var pureza : purezaList) {
            AnalisisPorAprobarDTO dto = new AnalisisPorAprobarDTO();
            dto.setTipo(TipoAnalisis.PUREZA);
            dto.setAnalisisID(pureza.getAnalisisID());
            dto.setLoteID(pureza.getLote().getLoteID());
            dto.setNomLote(pureza.getLote().getNomLote());
            dto.setFicha(pureza.getLote().getFicha());
            dto.setFechaInicio(pureza.getFechaInicio());
            dto.setFechaFin(pureza.getFechaFin());
            porAprobar.add(dto);
        }
        
        return porAprobar;
    }
}
