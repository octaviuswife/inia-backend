package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPms;

@Service
public class PmsService {

    @Autowired
    private PmsRepository pmsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Pms con estado REGISTRADO
    public PmsDTO crearPms(PmsRequestDTO solicitud) {
        Pms pms = mapearSolicitudAEntidad(solicitud);
        pms.setEstado(Estado.REGISTRADO);

        Pms pmsGuardado = pmsRepository.save(pms);
        return mapearEntidadADTO(pmsGuardado);
    }

    // Actualizar Pms existente
    public PmsDTO actualizarPms(Long id, PmsRequestDTO solicitud) {
        Optional<Pms> pmsExistente = pmsRepository.findById(id);
        if (pmsExistente.isPresent()) {
            Pms pms = pmsExistente.get();
            actualizarEntidadDesdeSolicitud(pms, solicitud);

            Pms pmsActualizado = pmsRepository.save(pms);
            return mapearEntidadADTO(pmsActualizado);
        }
        throw new RuntimeException("Pms no encontrado con id: " + id);
    }

    // Eliminar Pms (cambiar estado a INACTIVO)
    public void eliminarPms(Long id) {
        Optional<Pms> pmsExistente = pmsRepository.findById(id);
        if (pmsExistente.isPresent()) {
            Pms pms = pmsExistente.get();
            pms.setEstado(Estado.INACTIVO);
            pmsRepository.save(pms);
        } else {
            throw new RuntimeException("Pms no encontrado con id: " + id);
        }
    }

    // Listar todos los Pms activos
    public ResponseListadoPms obtenerTodosPmsActivos() {
        List<Pms> pmsList = pmsRepository.findByEstadoNot(Estado.INACTIVO);
        List<PmsDTO> pmsDTOs = pmsList.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());

        ResponseListadoPms respuesta = new ResponseListadoPms();
        respuesta.setPms(pmsDTOs);
        return respuesta;
    }

    // Obtener Pms por ID
    public PmsDTO obtenerPmsPorId(Long id) {
        Optional<Pms> pms = pmsRepository.findById(id);
        if (pms.isPresent()) {
            return mapearEntidadADTO(pms.get());
        }
        throw new RuntimeException("Pms no encontrado con id: " + id);
    }

    // ======================
    // Helpers de mapeo
    // ======================

    private Pms mapearSolicitudAEntidad(PmsRequestDTO solicitud) {
        Pms pms = new Pms();

        // Relación con Lote
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.getReference(Lote.class, solicitud.getIdLote());
            pms.setLote(lote);
        }

        // Campos de Analisis
        pms.setFechaInicio(solicitud.getFechaInicio());
        pms.setFechaFin(solicitud.getFechaFin());
        //pms.setPublicadoParcial(solicitud.getPublicadoParcial());
        pms.setCumpleEstandar(solicitud.getCumpleEstandar());
        pms.setComentarios(solicitud.getComentarios());

        // Campos específicos de PMS
        pms.setPromedio100g(solicitud.getPromedio100g());
        pms.setDesvioStd(solicitud.getDesvioStd());
        pms.setCoefVariacion(solicitud.getCoefVariacion());
        pms.setPmssinRedon(solicitud.getPmssinRedon());
        pms.setPmsconRedon(solicitud.getPmsconRedon());

        // Repeticiones
        if (solicitud.getRepPms() != null && !solicitud.getRepPms().isEmpty()) {
            List<RepPms> repeticiones = solicitud.getRepPms().stream()
                    .map(repReq -> crearRepPmsDesdeSolicitud(repReq, pms))
                    .collect(Collectors.toList());
            pms.setRepPms(repeticiones);
        }

        return pms;
    }

    private void actualizarEntidadDesdeSolicitud(Pms pms, PmsRequestDTO solicitud) {
        // Relación con Lote
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.getReference(Lote.class, solicitud.getIdLote());
            pms.setLote(lote);
        }

        // Campos de Analisis
        if (solicitud.getFechaInicio() != null) pms.setFechaInicio(solicitud.getFechaInicio());
        if (solicitud.getFechaFin() != null) pms.setFechaFin(solicitud.getFechaFin());
        //if (solicitud.getPublicadoParcial() != null) pms.setPublicadoParcial(solicitud.getPublicadoParcial());
        if (solicitud.getCumpleEstandar() != null) pms.setCumpleEstandar(solicitud.getCumpleEstandar());
        if (solicitud.getComentarios() != null) pms.setComentarios(solicitud.getComentarios());

        // Campos específicos de PMS
        if (solicitud.getPromedio100g() != null) pms.setPromedio100g(solicitud.getPromedio100g());
        if (solicitud.getDesvioStd() != null) pms.setDesvioStd(solicitud.getDesvioStd());
        if (solicitud.getCoefVariacion() != null) pms.setCoefVariacion(solicitud.getCoefVariacion());
        if (solicitud.getPmssinRedon() != null) pms.setPmssinRedon(solicitud.getPmssinRedon());
        if (solicitud.getPmsconRedon() != null) pms.setPmsconRedon(solicitud.getPmsconRedon());

        // Repeticiones
        if (solicitud.getRepPms() != null) {
            pms.getRepPms().clear();
            List<RepPms> nuevasReps = solicitud.getRepPms().stream()
                    .map(repReq -> crearRepPmsDesdeSolicitud(repReq, pms))
                    .collect(Collectors.toList());
            pms.setRepPms(nuevasReps);
        }
    }

    private PmsDTO mapearEntidadADTO(Pms pms) {
        PmsDTO dto = new PmsDTO();

        // Campos de Analisis
        dto.setAnalisisID(pms.getAnalisisID());
        dto.setLote(pms.getLote() != null ? pms.getLote().getFicha() : null);
        dto.setEstado(pms.getEstado());
        dto.setFechaInicio(pms.getFechaInicio());
        dto.setFechaFin(pms.getFechaFin());
        //dto.setPublicadoParcial(pms.getPublicadoParcial());
        dto.setCumpleEstandar(pms.getCumpleEstandar());
        dto.setComentarios(pms.getComentarios());

        // Campos específicos de PMS
        dto.setPromedio100g(pms.getPromedio100g());
        dto.setDesvioStd(pms.getDesvioStd());
        dto.setCoefVariacion(pms.getCoefVariacion());
        dto.setPmssinRedon(pms.getPmssinRedon());
        dto.setPmsconRedon(pms.getPmsconRedon());

        // Repeticiones
        if (pms.getRepPms() != null) {
            List<RepPmsDTO> repDTOs = pms.getRepPms().stream()
                    .map(this::mapearRepPmsADTO)
                    .collect(Collectors.toList());
            dto.setRepPms(repDTOs);
        }

        return dto;
    }

    private RepPms crearRepPmsDesdeSolicitud(RepPmsRequestDTO solicitud, Pms pms) {
        RepPms rep = new RepPms();
        rep.setNumRep(solicitud.getNumRep());
        rep.setPeso(solicitud.getPeso());
        rep.setPms(pms);
        return rep;
    }

    private RepPmsDTO mapearRepPmsADTO(RepPms rep) {
        RepPmsDTO dto = new RepPmsDTO();
        dto.setRepPMSID(rep.getRepPMSID());
        dto.setNumRep(rep.getNumRep());
        dto.setPeso(rep.getPeso());
        return dto;
    }
}
