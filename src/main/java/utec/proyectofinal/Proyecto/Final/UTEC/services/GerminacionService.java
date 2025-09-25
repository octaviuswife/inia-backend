package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoGerminacion;

@Service
public class GerminacionService {

    @Autowired
    private GerminacionRepository germinacionRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

    @PersistenceContext
    private EntityManager entityManager;

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

    // Eliminar Germinación (cambiar estado a INACTIVO)
    public void eliminarGerminacion(Long id) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            germinacion.setEstado(Estado.INACTIVO);
            germinacionRepository.save(germinacion);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    // Listar todas las Germinaciones activas
    public ResponseListadoGerminacion obtenerTodasGerminaciones() {
        List<Germinacion> germinacionesActivas = germinacionRepository.findByEstadoNot(Estado.INACTIVO);
        List<GerminacionDTO> germinacionesDTO = germinacionesActivas.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
        
        ResponseListadoGerminacion response = new ResponseListadoGerminacion();
        response.setGerminaciones(germinacionesDTO);
        return response;
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

    // Mapear de RequestDTO a Entity para creación
    private Germinacion mapearSolicitudAEntidad(GerminacionRequestDTO solicitud) {
        System.out.println("Mapeando solicitud a entidad germinación");
        
        Germinacion germinacion = new Germinacion();
        
        // Datos del análisis base (fechaInicio y fechaFin son automáticas, no del request)
        germinacion.setCumpleEstandar(solicitud.getCumpleEstandar());
        germinacion.setComentarios(solicitud.getComentarios());
        
        // Validar y establecer lote
        if (solicitud.getIdLote() != null) {
            System.out.println("Buscando lote con ID: " + solicitud.getIdLote());
            Lote lote = entityManager.find(Lote.class, solicitud.getIdLote());
            if (lote != null) {
                germinacion.setLote(lote);
                System.out.println("Lote encontrado y asignado: " + lote.getLoteID());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }
        
        // Datos específicos de Germinación
        germinacion.setFechaInicioGerm(solicitud.getFechaInicioGerm());
        germinacion.setFechaUltConteo(solicitud.getFechaUltConteo());
        germinacion.setNumDias(solicitud.getNumDias()); // Se recibe desde el frontend
        
        // Nuevos campos de control con validación
        if (solicitud.getNumeroRepeticiones() == null || solicitud.getNumeroRepeticiones() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones (mayor a 0).");
        }
        if (solicitud.getNumeroConteos() == null || solicitud.getNumeroConteos() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de conteos (mayor a 0).");
        }
        
        germinacion.setNumeroRepeticiones(solicitud.getNumeroRepeticiones());
        germinacion.setNumeroConteos(solicitud.getNumeroConteos());
        
        // Solo guardar fechaConteos no-null (las ingresadas por el usuario)
        List<java.time.LocalDate> fechaConteos = new ArrayList<>();
        if (solicitud.getFechaConteos() != null) {
            // Solo agregar fechas que no sean null
            for (LocalDate fecha : solicitud.getFechaConteos()) {
                if (fecha != null) {
                    fechaConteos.add(fecha);
                }
            }
        }
        
        germinacion.setFechaConteos(fechaConteos);
        
        System.out.println("Germinación mapeada exitosamente");
        return germinacion;
    }

    // Actualizar Entity desde RequestDTO para edición
    private void actualizarEntidadDesdeSolicitud(Germinacion germinacion, GerminacionRequestDTO solicitud) {
        System.out.println("Actualizando germinación desde solicitud");
        
        // Datos del análisis base

        germinacion.setCumpleEstandar(solicitud.getCumpleEstandar());
        germinacion.setComentarios(solicitud.getComentarios());
        
        // Validar y establecer lote si se proporciona
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.find(Lote.class, solicitud.getIdLote());
            if (lote != null) {
                germinacion.setLote(lote);
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        germinacion.setNumDias(solicitud.getNumDias()); // Se recibe desde el frontend
        
        // Actualizar campos de control si se proporcionan
        // NO se pueden editar una vez creado el análisis (como en Tetrazolio)
        // Los campos numeroRepeticiones y numeroConteos son de solo lectura en edición
        // actucalizar las fechas de conteo

        List<LocalDate> fechaConteos = new ArrayList<>();

        if (solicitud.getFechaConteos() != null) {
            // Solo agregar fechas que no sean null
            for (LocalDate fecha : solicitud.getFechaConteos()) {
                if (fecha != null) {
                    fechaConteos.add(fecha);
                }
            }
        }
        germinacion.setFechaConteos(fechaConteos);

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
        dto.setCumpleEstandar(germinacion.getCumpleEstandar());
        dto.setComentarios(germinacion.getComentarios());
        
        // Datos del lote si existe
        if (germinacion.getLote() != null) {
            dto.setLote(germinacion.getLote().getFicha());
        }
        
        // Datos específicos de Germinación con campos propios
        dto.setFechaInicioGerm(germinacion.getFechaInicioGerm());
        dto.setFechaUltConteo(germinacion.getFechaUltConteo());
        dto.setFechaConteos(germinacion.getFechaConteos());
        dto.setNumDias(germinacion.getNumDias());
        
        // Nuevos campos de control
        dto.setNumeroRepeticiones(germinacion.getNumeroRepeticiones());
        dto.setNumeroConteos(germinacion.getNumeroConteos());
        
        // Mapear historial de análisis
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(germinacion.getAnalisisID()));
        
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
            germinacion -> {
                // Validación específica de Germinación: completitud de tablas
                if (!todasTablasFinalizadas(germinacion)) {
                    throw new RuntimeException("No se puede finalizar el análisis. Hay tablas pendientes de completar.");
                }
            }
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
            null // No hay validación específica para aprobar
        );
    }
}