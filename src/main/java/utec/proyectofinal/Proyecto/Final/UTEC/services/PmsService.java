package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

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

        Pms guardado = pmsRepository.save(pms);
        return mapearEntidadADTO(guardado);
    }

    // Editar Pms
    public PmsDTO actualizarPms(Long id, PmsRequestDTO solicitud) {
        Optional<Pms> existente = pmsRepository.findById(id);

        if (existente.isPresent()) {
            Pms pms = existente.get();
            actualizarEntidadDesdeSolicitud(pms, solicitud);
            Pms actualizado = pmsRepository.save(pms);
            return mapearEntidadADTO(actualizado);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    // Eliminar Pms (cambia estado a INACTIVO)
    public void eliminarPms(Long id) {
        Optional<Pms> existente = pmsRepository.findById(id);

        if (existente.isPresent()) {
            Pms pms = existente.get();
            pms.setEstado(Estado.INACTIVO);
            pmsRepository.save(pms);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    // Listar todos los Pms activos
    public List<PmsDTO> obtenerTodos() {
        List<Pms> activos = pmsRepository.findAll()
                .stream()
                .filter(p -> p.getEstado() != Estado.INACTIVO)
                .collect(Collectors.toList());

        return activos.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener Pms por ID
    public PmsDTO obtenerPorId(Long id) {
        Optional<Pms> pms = pmsRepository.findById(id);
        if (pms.isPresent()) {
            return mapearEntidadADTO(pms.get());
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    public List<PmsDTO> obtenerPmsPorIdLote(Long idLote) {
        List<Pms> lista = entityManager
                .createQuery("SELECT p FROM Pms p WHERE p.lote.loteID = :idLote", Pms.class)
                .setParameter("idLote", idLote)
                .getResultList();

        return lista.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }



    // ==============================
    // Métodos auxiliares de mapeo
    // ==============================

    private Pms mapearSolicitudAEntidad(PmsRequestDTO solicitud) {
        Pms pms = new Pms();

        pms.setFechaInicio(solicitud.getFechaInicio());
        pms.setFechaFin(solicitud.getFechaFin());
        pms.setCumpleEstandar(solicitud.getCumpleEstandar());
        pms.setComentarios(solicitud.getComentarios());

        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.find(Lote.class, solicitud.getIdLote());
            if (lote != null) {
                pms.setLote(lote);
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        pms.setPromedio100g(solicitud.getPromedio100g());
        pms.setDesvioStd(solicitud.getDesvioStd());
        pms.setCoefVariacion(solicitud.getCoefVariacion());
        pms.setPmssinRedon(solicitud.getPmssinRedon());
        pms.setPmsconRedon(solicitud.getPmsconRedon());

        return pms;
    }

    private void actualizarEntidadDesdeSolicitud(Pms pms, PmsRequestDTO solicitud) {
        pms.setFechaInicio(solicitud.getFechaInicio());
        pms.setFechaFin(solicitud.getFechaFin());
        pms.setCumpleEstandar(solicitud.getCumpleEstandar());
        pms.setComentarios(solicitud.getComentarios());

        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.find(Lote.class, solicitud.getIdLote());
            if (lote != null) {
                pms.setLote(lote);
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        pms.setPromedio100g(solicitud.getPromedio100g());
        pms.setDesvioStd(solicitud.getDesvioStd());
        pms.setCoefVariacion(solicitud.getCoefVariacion());
        pms.setPmssinRedon(solicitud.getPmssinRedon());
        pms.setPmsconRedon(solicitud.getPmsconRedon());
    }

    private PmsDTO mapearEntidadADTO(Pms pms) {
        PmsDTO dto = new PmsDTO();

        dto.setAnalisisID(pms.getAnalisisID());
        dto.setEstado(pms.getEstado());
        dto.setFechaInicio(pms.getFechaInicio());
        dto.setFechaFin(pms.getFechaFin());
        dto.setCumpleEstandar(pms.getCumpleEstandar());
        dto.setComentarios(pms.getComentarios());

        if (pms.getLote() != null) {
            dto.setLote(pms.getLote().getFicha());
        }

        dto.setPromedio100g(pms.getPromedio100g());
        dto.setDesvioStd(pms.getDesvioStd());
        dto.setCoefVariacion(pms.getCoefVariacion());
        dto.setPmssinRedon(pms.getPmssinRedon());
        dto.setPmsconRedon(pms.getPmsconRedon());

        return dto;
    }
}
