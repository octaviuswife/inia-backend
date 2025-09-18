package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ContGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;

@Service
public class RepGermService {

    @Autowired
    private RepGermRepository repGermRepository;

    @Autowired
    private ContGermService contGermService;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear nueva repetición asociada a un conteo
    public RepGermDTO crearRepGerm(Long contGermId, RepGermRequestDTO solicitud) {
        try {
            System.out.println("Creando repetición para conteo ID: " + contGermId);
            
            // Validar que el conteo existe
            ContGerm contGerm = entityManager.find(ContGerm.class, contGermId);
            if (contGerm == null) {
                throw new RuntimeException("Conteo no encontrado con ID: " + contGermId);
            }
            
            // Crear la repetición
            RepGerm repGerm = mapearSolicitudAEntidad(solicitud, contGerm);
            RepGerm repGermGuardada = repGermRepository.save(repGerm);
            
            // Recalcular automáticamente los valores de INIA
            contGermService.calcularYActualizarValoresINIA(contGerm);
            
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
            ContGerm contGerm = repGerm.getContGerm(); // Guardar referencia antes de actualizar
            
            actualizarEntidadDesdeSolicitud(repGerm, solicitud);
            RepGerm repGermActualizada = repGermRepository.save(repGerm);
            
            // Recalcular automáticamente los valores de INIA
            contGermService.calcularYActualizarValoresINIA(contGerm);
            
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
            ContGerm contGerm = repGerm.getContGerm(); // Guardar referencia antes de eliminar
            
            repGermRepository.deleteById(id);
            
            // Recalcular automáticamente los valores de INIA después de eliminar
            contGermService.calcularYActualizarValoresINIA(contGerm);
            
            System.out.println("Repetición eliminada con ID: " + id);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Obtener todas las repeticiones de un conteo
    public List<RepGermDTO> obtenerRepeticionesPorConteo(Long contGermId) {
        List<RepGerm> repeticiones = repGermRepository.findByContGermId(contGermId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Contar repeticiones de un conteo
    public Long contarRepeticionesPorConteo(Long contGermId) {
        return repGermRepository.countByContGermId(contGermId);
    }

    // Mapear de RequestDTO a Entity
    private RepGerm mapearSolicitudAEntidad(RepGermRequestDTO solicitud, ContGerm contGerm) {
        RepGerm repGerm = new RepGerm();
        repGerm.setNumRep(solicitud.getNumRep());
        repGerm.setNormales(solicitud.getNormales());
        repGerm.setAnormales(solicitud.getAnormales());
        repGerm.setDuras(solicitud.getDuras());
        repGerm.setFrescas(solicitud.getFrescas());
        repGerm.setMuertas(solicitud.getMuertas());
        repGerm.setTotal(solicitud.getTotal());
        repGerm.setContGerm(contGerm);
        
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
        // El conteo asociado no se cambia en actualizaciones
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
        
        // Incluir ID del conteo asociado
        if (repGerm.getContGerm() != null) {
            dto.setContGermId(repGerm.getContGerm().getContGermID());
        }
        
        return dto;
    }
}