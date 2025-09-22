package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepTetrazolioViabilidadRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeadosRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoTetrazolio;

@Service
public class TetrazolioService {

    @Autowired
    private TetrazolioRepository tetrazolioRepository;

    @Autowired
    private RepTetrazolioViabilidadRepository repeticionRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Tetrazolio con estado REGISTRADO
    public TetrazolioDTO crearTetrazolio(TetrazolioRequestDTO solicitud) {
        try {
            System.out.println("Iniciando creación de tetrazolio con solicitud: " + solicitud);
            
            Tetrazolio tetrazolio = mapearSolicitudAEntidad(solicitud);
            tetrazolio.setEstado(Estado.REGISTRADO);
            
            Tetrazolio tetrazolioGuardado = tetrazolioRepository.save(tetrazolio);
            
            // Registrar automáticamente en el historial
            analisisHistorialService.registrarCreacion(tetrazolioGuardado);
            
            System.out.println("Tetrazolio creado exitosamente con ID: " + tetrazolioGuardado.getAnalisisID());
            
            return mapearEntidadADTO(tetrazolioGuardado);
        } catch (Exception e) {
            System.err.println("Error al crear tetrazolio: " + e.getMessage());
            throw new RuntimeException("Error al crear el análisis de tetrazolio: " + e.getMessage());
        }
    }

