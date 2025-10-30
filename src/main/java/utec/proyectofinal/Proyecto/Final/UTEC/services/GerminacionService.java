package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.specifications.GerminacionSpecification;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionEditRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoGerminacion;

@Service
public class GerminacionService {

    @Autowired
    private GerminacionRepository germinacionRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

    // Crear Germinación con estado REGISTRADO
    @Transactional
    public GerminacionDTO crearGerminacion(GerminacionRequestDTO solicitud) {
        try {
            System.out.println("Iniciando creación de germinación con solicitud: " + solicitud);
            
        Germinacion germinacion = mapearSolicitudAEntidad(solicitud);
        germinacion.setEstado(Estado.REGISTRADO);
        
        // Establecer fecha de inicio automáticamente
        analisisService.establecerFechaInicio(germinacion);
        
        Germinacion germinacionGuardada = germinacionRepository.save(germinacion);            // Registrar automáticamente en el historial
            analisisHistorialService.registrarCreacion(germinacionGuardada);
            
            System.out.println("Germinación creada exitosamente con ID: " + germinacionGuardada.getAnalisisID());
            
            return mapearEntidadADTO(germinacionGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear germinación: " + e.getMessage());
            throw new RuntimeException("Error al crear el análisis de germinación: " + e.getMessage());
        }
    }

    // Editar Germinación
    @Transactional
    public GerminacionDTO actualizarGerminacion(Long id, GerminacionRequestDTO solicitud) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            
            // Si el análisis está APROBADO y el usuario actual es ANALISTA, cambiar a PENDIENTE_APROBACION
            if (germinacion.getEstado() == Estado.APROBADO && analisisService.esAnalista()) {
                germinacion.setEstado(Estado.PENDIENTE_APROBACION);
                System.out.println("Análisis aprobado editado por analista - cambiando estado a PENDIENTE_APROBACION");
            }
            
            actualizarEntidadDesdeSolicitud(germinacion, solicitud);
            Germinacion germinacionActualizada = germinacionRepository.save(germinacion);
            
            // Registrar automáticamente en el historial
            analisisHistorialService.registrarModificacion(germinacionActualizada);
            
            return mapearEntidadADTO(germinacionActualizada);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    // Editar Germinación con DTO específico (sin fechas)
    @Transactional
    public GerminacionDTO actualizarGerminacionSeguro(Long id, GerminacionEditRequestDTO dto) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            
            // Manejar edición de análisis finalizado según el rol del usuario
            analisisService.manejarEdicionAnalisisFinalizado(germinacion);
            
            // Actualizar solo los campos permitidos del DTO de edición
            if (dto.getIdLote() != null) {
                Optional<Lote> loteOpt = loteRepository.findById(dto.getIdLote());
                if (loteOpt.isPresent()) {
                    germinacion.setLote(loteOpt.get());
                } else {
                    throw new RuntimeException("Lote no encontrado con ID: " + dto.getIdLote());
                }
            }
            if (dto.getComentarios() != null) {
                germinacion.setComentarios(dto.getComentarios());
            }
            // numDias NO es editable - se mantiene el valor original
            
            Germinacion germinacionActualizada = germinacionRepository.save(germinacion);
            
            // Registrar automáticamente en el historial
            analisisHistorialService.registrarModificacion(germinacionActualizada);
            
            return mapearEntidadADTO(germinacionActualizada);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    // Eliminar Germinación (desactivar - cambiar activo a false)
    public void eliminarGerminacion(Long id) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            germinacion.setActivo(false);
            germinacionRepository.save(germinacion);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    // Desactivar Germinacion (cambiar activo a false)
    public void desactivarGerminacion(Long id) {
        analisisService.desactivarAnalisis(id, germinacionRepository);
    }

    // Reactivar Germinacion (cambiar activo a true)
    public GerminacionDTO reactivarGerminacion(Long id) {
        return analisisService.reactivarAnalisis(id, germinacionRepository, this::mapearEntidadADTO);
    }

    // Listar todas las Germinaciones activas
    public ResponseListadoGerminacion obtenerTodasGerminaciones() {
        List<Germinacion> germinacionesActivas = germinacionRepository.findByActivoTrue();
        List<GerminacionDTO> germinacionesDTO = germinacionesActivas.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
        
        ResponseListadoGerminacion response = new ResponseListadoGerminacion();
        response.setGerminaciones(germinacionesDTO);
        return response;
    }

    // Listar germinaciones con paginado (para listado)
    public Page<GerminacionListadoDTO> obtenerGerminacionesPaginadas(Pageable pageable) {
        Page<Germinacion> germinacionesPage = germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
        return germinacionesPage.map(this::mapearEntidadAListadoDTO);
    }

    // Listar germinaciones con paginado y filtro de activo
    public Page<GerminacionListadoDTO> obtenerGerminacionesPaginadasConFiltro(Pageable pageable, String filtroActivo) {
        Page<Germinacion> germinacionesPage;
        
        switch (filtroActivo.toLowerCase()) {
            case "activos":
                germinacionesPage = germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
                break;
            case "inactivos":
                germinacionesPage = germinacionRepository.findByActivoFalseOrderByFechaInicioDesc(pageable);
                break;
            default: // "todos"
                germinacionesPage = germinacionRepository.findAllByOrderByFechaInicioDesc(pageable);
                break;
        }
        
        return germinacionesPage.map(this::mapearEntidadAListadoDTO);
    }

    // Listar germinaciones con paginado y filtros completos
    public Page<GerminacionListadoDTO> obtenerGerminacionesPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String estado,
            Long loteId) {
        Specification<Germinacion> spec = GerminacionSpecification.conFiltros(searchTerm, activo, estado, loteId);
        Page<Germinacion> germinacionesPage = germinacionRepository.findAll(spec, pageable);
        return germinacionesPage.map(this::mapearEntidadAListadoDTO);
    }

