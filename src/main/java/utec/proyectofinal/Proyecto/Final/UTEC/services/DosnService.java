package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ListadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;
import utec.proyectofinal.Proyecto.Final.UTEC.business.mappers.MappingUtils;

@Service
public class DosnService {

    @Autowired
    private DosnRepository dosnRepository;

    @Autowired
    private ListadoRepository listadoRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Dosn
    public DosnDTO crearDosn(DosnRequestDTO solicitud) {
        Dosn dosn = mapearSolicitudAEntidad(solicitud);
        dosn.setEstado(Estado.REGISTRADO);
        return mapearEntidadADTO(dosnRepository.save(dosn));
    }

    // Editar Dosn
    public DosnDTO actualizarDosn(Long id, DosnRequestDTO solicitud) {
        Dosn dosn = dosnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dosn no encontrada con id: " + id));

        actualizarEntidadDesdeSolicitud(dosn, solicitud);
        return mapearEntidadADTO(dosnRepository.save(dosn));
    }

    // Eliminar Dosn (estado INACTIVO)
    public void eliminarDosn(Long id) {
        Dosn dosn = dosnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dosn no encontrada con id: " + id));

        dosn.setEstado(Estado.INACTIVO);
        dosnRepository.save(dosn);
    }

    // Listar todas las Dosn activas
    public ResponseListadoDosn obtenerTodasDosnActivas() {
        List<DosnDTO> dosnDTOs = dosnRepository.findByEstadoNot(Estado.INACTIVO)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());

