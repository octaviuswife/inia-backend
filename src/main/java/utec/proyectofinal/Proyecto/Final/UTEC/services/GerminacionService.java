package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoGerminacion;

@Service
public class GerminacionService {

    @Autowired
    private GerminacionRepository germinacionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Germinación con estado REGISTRADO
    public GerminacionDTO crearGerminacion(GerminacionRequestDTO solicitud) {
        try {
            System.out.println("Iniciando creación de germinación con solicitud: " + solicitud);
            
            Germinacion germinacion = mapearSolicitudAEntidad(solicitud);
            germinacion.setEstado(Estado.REGISTRADO);
            
            Germinacion germinacionGuardada = germinacionRepository.save(germinacion);
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
            actualizarEntidadDesdeSolicitud(germinacion, solicitud);
            Germinacion germinacionActualizada = germinacionRepository.save(germinacion);
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
        germinacion.setFechaConteos(solicitud.getFechaConteos());
        germinacion.setFechaFin(solicitud.getFechaFinGerminacion());
        germinacion.setTratamiento(solicitud.getTratamiento());
        germinacion.setProductoYDosis(solicitud.getProductoYDosis());
        germinacion.setNumSemillasPRep(solicitud.getNumSemillasPRep());
        germinacion.setMetodo(solicitud.getMetodo());
        germinacion.setTemperatura(solicitud.getTemperatura());
        germinacion.setPrefrio(solicitud.getPrefrio());
        germinacion.setPretratamiento(solicitud.getPretratamiento());
        germinacion.setNumDias(solicitud.getNumDias());
        
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
        germinacion.setFechaConteos(solicitud.getFechaConteos());
        germinacion.setFechaFin(solicitud.getFechaFinGerminacion());
        germinacion.setTratamiento(solicitud.getTratamiento());
        germinacion.setProductoYDosis(solicitud.getProductoYDosis());
        germinacion.setNumSemillasPRep(solicitud.getNumSemillasPRep());
        germinacion.setMetodo(solicitud.getMetodo());
        germinacion.setTemperatura(solicitud.getTemperatura());
        germinacion.setPrefrio(solicitud.getPrefrio());
        germinacion.setPretratamiento(solicitud.getPretratamiento());
        germinacion.setNumDias(solicitud.getNumDias());
        
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
        dto.setTratamiento(germinacion.getTratamiento());
        dto.setProductoYDosis(germinacion.getProductoYDosis());
        dto.setNumSemillasPRep(germinacion.getNumSemillasPRep());
        dto.setMetodo(germinacion.getMetodo());
        dto.setTemperatura(germinacion.getTemperatura());
        dto.setPrefrio(germinacion.getPrefrio());
        dto.setPretratamiento(germinacion.getPretratamiento());
        dto.setNumDias(germinacion.getNumDias());
        
        return dto;
    }
}