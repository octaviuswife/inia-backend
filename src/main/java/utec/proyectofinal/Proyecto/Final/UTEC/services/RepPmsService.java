package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;

@Service
public class RepPmsService {

    @Autowired
    private RepPmsRepository repPmsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear nueva repetición asociada a un Pms
    public RepPmsDTO crearRepeticion(Long pmsId, RepPmsRequestDTO solicitud) {
        Pms pms = entityManager.find(Pms.class, pmsId);
        if (pms == null) {
            throw new RuntimeException("Pms no encontrado con ID: " + pmsId);
        }

        RepPms repeticion = mapearSolicitudAEntidad(solicitud, pms);
        RepPms guardada = repPmsRepository.save(repeticion);
        return mapearEntidadADTO(guardada);
    }

    // Obtener repetición por ID
    public RepPmsDTO obtenerPorId(Long id) {
        Optional<RepPms> repeticion = repPmsRepository.findById(id);
        if (repeticion.isPresent()) {
            return mapearEntidadADTO(repeticion.get());
        } else {
            throw new RuntimeException("Repetición PMS no encontrada con ID: " + id);
        }
    }

    // Actualizar repetición
    public RepPmsDTO actualizarRepeticion(Long id, RepPmsRequestDTO solicitud) {
        Optional<RepPms> existente = repPmsRepository.findById(id);

        if (existente.isPresent()) {
            RepPms rep = existente.get();
            actualizarEntidadDesdeSolicitud(rep, solicitud);
            RepPms actualizado = repPmsRepository.save(rep);
            return mapearEntidadADTO(actualizado);
        } else {
            throw new RuntimeException("Repetición PMS no encontrada con ID: " + id);
        }
    }

    // Eliminar repetición
    public void eliminarRepeticion(Long id) {
        Optional<RepPms> existente = repPmsRepository.findById(id);

        if (existente.isPresent()) {
            repPmsRepository.deleteById(id);
        } else {
            throw new RuntimeException("Repetición PMS no encontrada con ID: " + id);
        }
    }

    // Obtener todas las repeticiones de un Pms
    public List<RepPmsDTO> obtenerPorPms(Long pmsId) {
        List<RepPms> repeticiones = repPmsRepository.findByPmsId(pmsId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Contar repeticiones de un Pms
    public Long contarPorPms(Long pmsId) {
        return repPmsRepository.countByPmsId(pmsId);
    }

    // ==============================
    // Métodos auxiliares de mapeo
    // ==============================

    private RepPms mapearSolicitudAEntidad(RepPmsRequestDTO solicitud, Pms pms) {
        RepPms rep = new RepPms();
        rep.setNumRep(solicitud.getNumRep());
        rep.setPeso(solicitud.getPeso());
        rep.setPms(pms);
        return rep;
    }

    private void actualizarEntidadDesdeSolicitud(RepPms rep, RepPmsRequestDTO solicitud) {
        rep.setNumRep(solicitud.getNumRep());
        rep.setPeso(solicitud.getPeso());
    }

    private RepPmsDTO mapearEntidadADTO(RepPms rep) {
        RepPmsDTO dto = new RepPmsDTO();
        dto.setRepPMSID(rep.getRepPMSID());
        dto.setNumRep(rep.getNumRep());
        dto.setPeso(rep.getPeso());
        return dto;
    }
}
