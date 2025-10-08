package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepTetrazolioViabilidad;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepTetrazolioViabilidadRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepTetrazolioViabilidadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepTetrazolioViabilidadDTO;

@Service
public class RepTetrazolioViabilidadService {

    @Autowired
    private RepTetrazolioViabilidadRepository repeticionRepository;

    @Autowired
    private TetrazolioRepository tetrazolioRepository;

    // Crear nueva repetición asociada a un tetrazolio
    public RepTetrazolioViabilidadDTO crearRepeticion(Long tetrazolioId, RepTetrazolioViabilidadRequestDTO solicitud) {
        try {
            System.out.println("Creando repetición para tetrazolio ID: " + tetrazolioId);
            
            // Validar que el tetrazolio existe
            Optional<Tetrazolio> tetrazolioOpt = tetrazolioRepository.findById(tetrazolioId);
            if (tetrazolioOpt.isEmpty()) {
                throw new RuntimeException("Tetrazolio no encontrado con ID: " + tetrazolioId);
            }
            
            Tetrazolio tetrazolio = tetrazolioOpt.get();
            
            // Validar límite de repeticiones
            validarLimiteRepeticiones(tetrazolio);
            
            // Crear la repetición
            RepTetrazolioViabilidad repeticion = mapearSolicitudAEntidad(solicitud, tetrazolio);
            RepTetrazolioViabilidad repeticionGuardada = repeticionRepository.save(repeticion);
            
            System.out.println("Repetición creada exitosamente con ID: " + repeticionGuardada.getRepTetrazolioViabID());
            return mapearEntidadADTO(repeticionGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear repetición: " + e.getMessage());
            throw new RuntimeException("Error al crear la repetición: " + e.getMessage());
        }
    }

    // Obtener repetición por ID
    public RepTetrazolioViabilidadDTO obtenerRepeticionPorId(Long id) {
        Optional<RepTetrazolioViabilidad> repeticion = repeticionRepository.findById(id);
        if (repeticion.isPresent()) {
            return mapearEntidadADTO(repeticion.get());
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Actualizar repetición
    public RepTetrazolioViabilidadDTO actualizarRepeticion(Long id, RepTetrazolioViabilidadRequestDTO solicitud) {
        Optional<RepTetrazolioViabilidad> repeticionExistente = repeticionRepository.findById(id);
        
        if (repeticionExistente.isPresent()) {
            RepTetrazolioViabilidad repeticion = repeticionExistente.get();
            actualizarEntidadDesdeSolicitud(repeticion, solicitud);
            RepTetrazolioViabilidad repeticionActualizada = repeticionRepository.save(repeticion);
            return mapearEntidadADTO(repeticionActualizada);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Eliminar repetición (eliminar realmente, no cambio de estado)
    public void eliminarRepeticion(Long id) {
        Optional<RepTetrazolioViabilidad> repeticionExistente = repeticionRepository.findById(id);
        
        if (repeticionExistente.isPresent()) {
            repeticionRepository.deleteById(id);
            System.out.println("Repetición eliminada con ID: " + id);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Obtener todas las repeticiones de un tetrazolio
    public List<RepTetrazolioViabilidadDTO> obtenerRepeticionesPorTetrazolio(Long tetrazolioId) {
        List<RepTetrazolioViabilidad> repeticiones = repeticionRepository.findByTetrazolioId(tetrazolioId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Contar repeticiones de un tetrazolio
    public Long contarRepeticionesPorTetrazolio(Long tetrazolioId) {
        return repeticionRepository.countByTetrazolioId(tetrazolioId);
    }

    // Mapear de RequestDTO a Entity
    private RepTetrazolioViabilidad mapearSolicitudAEntidad(RepTetrazolioViabilidadRequestDTO solicitud, Tetrazolio tetrazolio) {
        RepTetrazolioViabilidad repeticion = new RepTetrazolioViabilidad();
        repeticion.setFecha(solicitud.getFecha());
        repeticion.setViablesNum(solicitud.getViablesNum());
        repeticion.setNoViablesNum(solicitud.getNoViablesNum());
        repeticion.setDuras(solicitud.getDuras());
        repeticion.setTetrazolio(tetrazolio);
        return repeticion;
    }

    // Actualizar Entity desde RequestDTO
    private void actualizarEntidadDesdeSolicitud(RepTetrazolioViabilidad repeticion, RepTetrazolioViabilidadRequestDTO solicitud) {
        repeticion.setFecha(solicitud.getFecha());
        repeticion.setViablesNum(solicitud.getViablesNum());
        repeticion.setNoViablesNum(solicitud.getNoViablesNum());
        repeticion.setDuras(solicitud.getDuras());
        // El tetrazolio asociado no se cambia en actualizaciones
    }

    // Mapear de Entity a DTO
    private RepTetrazolioViabilidadDTO mapearEntidadADTO(RepTetrazolioViabilidad repeticion) {
        RepTetrazolioViabilidadDTO dto = new RepTetrazolioViabilidadDTO();
        dto.setRepTetrazolioViabID(repeticion.getRepTetrazolioViabID());
        dto.setFecha(repeticion.getFecha());
        dto.setViablesNum(repeticion.getViablesNum());
        dto.setNoViablesNum(repeticion.getNoViablesNum());
        dto.setDuras(repeticion.getDuras());
        return dto;
    }
    
    // Validar que no se excedan las repeticiones esperadas
    private void validarLimiteRepeticiones(Tetrazolio tetrazolio) {
        if (tetrazolio.getNumRepeticionesEsperadas() != null && tetrazolio.getNumRepeticionesEsperadas() > 0) {
            Long repeticionesExistentes = repeticionRepository.countByTetrazolioId(tetrazolio.getAnalisisID());
            
            if (repeticionesExistentes >= tetrazolio.getNumRepeticionesEsperadas()) {
                throw new RuntimeException(
                    String.format("No se pueden crear más repeticiones. Ya se han creado %d de %d repeticiones esperadas para este análisis.", 
                                repeticionesExistentes, tetrazolio.getNumRepeticionesEsperadas()));
            }
        }
    }
}