    // Obtener Germinación por ID
    public GerminacionDTO obtenerGerminacionPorId(Long id) {
        Optional<Germinacion> germinacion = germinacionRepository.findById(id);
        if (germinacion.isPresent()) {
            return mapearEntidadADTO(germinacion.get());
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    // Obtener Germinaciones por Lote
    public List<GerminacionDTO> obtenerGerminacionesPorIdLote(Long idLote) {
        List<Germinacion> germinaciones = germinacionRepository.findByIdLote(idLote);
        return germinaciones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Validar que todas las tablas asociadas estén finalizadas
    private boolean todasTablasFinalizadas(Germinacion germinacion) {
        if (germinacion.getTablaGerm() == null || germinacion.getTablaGerm().isEmpty()) {
            return false; // No hay tablas, no se puede finalizar
        }
        
        for (TablaGerm tabla : germinacion.getTablaGerm()) {
            if (tabla.getFinalizada() == null || !tabla.getFinalizada()) {
                return false; // Hay al menos una tabla no finalizada
            }
        }
        
        return true; // Todas las tablas están finalizadas
    }

    /**
     * Validación completa para operaciones críticas de Germinación (finalizar y marcar para repetir)
     * Verifica completitud de tablas
     */
    private void validarGerminacionParaOperacionCritica(Germinacion germinacion) {
        // Validación específica de Germinación: completitud de tablas
        if (!todasTablasFinalizadas(germinacion)) {
            throw new RuntimeException("No se puede completar la operación. Hay tablas pendientes de completar.");
        }
    }

    // Mapear de RequestDTO a Entity para creación
    private Germinacion mapearSolicitudAEntidad(GerminacionRequestDTO solicitud) {
        System.out.println("Mapeando solicitud a entidad germinación");
        
        Germinacion germinacion = new Germinacion();
        
        // Datos del análisis base (fechaInicio y fechaFin son automáticas, no del request)
        germinacion.setComentarios(solicitud.getComentarios());
        
        // Validar y establecer lote
        if (solicitud.getIdLote() != null) {
            System.out.println("Buscando lote con ID: " + solicitud.getIdLote());
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                Lote lote = loteOpt.get();
                germinacion.setLote(lote);
                System.out.println("Lote encontrado y asignado: " + lote.getLoteID());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }
        
        System.out.println("Germinación mapeada exitosamente");
        return germinacion;
    }

    // Actualizar Entity desde RequestDTO para edición
    private void actualizarEntidadDesdeSolicitud(Germinacion germinacion, GerminacionRequestDTO solicitud) {
        System.out.println("Actualizando germinación desde solicitud");
        
        // Datos del análisis base
        germinacion.setComentarios(solicitud.getComentarios());
        
        // Validar y establecer lote si se proporciona
        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                germinacion.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        System.out.println("Germinación actualizada exitosamente");
    }

    // Mapear de Entity a DTO
    private GerminacionDTO mapearEntidadADTO(Germinacion germinacion) {
        GerminacionDTO dto = new GerminacionDTO();
        
        // Datos del análisis base (fechaInicio y fechaFin automáticas del sistema)
        dto.setAnalisisID(germinacion.getAnalisisID());
        dto.setEstado(germinacion.getEstado());
        dto.setFechaInicio(germinacion.getFechaInicio());
        dto.setFechaFin(germinacion.getFechaFin());
        dto.setComentarios(germinacion.getComentarios());
        
        // Datos del lote si existe
        if (germinacion.getLote() != null) {
            dto.setIdLote(germinacion.getLote().getLoteID());
            dto.setLote(germinacion.getLote().getFicha());
        }
        
        // Mapear historial de análisis
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(germinacion.getAnalisisID()));
        
        return dto;
    }

    // Mapear de Entity a DTO simple para listado
    private GerminacionListadoDTO mapearEntidadAListadoDTO(Germinacion germinacion) {
        GerminacionListadoDTO dto = new GerminacionListadoDTO();
        
        // Datos básicos del análisis
        dto.setAnalisisID(germinacion.getAnalisisID());
        dto.setEstado(germinacion.getEstado());
        dto.setFechaInicio(germinacion.getFechaInicio());
        dto.setFechaFin(germinacion.getFechaFin());
        dto.setActivo(germinacion.getActivo());
        
        // Datos del lote
        if (germinacion.getLote() != null) {
            dto.setIdLote(germinacion.getLote().getLoteID());
            dto.setLote(germinacion.getLote().getNomLote()); // Usar nomLote en lugar de ficha
            
            // Obtener especie del lote
            if (germinacion.getLote().getCultivar() != null && germinacion.getLote().getCultivar().getEspecie() != null) {
                String nombreEspecie = germinacion.getLote().getCultivar().getEspecie().getNombreCientifico();
                dto.setEspecie(nombreEspecie);
            }
        }
        
        // Cumple norma: true si NO está "A REPETIR"
        dto.setCumpleNorma(germinacion.getEstado() != Estado.A_REPETIR);
        
        // Obtener información del historial para usuarios
        if (germinacion.getAnalisisID() != null) {
            var historial = analisisHistorialService.obtenerHistorialAnalisis(germinacion.getAnalisisID());
            if (!historial.isEmpty()) {
                // Usuario creador (primer registro)
                var primerRegistro = historial.get(historial.size() - 1);
                dto.setUsuarioCreador(primerRegistro.getUsuario());
                
                // Usuario modificador (último registro)
                var ultimoRegistro = historial.get(0);
                dto.setUsuarioModificador(ultimoRegistro.getUsuario());
            }
        }
        
        return dto;
    }

    /**
     * Finalizar análisis según el rol del usuario
     * - Analistas: pasa a PENDIENTE_APROBACION
     * - Administradores: pasa directamente a APROBADO
     */
    public GerminacionDTO finalizarAnalisis(Long id) {
        return analisisService.finalizarAnalisisGenerico(
            id,
            germinacionRepository,
            this::mapearEntidadADTO,
            this::validarGerminacionParaOperacionCritica
        );
    }

    /**
     * Aprobar análisis (solo administradores)
     */
    public GerminacionDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
            id,
            germinacionRepository,
            this::mapearEntidadADTO,
            this::validarGerminacionParaOperacionCritica, // Mismas validaciones que finalizar
            germinacionRepository::findByIdLote // Función para buscar por lote
        );
    }

    // Marcar análisis para repetir (solo administradores)
    public GerminacionDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
            id,
            germinacionRepository,
            this::mapearEntidadADTO,
            this::validarGerminacionParaOperacionCritica // Mismas validaciones que finalizar
        );
    }
}