        ResponseListadoDosn respuesta = new ResponseListadoDosn();
        respuesta.setDosns(dosnDTOs);
        return respuesta;
    }

    // Obtener Dosn por ID
    public DosnDTO obtenerDosnPorId(Long id) {
        return dosnRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElseThrow(() -> new RuntimeException("Dosn no encontrada con id: " + id));
    }

    // Obtener Dosn por Lote
    public List<DosnDTO> obtenerDosnPorIdLote(Integer idLote) {
        return dosnRepository.findByIdLote(idLote)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // === Mappers ===

    private Dosn mapearSolicitudAEntidad(DosnRequestDTO solicitud) {
        Dosn dosn = new Dosn();

        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.getReference(Lote.class, solicitud.getIdLote());
            dosn.setLote(lote);
        }

        dosn.setFechaInicio(solicitud.getFechaInicio());
        dosn.setFechaFin(solicitud.getFechaFin());
        //dosn.setPublicadoParcial(solicitud.getPublicadoParcial());
        dosn.setCumpleEstandar(solicitud.getCumpleEstandar());
        dosn.setComentarios(solicitud.getComentarios());

        dosn.setFechaINIA(solicitud.getFechaINIA());
        dosn.setGramosAnalizadosINIA(solicitud.getGramosAnalizadosINIA());
        dosn.setTipoINIA(solicitud.getTipoINIA());

        dosn.setFechaINASE(solicitud.getFechaINASE());
        dosn.setGramosAnalizadosINASE(solicitud.getGramosAnalizadosINASE());
        dosn.setTipoINASE(solicitud.getTipoINASE());

        dosn.setCuscuta_g(solicitud.getCuscuta_g());
        dosn.setCuscutaNum(solicitud.getCuscutaNum());
        dosn.setFechaCuscuta(solicitud.getFechaCuscuta());

        if (solicitud.getListados() != null && !solicitud.getListados().isEmpty()) {
            List<Listado> listados = solicitud.getListados().stream()
                    .map(req -> crearListadoDesdeSolicitud(req, dosn))
                    .collect(Collectors.toList());
            dosn.setListados(listados);
        }

        return dosn;
    }

    private void actualizarEntidadDesdeSolicitud(Dosn dosn, DosnRequestDTO solicitud) {
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.getReference(Lote.class, solicitud.getIdLote());
            dosn.setLote(lote);
        }

        if (solicitud.getFechaInicio() != null) dosn.setFechaInicio(solicitud.getFechaInicio());
        if (solicitud.getFechaFin() != null) dosn.setFechaFin(solicitud.getFechaFin());
        //if (solicitud.getPublicadoParcial() != null) dosn.setPublicadoParcial(solicitud.getPublicadoParcial());
        if (solicitud.getCumpleEstandar() != null) dosn.setCumpleEstandar(solicitud.getCumpleEstandar());
        if (solicitud.getComentarios() != null) dosn.setComentarios(solicitud.getComentarios());

        if (solicitud.getFechaINIA() != null) dosn.setFechaINIA(solicitud.getFechaINIA());
        if (solicitud.getGramosAnalizadosINIA() != null) dosn.setGramosAnalizadosINIA(solicitud.getGramosAnalizadosINIA());
        if (solicitud.getTipoINIA() != null) dosn.setTipoINIA(solicitud.getTipoINIA());

        if (solicitud.getFechaINASE() != null) dosn.setFechaINASE(solicitud.getFechaINASE());
        if (solicitud.getGramosAnalizadosINASE() != null) dosn.setGramosAnalizadosINASE(solicitud.getGramosAnalizadosINASE());
        if (solicitud.getTipoINASE() != null) dosn.setTipoINASE(solicitud.getTipoINASE());

        if (solicitud.getCuscuta_g() != null) dosn.setCuscuta_g(solicitud.getCuscuta_g());
        if (solicitud.getCuscutaNum() != null) dosn.setCuscutaNum(solicitud.getCuscutaNum());
        if (solicitud.getFechaCuscuta() != null) dosn.setFechaCuscuta(solicitud.getFechaCuscuta());

        if (solicitud.getListados() != null) {
            listadoRepository.deleteAll(dosn.getListados());
            List<Listado> nuevosListados = solicitud.getListados().stream()
                    .map(req -> crearListadoDesdeSolicitud(req, dosn))
                    .collect(Collectors.toList());
            dosn.setListados(nuevosListados);
        }
    }

    private DosnDTO mapearEntidadADTO(Dosn dosn) {
        DosnDTO dto = new DosnDTO();

        dto.setAnalisisID(dosn.getAnalisisID());
        dto.setEstado(dosn.getEstado());
        dto.setFechaInicio(dosn.getFechaInicio());
        dto.setFechaFin(dosn.getFechaFin());
        //dto.setPublicadoParcial(dosn.getPublicadoParcial());
        dto.setCumpleEstandar(dosn.getCumpleEstandar());
        dto.setComentarios(dosn.getComentarios());
        dto.setLote(dosn.getLote() != null ? dosn.getLote().getFicha() : null);

        dto.setFechaINIA(dosn.getFechaINIA());
        dto.setGramosAnalizadosINIA(dosn.getGramosAnalizadosINIA());
        dto.setTipoINIA(dosn.getTipoINIA());

        dto.setFechaINASE(dosn.getFechaINASE());
        dto.setGramosAnalizadosINASE(dosn.getGramosAnalizadosINASE());
        dto.setTipoINASE(dosn.getTipoINASE());

        dto.setCuscuta_g(dosn.getCuscuta_g());
        dto.setCuscutaNum(dosn.getCuscutaNum());
        dto.setFechaCuscuta(dosn.getFechaCuscuta());

        if (dosn.getListados() != null) {
            List<ListadoDTO> listadoDTOs = dosn.getListados().stream()
                    .map(MappingUtils::toListadoDTO)
                    .collect(Collectors.toList());
            dto.setListados(listadoDTOs);
        }

        return dto;
    }

    private Listado crearListadoDesdeSolicitud(ListadoRequestDTO solicitud, Dosn dosn) {
        Listado listado = MappingUtils.fromListadoRequest(solicitud, entityManager);
        listado.setDosn(dosn);
        return listado;
    }
}