    // Editar Tetrazolio
    public TetrazolioDTO actualizarTetrazolio(Long id, TetrazolioRequestDTO solicitud) {
        Optional<Tetrazolio> tetrazolioExistente = tetrazolioRepository.findById(id);
        
        if (tetrazolioExistente.isPresent()) {
            Tetrazolio tetrazolio = tetrazolioExistente.get();
            
            // Si el análisis está APROBADO y el usuario actual es ANALISTA, cambiar a PENDIENTE_APROBACION
            if (tetrazolio.getEstado() == Estado.APROBADO && analisisService.esAnalista()) {
                tetrazolio.setEstado(Estado.PENDIENTE_APROBACION);
            }
            
            actualizarEntidadDesdeSolicitud(tetrazolio, solicitud);
            Tetrazolio tetrazolioActualizado = tetrazolioRepository.save(tetrazolio);
            
            // Registrar automáticamente en el historial
            analisisHistorialService.registrarModificacion(tetrazolioActualizado);
            
            return mapearEntidadADTO(tetrazolioActualizado);
        } else {
            throw new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id);
        }
    }

    // Eliminar Tetrazolio (cambiar estado a INACTIVO)
    public void eliminarTetrazolio(Long id) {
        Optional<Tetrazolio> tetrazolioExistente = tetrazolioRepository.findById(id);
        
        if (tetrazolioExistente.isPresent()) {
            Tetrazolio tetrazolio = tetrazolioExistente.get();
            tetrazolio.setEstado(Estado.INACTIVO);
            tetrazolioRepository.save(tetrazolio);
        } else {
            throw new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id);
        }
    }

    // Listar todos los Tetrazolios activos usando ResponseListadoTetrazolio
    public ResponseListadoTetrazolio obtenerTodosTetrazolio() {
        List<Tetrazolio> tetrazoliosActivos = tetrazolioRepository.findByEstadoNot(Estado.INACTIVO);
        List<TetrazolioDTO> tetrazoliosDTO = tetrazoliosActivos.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
        
        ResponseListadoTetrazolio response = new ResponseListadoTetrazolio();
        response.setTetrazolios(tetrazoliosDTO);
        return response;
    }

    // Obtener Tetrazolio por ID
    public TetrazolioDTO obtenerTetrazolioPorId(Long id) {
        Optional<Tetrazolio> tetrazolio = tetrazolioRepository.findById(id);
        if (tetrazolio.isPresent()) {
            return mapearEntidadADTO(tetrazolio.get());
        } else {
            throw new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id);
        }
    }

    // Obtener Tetrazolios por Lote
    public List<TetrazolioDTO> obtenerTetrazoliosPorIdLote(Long idLote) {
        List<Tetrazolio> tetrazolios = tetrazolioRepository.findByIdLote(idLote);
        return tetrazolios.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Actualizar porcentajes redondeados (solo cuando todas las repeticiones estén completas)
    public TetrazolioDTO actualizarPorcentajesRedondeados(Long id, PorcentajesRedondeadosRequestDTO solicitud) {
        Optional<Tetrazolio> tetrazolioExistente = tetrazolioRepository.findById(id);
        
        if (tetrazolioExistente.isPresent()) {
            Tetrazolio tetrazolio = tetrazolioExistente.get();
            
            // Validar que se hayan completado todas las repeticiones antes de permitir actualizar porcentajes
            validarCompletitudRepeticiones(tetrazolio);
            
            // Actualizar solo los porcentajes
            tetrazolio.setPorcViablesRedondeo(solicitud.getPorcViablesRedondeo());
            tetrazolio.setPorcNoViablesRedondeo(solicitud.getPorcNoViablesRedondeo());
            tetrazolio.setPorcDurasRedondeo(solicitud.getPorcDurasRedondeo());
            
            Tetrazolio tetrazolioActualizado = tetrazolioRepository.save(tetrazolio);
            System.out.println("Porcentajes redondeados actualizados exitosamente para tetrazolio ID: " + id);
            return mapearEntidadADTO(tetrazolioActualizado);
        } else {
            throw new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id);
        }
    }

    // Mapear de RequestDTO a Entity para creación
    private Tetrazolio mapearSolicitudAEntidad(TetrazolioRequestDTO solicitud) {
        System.out.println("Mapeando solicitud a entidad tetrazolio");
        
        // Validar que se especifique el número de repeticiones esperadas
        if (solicitud.getNumRepeticionesEsperadas() == null || solicitud.getNumRepeticionesEsperadas() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones esperadas (mayor a 0).");
        }
        
        Tetrazolio tetrazolio = new Tetrazolio();
        
        // Datos del análisis base
        tetrazolio.setFechaInicio(solicitud.getFechaInicio());
        tetrazolio.setFechaFin(solicitud.getFechaFin());
        tetrazolio.setCumpleEstandar(solicitud.getCumpleEstandar());
        tetrazolio.setComentarios(solicitud.getComentarios());
        
        // Validar y establecer lote
        if (solicitud.getIdLote() != null) {
            System.out.println("Buscando lote con ID: " + solicitud.getIdLote());
            Lote lote = entityManager.find(Lote.class, solicitud.getIdLote());
            if (lote != null) {
                tetrazolio.setLote(lote);
                System.out.println("Lote encontrado y asignado: " + lote.getLoteID());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }
        
        // Datos específicos de Tetrazolio
        tetrazolio.setNumSemillasPorRep(solicitud.getNumSemillasPorRep());
        tetrazolio.setPretratamiento(solicitud.getPretratamiento());
        tetrazolio.setConcentracion(solicitud.getConcentracion());
        tetrazolio.setTincionHs(solicitud.getTincionHs());
        tetrazolio.setTincionTemp(solicitud.getTincionTemp());
        tetrazolio.setFecha(solicitud.getFecha());
        tetrazolio.setNumRepeticionesEsperadas(solicitud.getNumRepeticionesEsperadas());
        
        System.out.println("Tetrazolio mapeado exitosamente");
        return tetrazolio;
    }

    // Actualizar Entity desde RequestDTO para edición
    private void actualizarEntidadDesdeSolicitud(Tetrazolio tetrazolio, TetrazolioRequestDTO solicitud) {
        System.out.println("Actualizando tetrazolio desde solicitud");
        
        // Datos del análisis base
        tetrazolio.setFechaInicio(solicitud.getFechaInicio());
        tetrazolio.setFechaFin(solicitud.getFechaFin());
        tetrazolio.setCumpleEstandar(solicitud.getCumpleEstandar());
        tetrazolio.setComentarios(solicitud.getComentarios());
        
        // Validar y establecer lote si se proporciona
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.find(Lote.class, solicitud.getIdLote());
            if (lote != null) {
                tetrazolio.setLote(lote);
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }
        
        // Datos específicos de Tetrazolio
        tetrazolio.setNumSemillasPorRep(solicitud.getNumSemillasPorRep());
        tetrazolio.setPretratamiento(solicitud.getPretratamiento());
        tetrazolio.setConcentracion(solicitud.getConcentracion());
        tetrazolio.setTincionHs(solicitud.getTincionHs());
        tetrazolio.setTincionTemp(solicitud.getTincionTemp());
        tetrazolio.setFecha(solicitud.getFecha());
        tetrazolio.setNumRepeticionesEsperadas(solicitud.getNumRepeticionesEsperadas());
        
        System.out.println("Tetrazolio actualizado exitosamente");
    }

    // Mapear de Entity a DTO
    private TetrazolioDTO mapearEntidadADTO(Tetrazolio tetrazolio) {
        TetrazolioDTO dto = new TetrazolioDTO();
        
        // Datos del análisis base
        dto.setAnalisisID(tetrazolio.getAnalisisID());
        dto.setEstado(tetrazolio.getEstado());
        dto.setFechaInicio(tetrazolio.getFechaInicio());
        dto.setFechaFin(tetrazolio.getFechaFin());
        dto.setCumpleEstandar(tetrazolio.getCumpleEstandar());
        dto.setComentarios(tetrazolio.getComentarios());
        
        // Datos del lote si existe
        if (tetrazolio.getLote() != null) {
            dto.setLote(tetrazolio.getLote().getFicha());
        }
        
        // Datos específicos de Tetrazolio
        dto.setNumSemillasPorRep(tetrazolio.getNumSemillasPorRep());
        dto.setPretratamiento(tetrazolio.getPretratamiento());
        dto.setConcentracion(tetrazolio.getConcentracion());
        dto.setTincionHs(tetrazolio.getTincionHs());
        dto.setTincionTemp(tetrazolio.getTincionTemp());
        dto.setFecha(tetrazolio.getFecha());
        dto.setNumRepeticionesEsperadas(tetrazolio.getNumRepeticionesEsperadas());
        dto.setPorcViablesRedondeo(tetrazolio.getPorcViablesRedondeo());
        dto.setPorcNoViablesRedondeo(tetrazolio.getPorcNoViablesRedondeo());
        dto.setPorcDurasRedondeo(tetrazolio.getPorcDurasRedondeo());
        
        return dto;
    }
    
    // Validar que se hayan completado todas las repeticiones esperadas
    private void validarCompletitudRepeticiones(Tetrazolio tetrazolio) {
        if (tetrazolio.getNumRepeticionesEsperadas() != null && tetrazolio.getNumRepeticionesEsperadas() > 0) {
            Long repeticionesCreadas = repeticionRepository.countByTetrazolioId(tetrazolio.getAnalisisID());
            
            if (repeticionesCreadas < tetrazolio.getNumRepeticionesEsperadas()) {
                throw new RuntimeException(
                    String.format("No se puede finalizar el análisis. Se esperan %d repeticiones pero solo hay %d creadas. " +
                                "Complete todas las repeticiones antes de finalizar o actualizar porcentajes.", 
                                tetrazolio.getNumRepeticionesEsperadas(), repeticionesCreadas));
            }
        }
    }
    
    // Cambiar estado a PENDIENTE_APROBACION (con validaciones)
    public TetrazolioDTO enviarAprobacion(Long id) {
        Optional<Tetrazolio> tetrazolioExistente = tetrazolioRepository.findById(id);
        
        if (tetrazolioExistente.isPresent()) {
            Tetrazolio tetrazolio = tetrazolioExistente.get();
            validarCompletitudRepeticiones(tetrazolio);
            tetrazolio.setEstado(Estado.PENDIENTE_APROBACION);
            Tetrazolio tetrazolioActualizado = tetrazolioRepository.save(tetrazolio);
            return mapearEntidadADTO(tetrazolioActualizado);
        } else {
            throw new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id);
        }
    }
    
    // Finalizar análisis Tetrazolio - cambia estado según rol del usuario
    public TetrazolioDTO finalizarAnalisis(Long id) {
        Optional<Tetrazolio> tetrazolioExistente = tetrazolioRepository.findById(id);
        
        if (tetrazolioExistente.isPresent()) {
            Tetrazolio tetrazolio = tetrazolioExistente.get();
            validarCompletitudRepeticiones(tetrazolio);
            
            // Usar el servicio común para finalizar el análisis
            analisisService.finalizarAnalisis(tetrazolio);
            
            // Guardar cambios
            Tetrazolio tetrazolioActualizado = tetrazolioRepository.save(tetrazolio);
            
            return mapearEntidadADTO(tetrazolioActualizado);
        } else {
            throw new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id);
        }
    }

    // Cambiar estado a APROBADO (con validaciones)
    public TetrazolioDTO aprobarAnalisis(Long id) {
        Optional<Tetrazolio> tetrazolioExistente = tetrazolioRepository.findById(id);
        
        if (tetrazolioExistente.isPresent()) {
            Tetrazolio tetrazolio = tetrazolioExistente.get();
            
            // Validar completitud específica del tetrazolio
            validarCompletitudRepeticiones(tetrazolio);
            
            // Usar el servicio común para aprobar el análisis
            analisisService.aprobarAnalisis(tetrazolio);
            
            // Guardar cambios
            Tetrazolio tetrazolioActualizado = tetrazolioRepository.save(tetrazolio);
            
            return mapearEntidadADTO(tetrazolioActualizado);
        } else {
            throw new RuntimeException("Análisis de tetrazolio no encontrado con ID: " + id);
        }
    }
}