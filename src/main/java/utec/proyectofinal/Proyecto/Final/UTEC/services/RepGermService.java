package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;

@Service
public class RepGermService {

    @Autowired
    private RepGermRepository repGermRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear nueva repetición asociada a una tabla
    public RepGermDTO crearRepGerm(Long tablaGermId, RepGermRequestDTO solicitud) {
        try {
            System.out.println("Creando repetición para tabla ID: " + tablaGermId);
            
            // Validar que la tabla existe
            TablaGerm tablaGerm = entityManager.find(TablaGerm.class, tablaGermId);
            if (tablaGerm == null) {
                throw new RuntimeException("Tabla no encontrada con ID: " + tablaGermId);
            }
            
            // Crear la repetición
            RepGerm repGerm = mapearSolicitudAEntidad(solicitud, tablaGerm);
            RepGerm repGermGuardada = repGermRepository.save(repGerm);
            
            System.out.println("Repetición creada exitosamente con ID: " + repGermGuardada.getRepGermID());
            return mapearEntidadADTO(repGermGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear repetición: " + e.getMessage());
            throw new RuntimeException("Error al crear la repetición: " + e.getMessage());
        }
    }

    // Obtener repetición por ID
    public RepGermDTO obtenerRepGermPorId(Long id) {
        Optional<RepGerm> repGerm = repGermRepository.findById(id);
        if (repGerm.isPresent()) {
            return mapearEntidadADTO(repGerm.get());
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Actualizar repetición
    public RepGermDTO actualizarRepGerm(Long id, RepGermRequestDTO solicitud) {
        Optional<RepGerm> repGermExistente = repGermRepository.findById(id);
        
        if (repGermExistente.isPresent()) {
            RepGerm repGerm = repGermExistente.get();
            
            actualizarEntidadDesdeSolicitud(repGerm, solicitud);
            RepGerm repGermActualizada = repGermRepository.save(repGerm);
            
            return mapearEntidadADTO(repGermActualizada);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Eliminar repetición (eliminar realmente, no cambio de estado)
    public void eliminarRepGerm(Long id) {
        Optional<RepGerm> repGermExistente = repGermRepository.findById(id);
        
        if (repGermExistente.isPresent()) {
            RepGerm repGerm = repGermExistente.get();
            
            repGermRepository.deleteById(id);
            
            System.out.println("Repetición eliminada con ID: " + id);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Obtener todas las repeticiones de una tabla
    public List<RepGermDTO> obtenerRepeticionesPorTabla(Long tablaGermId) {
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGermId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Contar repeticiones de una tabla
    public Long contarRepeticionesPorTabla(Long tablaGermId) {
        return repGermRepository.countByTablaGermId(tablaGermId);
    }

    // Mapear de RequestDTO a Entity
    private RepGerm mapearSolicitudAEntidad(RepGermRequestDTO solicitud, TablaGerm tablaGerm) {
        RepGerm repGerm = new RepGerm();
        repGerm.setNumRep(solicitud.getNumRep());
        repGerm.setNormales(solicitud.getNormales());
        repGerm.setAnormales(solicitud.getAnormales());
        repGerm.setDuras(solicitud.getDuras());
        repGerm.setFrescas(solicitud.getFrescas());
        repGerm.setMuertas(solicitud.getMuertas());
        repGerm.setTotal(solicitud.getTotal());
        repGerm.setTablaGerm(tablaGerm);
        
        return repGerm;
    }

    // Actualizar Entity desde RequestDTO
    private void actualizarEntidadDesdeSolicitud(RepGerm repGerm, RepGermRequestDTO solicitud) {
        repGerm.setNumRep(solicitud.getNumRep());
        repGerm.setNormales(solicitud.getNormales());
        repGerm.setAnormales(solicitud.getAnormales());
        repGerm.setDuras(solicitud.getDuras());
        repGerm.setFrescas(solicitud.getFrescas());
        repGerm.setMuertas(solicitud.getMuertas());
        repGerm.setTotal(solicitud.getTotal());
        // La tabla asociada no se cambia en actualizaciones
    }

    // Mapear de Entity a DTO
    private RepGermDTO mapearEntidadADTO(RepGerm repGerm) {
        RepGermDTO dto = new RepGermDTO();
        dto.setRepGermID(repGerm.getRepGermID());
        dto.setNumRep(repGerm.getNumRep());
        dto.setNormales(repGerm.getNormales());
        dto.setAnormales(repGerm.getAnormales());
        dto.setDuras(repGerm.getDuras());
        dto.setFrescas(repGerm.getFrescas());
        dto.setMuertas(repGerm.getMuertas());
        dto.setTotal(repGerm.getTotal());
        
        // Incluir ID de la tabla asociada
        if (repGerm.getTablaGerm() != null) {
            dto.setTablaGermId(repGerm.getTablaGerm().getTablaGermID());
        }
        
        return dto;
    }
}