package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
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
        List<Germinacion> germinacionesActivas = germinacionRepository.findByEstadoNot(Estado.INACTIVO);
        List<GerminacionDTO> germinacionesDTO = germinacionesActivas.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
        
        ResponseListadoGerminacion response = new ResponseListadoGerminacion();
        response.setGerminaciones(germinacionesDTO);
        return response;
    }

    // Listar germinaciones con paginado (para listado)
    public Page<GerminacionListadoDTO> obtenerGerminacionesPaginadas(Pageable pageable) {
        Page<Germinacion> germinacionesPage = germinacionRepository.findByEstadoNotOrderByFechaInicioDesc(Estado.INACTIVO, pageable);
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
        
        // Validaciones de fechas
        validarFechasGerminacion(solicitud);
        
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
        
        // Datos específicos de Germinación
        germinacion.setFechaInicioGerm(solicitud.getFechaInicioGerm());
        germinacion.setFechaUltConteo(solicitud.getFechaUltConteo());
        germinacion.setNumDias(solicitud.getNumDias()); // Se recibe desde el frontend
        
        

        // Validaciones de repeticiones y conteos
        validarParametrosGerminacion(solicitud);
        
        germinacion.setNumeroRepeticiones(solicitud.getNumeroRepeticiones());
        germinacion.setNumeroConteos(solicitud.getNumeroConteos());
        
        // Validar y establecer fechas de conteos
        validarFechasConteos(solicitud);
        
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
    
    /**
     * Validar fechas de germinación
     */
    private void validarFechasGerminacion(GerminacionRequestDTO solicitud) {
        if (solicitud.getFechaInicioGerm() == null) {
            throw new RuntimeException("La fecha de inicio de germinación es obligatoria");
        }
        
        if (solicitud.getFechaUltConteo() == null) {
            throw new RuntimeException("La fecha de último conteo es obligatoria");
        }
        
        // Validar que la fecha de último conteo sea posterior a la de inicio
        if (!solicitud.getFechaUltConteo().isAfter(solicitud.getFechaInicioGerm())) {
            throw new RuntimeException("La fecha de último conteo debe ser posterior a la fecha de inicio de germinación");
        }
        
        // Validar que haya al menos 1 día de diferencia
        long diasDiferencia = solicitud.getFechaInicioGerm().until(solicitud.getFechaUltConteo(), java.time.temporal.ChronoUnit.DAYS);
        if (diasDiferencia < 1) {
            throw new RuntimeException("Debe haber al menos 1 día de diferencia entre la fecha de inicio y la fecha de último conteo");
        }
    }
    
    /**
     * Validar parámetros de repeticiones y conteos
     */
    private void validarParametrosGerminacion(GerminacionRequestDTO solicitud) {
        //Nuevos campos de control con validación
        if (solicitud.getNumeroRepeticiones() == null ) {
            throw new RuntimeException("El número de repeticiones es obligatorio");
        }

        if (solicitud.getNumeroConteos() == null ) {
            throw new RuntimeException("El número de conteos es obligatorio");
        }
        
        if (solicitud.getNumeroRepeticiones() == null || solicitud.getNumeroRepeticiones() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones (mayor a 0).");
        }
        
        if (solicitud.getNumeroConteos() == null || solicitud.getNumeroConteos() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de conteos (mayor a 0).");
        }
    }
    
    /**
     * Validar fechas de conteos
     */
    private void validarFechasConteos(GerminacionRequestDTO solicitud) {
        if (solicitud.getFechaConteos() != null && !solicitud.getFechaConteos().isEmpty()) {
            LocalDate fechaInicio = solicitud.getFechaInicioGerm();
            LocalDate fechaFin = solicitud.getFechaUltConteo();
            
            for (int i = 0; i < solicitud.getFechaConteos().size(); i++) {
                LocalDate fechaConteo = solicitud.getFechaConteos().get(i);
                if (fechaConteo != null) {
                    // Validar que esté dentro del rango permitido
                    if (fechaConteo.isBefore(fechaInicio) || fechaConteo.isAfter(fechaFin)) {
                        throw new RuntimeException("La fecha de conteo " + (i + 1) + " debe estar entre la fecha de inicio y la fecha de último conteo");
                    }
                }
            }
            
            // Validar que el número de fechas coincida con el número de conteos
            if (solicitud.getFechaConteos().size() != solicitud.getNumeroConteos()) {
                throw new RuntimeException("El número de fechas de conteos debe coincidir con el número de conteos definido");
            }
        }
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

        germinacion.setNumDias(solicitud.getNumDias()); // Se recibe desde el frontend
        
        // Actualizar campos de control si se proporcionan
        // NO se pueden editar una vez creado el análisis (como en Tetrazolio)
        // Los campos numeroRepeticiones y numeroConteos son de solo lectura en edición
        
        // NO PERMITIR edición de fechas una vez creado el análisis
        // Las fechas son inmutables después de la creación para mantener integridad de datos
        if (solicitud.getFechaConteos() != null && !solicitud.getFechaConteos().isEmpty()) {
            // Verificar si las fechas son diferentes a las existentes
            List<LocalDate> fechasExistentes = germinacion.getFechaConteos();
            List<LocalDate> fechasNuevas = solicitud.getFechaConteos().stream()
                .filter(fecha -> fecha != null)
                .collect(java.util.stream.Collectors.toList());
            
            // Si las fechas son diferentes, rechazar la actualización
            if (!fechasExistentes.equals(fechasNuevas)) {
                throw new RuntimeException("No se pueden modificar las fechas de conteo una vez creado el análisis de germinación");
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

    // Mapear de Entity a DTO simple para listado
    private GerminacionListadoDTO mapearEntidadAListadoDTO(Germinacion germinacion) {
        GerminacionListadoDTO dto = new GerminacionListadoDTO();
        
        // Datos básicos del análisis
        dto.setAnalisisID(germinacion.getAnalisisID());
        dto.setEstado(germinacion.getEstado());
        dto.setFechaInicio(germinacion.getFechaInicio());
        dto.setFechaFin(germinacion.getFechaFin());
        
        // Datos del lote
        if (germinacion.getLote() != null) {
            dto.setIdLote(germinacion.getLote().getLoteID());
            dto.setLote(germinacion.getLote().getFicha());
        }
        
        // Datos específicos de germinación
        dto.setFechaInicioGerm(germinacion.getFechaInicioGerm());
        dto.setFechaUltConteo(germinacion.getFechaUltConteo());
        dto.setNumDias(germinacion.getNumDias());
        
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

    // Marcar análisis para repetir (solo administradores)
    public GerminacionDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
            id,
            germinacionRepository,
            this::mapearEntidadADTO,
            null // No hay validación específica para marcar a repetir
        );
    }
}