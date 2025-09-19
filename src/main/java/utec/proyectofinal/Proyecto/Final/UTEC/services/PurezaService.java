package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ListadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasYCultivosCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.mappers.MappingUtils;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPureza;

@Service
public class PurezaService {

    @Autowired
    private PurezaRepository purezaRepository;

    @Autowired
    private ListadoRepository listadoRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Pureza con estado REGISTRADO
    public PurezaDTO crearPureza(PurezaRequestDTO solicitud) {
        Pureza pureza = mapearSolicitudAEntidad(solicitud);
        pureza.setEstado(Estado.REGISTRADO);
        return mapearEntidadADTO(purezaRepository.save(pureza));
    }

    // Editar Pureza
    public PurezaDTO actualizarPureza(Long id, PurezaRequestDTO solicitud) {
        Pureza pureza = purezaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));

        actualizarEntidadDesdeSolicitud(pureza, solicitud);
        return mapearEntidadADTO(purezaRepository.save(pureza));
    }

    // Eliminar Pureza (cambiar estado a INACTIVO)
    public void eliminarPureza(Long id) {
        Pureza pureza = purezaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));

        pureza.setEstado(Estado.INACTIVO);
        purezaRepository.save(pureza);
    }

    // Listar todas las Purezas activas
    public ResponseListadoPureza obtenerTodasPurezasActivas() {
        List<PurezaDTO> purezaDTOs = purezaRepository.findByEstadoNot(Estado.INACTIVO)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());

        ResponseListadoPureza respuesta = new ResponseListadoPureza();
        respuesta.setPurezas(purezaDTOs);
        return respuesta;
    }

    // Obtener Pureza por ID
    public PurezaDTO obtenerPurezaPorId(Long id) {
        return purezaRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));
    }

    // Obtener Purezas por Lote
    public List<PurezaDTO> obtenerPurezasPorIdLote(Long idLote) {
        return purezaRepository.findByIdLote(idLote)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener todos los catálogos
    public List<MalezasYCultivosCatalogoDTO> obtenerTodosCatalogos() {
        return catalogoRepository.findAll()
                .stream()
                .map(MappingUtils::toCatalogoDTO)
                .collect(Collectors.toList());
    }

    // === Mappers internos ===

    private Pureza mapearSolicitudAEntidad(PurezaRequestDTO solicitud) {
        Pureza pureza = new Pureza();

        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.getReference(Lote.class, solicitud.getIdLote());
            pureza.setLote(lote);
        }

        pureza.setFechaInicio(solicitud.getFechaInicio());
        pureza.setFechaFin(solicitud.getFechaFin());
        pureza.setCumpleEstandar(solicitud.getCumpleEstandar());
        pureza.setComentarios(solicitud.getComentarios());

        // Campos específicos
        pureza.setFecha(solicitud.getFecha());
        pureza.setPesoInicial_g(solicitud.getPesoInicial_g());
        pureza.setSemillaPura_g(solicitud.getSemillaPura_g());
        pureza.setMateriaInerte_g(solicitud.getMateriaInerte_g());
        pureza.setOtrosCultivos_g(solicitud.getOtrosCultivos_g());
        pureza.setMalezas_g(solicitud.getMalezas_g());
        pureza.setMalezasToleradas_g(solicitud.getMalezasToleradas_g());
        pureza.setPesoTotal_g(solicitud.getPesoTotal_g());

        pureza.setRedonSemillaPura(solicitud.getRedonSemillaPura());
        pureza.setRedonMateriaInerte(solicitud.getRedonMateriaInerte());
        pureza.setRedonOtrosCultivos(solicitud.getRedonOtrosCultivos());
        pureza.setRedonMalezas(solicitud.getRedonMalezas());
        pureza.setRedonMalezasToleradas(solicitud.getRedonMalezasToleradas());
        pureza.setRedonPesoTotal(solicitud.getRedonPesoTotal());

        pureza.setInaseValor(solicitud.getInaseValor());
        pureza.setInaseFecha(solicitud.getInaseFecha());

        if (solicitud.getOtrasSemillas() != null && !solicitud.getOtrasSemillas().isEmpty()) {
            List<Listado> otrasSemillas = solicitud.getOtrasSemillas().stream()
                    .map(req -> crearListadoDesdeSolicitud(req, pureza))
                    .collect(Collectors.toList());
            pureza.setListados(otrasSemillas);
        }

        return pureza;
    }

    private void actualizarEntidadDesdeSolicitud(Pureza pureza, PurezaRequestDTO solicitud) {
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.find(Lote.class, solicitud.getIdLote());
            if (lote == null) {
                throw new RuntimeException("Lote no encontrado con id: " + solicitud.getIdLote());
            }
            pureza.setLote(lote);
        }

        if (solicitud.getFechaInicio() != null) pureza.setFechaInicio(solicitud.getFechaInicio());
        if (solicitud.getFechaFin() != null) pureza.setFechaFin(solicitud.getFechaFin());
        if (solicitud.getCumpleEstandar() != null) pureza.setCumpleEstandar(solicitud.getCumpleEstandar());
        if (solicitud.getComentarios() != null) pureza.setComentarios(solicitud.getComentarios());

        if (solicitud.getFecha() != null) pureza.setFecha(solicitud.getFecha());
        if (solicitud.getPesoInicial_g() != null) pureza.setPesoInicial_g(solicitud.getPesoInicial_g());
        if (solicitud.getSemillaPura_g() != null) pureza.setSemillaPura_g(solicitud.getSemillaPura_g());
        if (solicitud.getMateriaInerte_g() != null) pureza.setMateriaInerte_g(solicitud.getMateriaInerte_g());
        if (solicitud.getOtrosCultivos_g() != null) pureza.setOtrosCultivos_g(solicitud.getOtrosCultivos_g());
        if (solicitud.getMalezas_g() != null) pureza.setMalezas_g(solicitud.getMalezas_g());
        if (solicitud.getMalezasToleradas_g() != null) pureza.setMalezasToleradas_g(solicitud.getMalezasToleradas_g());
        if (solicitud.getPesoTotal_g() != null) pureza.setPesoTotal_g(solicitud.getPesoTotal_g());

        if (solicitud.getRedonSemillaPura() != null) pureza.setRedonSemillaPura(solicitud.getRedonSemillaPura());
        if (solicitud.getRedonMateriaInerte() != null) pureza.setRedonMateriaInerte(solicitud.getRedonMateriaInerte());
        if (solicitud.getRedonOtrosCultivos() != null) pureza.setRedonOtrosCultivos(solicitud.getRedonOtrosCultivos());
        if (solicitud.getRedonMalezas() != null) pureza.setRedonMalezas(solicitud.getRedonMalezas());
        if (solicitud.getRedonMalezasToleradas() != null) pureza.setRedonMalezasToleradas(solicitud.getRedonMalezasToleradas());
        if (solicitud.getRedonPesoTotal() != null) pureza.setRedonPesoTotal(solicitud.getRedonPesoTotal());

        if (solicitud.getInaseValor() != null) pureza.setInaseValor(solicitud.getInaseValor());
        if (solicitud.getInaseFecha() != null) pureza.setInaseFecha(solicitud.getInaseFecha());

        if (solicitud.getOtrasSemillas() != null) {
            listadoRepository.deleteAll(pureza.getListados());

            List<Listado> nuevosListados = solicitud.getOtrasSemillas().stream()
                    .map(req -> crearListadoDesdeSolicitud(req, pureza))
                    .collect(Collectors.toList());
            pureza.setListados(nuevosListados);
        }
    }

    private PurezaDTO mapearEntidadADTO(Pureza pureza) {
        PurezaDTO dto = new PurezaDTO();

        dto.setAnalisisID(pureza.getAnalisisID());
        dto.setLote(pureza.getLote() != null ? pureza.getLote().getFicha() : null);
        dto.setEstado(pureza.getEstado());
        dto.setFechaInicio(pureza.getFechaInicio());
        dto.setFechaFin(pureza.getFechaFin());
        dto.setCumpleEstandar(pureza.getCumpleEstandar());
        dto.setComentarios(pureza.getComentarios());

        dto.setFecha(pureza.getFecha());
        dto.setPesoInicial_g(pureza.getPesoInicial_g());
        dto.setSemillaPura_g(pureza.getSemillaPura_g());
        dto.setMateriaInerte_g(pureza.getMateriaInerte_g());
        dto.setOtrosCultivos_g(pureza.getOtrosCultivos_g());
        dto.setMalezas_g(pureza.getMalezas_g());
        dto.setMalezasToleradas_g(pureza.getMalezasToleradas_g());
        dto.setPesoTotal_g(pureza.getPesoTotal_g());

        dto.setRedonSemillaPura(pureza.getRedonSemillaPura());
        dto.setRedonMateriaInerte(pureza.getRedonMateriaInerte());
        dto.setRedonOtrosCultivos(pureza.getRedonOtrosCultivos());
        dto.setRedonMalezas(pureza.getRedonMalezas());
        dto.setRedonMalezasToleradas(pureza.getRedonMalezasToleradas());
        dto.setRedonPesoTotal(pureza.getRedonPesoTotal());

        dto.setInaseValor(pureza.getInaseValor());
        dto.setInaseFecha(pureza.getInaseFecha());

        if (pureza.getListados() != null) {
            List<ListadoDTO> otrasSemillasDTO = pureza.getListados().stream()
                    .map(MappingUtils::toListadoDTO)
                    .collect(Collectors.toList());
            dto.setOtrasSemillas(otrasSemillasDTO);
        }

        return dto;
    }

    private Listado crearListadoDesdeSolicitud(ListadoRequestDTO solicitud, Pureza pureza) {
        Listado listado = MappingUtils.fromListadoRequest(solicitud, entityManager);
        listado.setPureza(pureza);
        return listado;
    }
}
