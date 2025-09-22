package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Germinación con estado REGISTRADO
    public GerminacionDTO crearGerminacion(GerminacionRequestDTO solicitud) {
        try {
            System.out.println("Iniciando creación de germinación con solicitud: " + solicitud);
            
            Germinacion germinacion = mapearSolicitudAEntidad(solicitud);
            germinacion.setEstado(Estado.REGISTRADO);
            
            Germinacion germinacionGuardada = germinacionRepository.save(germinacion);
            
            // Registrar automáticamente en el historial
            analisisHistorialService.registrarCreacion(germinacionGuardada);
            
            System.out.println("Germinación creada exitosamente con ID: " + germinacionGuardada.getAnalisisID());
            
            return mapearEntidadADTO(germinacionGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear germinación: " + e.getMessage());
            throw new RuntimeException("Error al crear el análisis de germinación: " + e.getMessage());
        }
    }

    // Editar Germinación
    public GerminacionDTO actualizarGerminacion(Long id, GerminacionRequestDTO solicitud) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            
            // Si el análisis está APROBADO y el usuario actual es ANALISTA, cambiar a PENDIENTE_APROBACION
            if (germinacion.getEstado() == Estado.APROBADO && esAnalista()) {
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
    
    // Método para determinar si el usuario actual es analista
    private boolean esAnalista() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANALISTA"));
        }
        return false;
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
        
        // Datos del análisis base
        germinacion.setFechaInicio(solicitud.getFechaInicio());
        germinacion.setFechaFin(solicitud.getFechaFin());
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
        germinacion.setFechaInicio(solicitud.getFechaInicioGerminacion());
        germinacion.setFechaFin(solicitud.getFechaFinGerminacion());
        
        // Calcular numDias automáticamente
        if (solicitud.getFechaInicioGerminacion() != null && solicitud.getFechaFinGerminacion() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                solicitud.getFechaInicioGerminacion(), 
                solicitud.getFechaFinGerminacion()
            );
            germinacion.setNumDias(String.valueOf(dias));
        }
        
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
        germinacion.setFechaInicio(solicitud.getFechaInicio());
        germinacion.setFechaFin(solicitud.getFechaFin());
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
        
        // Datos específicos de Germinación
        germinacion.setFechaInicio(solicitud.getFechaInicioGerminacion());
        germinacion.setFechaFin(solicitud.getFechaFinGerminacion());
        
        // Calcular numDias automáticamente
        if (solicitud.getFechaInicioGerminacion() != null && solicitud.getFechaFinGerminacion() != null) {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(
                solicitud.getFechaInicioGerminacion(), 
                solicitud.getFechaFinGerminacion()
            );
            germinacion.setNumDias(String.valueOf(dias));
        }
        
        // Actualizar campos de control si se proporcionan
        if (solicitud.getNumeroRepeticiones() != null && solicitud.getNumeroRepeticiones() > 0) {
            germinacion.setNumeroRepeticiones(solicitud.getNumeroRepeticiones());
        }
        if (solicitud.getNumeroConteos() != null && solicitud.getNumeroConteos() > 0) {
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
        }
        
        System.out.println("Germinación actualizada exitosamente");
    }

    // Mapear de Entity a DTO
    private GerminacionDTO mapearEntidadADTO(Germinacion germinacion) {
        GerminacionDTO dto = new GerminacionDTO();
        
        // Datos del análisis base
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
        
        // Datos específicos de Germinación
        dto.setFechaInicio(germinacion.getFechaInicio());
        dto.setFechaConteos(germinacion.getFechaConteos());
        dto.setFechaFin(germinacion.getFechaFin());
        dto.setNumDias(germinacion.getNumDias());
        
        // Nuevos campos de control
        dto.setNumeroRepeticiones(germinacion.getNumeroRepeticiones());
        dto.setNumeroConteos(germinacion.getNumeroConteos());
        
        return dto;
    }

    /**
     * Finalizar análisis según el rol del usuario
     * - Analistas: pasa a PENDIENTE_APROBACION
     * - Administradores: pasa directamente a APROBADO
     */
    public GerminacionDTO finalizarAnalisis(Long id) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            
            // Validar que todas las tablas estén finalizadas
            if (!todasTablasFinalizadas(germinacion)) {
                throw new RuntimeException("No se puede finalizar el análisis. Hay tablas pendientes de completar.");
            }
            
            if (esAnalista()) {
                // Analista: enviar a pendiente de aprobación
                germinacion.setEstado(Estado.PENDIENTE_APROBACION);
                System.out.println("Análisis finalizado por analista - enviado a PENDIENTE_APROBACION");
            } else {
                // Admin: aprobar directamente
                germinacion.setEstado(Estado.APROBADO);
                System.out.println("Análisis finalizado por admin - estado APROBADO");
            }
            
            Germinacion germinacionActualizada = germinacionRepository.save(germinacion);
            
            // Registrar en el historial
            analisisHistorialService.registrarModificacion(germinacionActualizada);
            
            return mapearEntidadADTO(germinacionActualizada);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }

    /**
     * Aprobar análisis (solo administradores)
     */
    public GerminacionDTO aprobarAnalisis(Long id) {
        Optional<Germinacion> germinacionExistente = germinacionRepository.findById(id);
        
        if (germinacionExistente.isPresent()) {
            Germinacion germinacion = germinacionExistente.get();
            germinacion.setEstado(Estado.APROBADO);
            
            Germinacion germinacionActualizada = germinacionRepository.save(germinacion);
            
            // Registrar en el historial
            analisisHistorialService.registrarModificacion(germinacionActualizada);
            
            return mapearEntidadADTO(germinacionActualizada);
        } else {
            throw new RuntimeException("Análisis de germinación no encontrado con ID: " + id);
        }
    }
}