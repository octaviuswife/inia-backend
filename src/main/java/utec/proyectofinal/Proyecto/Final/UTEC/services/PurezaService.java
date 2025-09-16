package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ListadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
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
        
        Pureza purezaGuardada = purezaRepository.save(pureza);
        return mapearEntidadADTO(purezaGuardada);
    }

    // Editar Pureza
    public PurezaDTO actualizarPureza(Long id, PurezaRequestDTO solicitud) {
        Optional<Pureza> purezaExistente = purezaRepository.findById(id);
        if (purezaExistente.isPresent()) {
            Pureza pureza = purezaExistente.get();
            actualizarEntidadDesdeSolicitud(pureza, solicitud);
            
            Pureza purezaActualizada = purezaRepository.save(pureza);
            return mapearEntidadADTO(purezaActualizada);
        }
        throw new RuntimeException("Pureza no encontrada con id: " + id);
    }

    // Eliminar Pureza (cambiar estado a INACTIVO)
    public void eliminarPureza(Long id) {
        Optional<Pureza> purezaExistente = purezaRepository.findById(id);
        if (purezaExistente.isPresent()) {
            Pureza pureza = purezaExistente.get();
            pureza.setEstado(Estado.INACTIVO);
            purezaRepository.save(pureza);
        } else {
            throw new RuntimeException("Pureza no encontrada con id: " + id);
        }
    }

    // Listar todas las Purezas activas usando ResponseListadoPureza
    public ResponseListadoPureza obtenerTodasPurezasActivas() {
        List<Pureza> purezas = purezaRepository.findByEstadoNot(Estado.INACTIVO);
        List<PurezaDTO> purezaDTOs = purezas.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
        
        ResponseListadoPureza respuesta = new ResponseListadoPureza();
        respuesta.setPurezas(purezaDTOs);
        return respuesta;
    }

    // Obtener Pureza por ID
    public PurezaDTO obtenerPurezaPorId(Long id) {
        Optional<Pureza> pureza = purezaRepository.findById(id);
        if (pureza.isPresent()) {
            return mapearEntidadADTO(pureza.get());
        }
        throw new RuntimeException("Pureza no encontrada con id: " + id);
    }

    // Obtener Purezas por Lote
    public List<PurezaDTO> obtenerPurezasPorIdLote(Integer idLote) {
        List<Pureza> purezas = purezaRepository.findByIdLote(idLote);
        return purezas.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener todos los catálogos para el select
    public List<CatalogoDTO> obtenerTodosCatalogos() {
        List<Catalogo> catalogos = catalogoRepository.findAll();
        return catalogos.stream()
                .map(this::mapearCatalogoADTO)
                .collect(Collectors.toList());
    }

    // Mapear de RequestDTO a Entity para creación
    private Pureza mapearSolicitudAEntidad(PurezaRequestDTO solicitud) {
        Pureza pureza = new Pureza();
        
        // Mapear campos de Analisis
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.getReference(Lote.class, solicitud.getIdLote());
            pureza.setLote(lote);
        }
        pureza.setFechaInicio(solicitud.getFechaInicio());
        pureza.setFechaFin(solicitud.getFechaFin());
        pureza.setPublicadoParcial(solicitud.getPublicadoParcial());
        pureza.setCumpleEstandar(solicitud.getCumpleEstandar());
        pureza.setComentarios(solicitud.getComentarios());

        // Mapear campos específicos de Pureza
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

        // Mapear otras semillas
        if (solicitud.getOtrasSemillas() != null && !solicitud.getOtrasSemillas().isEmpty()) {
            List<Listado> otrasSemillas = solicitud.getOtrasSemillas().stream()
                    .map(solicitudListado -> crearListadoDesdeSolicitud(solicitudListado, pureza))
                    .collect(Collectors.toList());
            pureza.setListados(otrasSemillas);
        }

        return pureza;
    }

    // Actualizar Entity desde RequestDTO para edición
    private void actualizarEntidadDesdeSolicitud(Pureza pureza, PurezaRequestDTO solicitud) {
        // Actualizar campos de Analisis
        if (solicitud.getIdLote() != null) {
            Lote lote = entityManager.getReference(Lote.class, solicitud.getIdLote());
            pureza.setLote(lote);
        }
        if (solicitud.getFechaInicio() != null) pureza.setFechaInicio(solicitud.getFechaInicio());
        if (solicitud.getFechaFin() != null) pureza.setFechaFin(solicitud.getFechaFin());
        if (solicitud.getPublicadoParcial() != null) pureza.setPublicadoParcial(solicitud.getPublicadoParcial());
        if (solicitud.getCumpleEstandar() != null) pureza.setCumpleEstandar(solicitud.getCumpleEstandar());
        if (solicitud.getComentarios() != null) pureza.setComentarios(solicitud.getComentarios());

        // Actualizar campos específicos de Pureza
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

        // Actualizar otras semillas
        if (solicitud.getOtrasSemillas() != null) {
            // Primero eliminar los listados existentes asociados a esta pureza
            List<Listado> listadosExistentes = listadoRepository.findByPurezaAnalisisID(pureza.getAnalisisID());
            listadoRepository.deleteAll(listadosExistentes);
            
            // Crear nuevos listados
            List<Listado> nuevosListados = solicitud.getOtrasSemillas().stream()
                    .map(solicitudListado -> crearListadoDesdeSolicitud(solicitudListado, pureza))
                    .collect(Collectors.toList());
            pureza.setListados(nuevosListados);
        }
    }

    // Mapear de Entity a DTO siguiendo el patrón del ejemplo
    private PurezaDTO mapearEntidadADTO(Pureza pureza) {
        PurezaDTO dto = new PurezaDTO();
        
        // Mapear campos de Analisis
        dto.setAnalisisID(pureza.getAnalisisID());
        dto.setLote(pureza.getLote() != null ? pureza.getLote().getFicha() : null);
        dto.setEstado(pureza.getEstado());
        dto.setFechaInicio(pureza.getFechaInicio());
        dto.setFechaFin(pureza.getFechaFin());
        dto.setPublicadoParcial(pureza.getPublicadoParcial());
        dto.setCumpleEstandar(pureza.getCumpleEstandar());
        dto.setComentarios(pureza.getComentarios());

        // Mapear campos específicos de Pureza
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

        // Mapear otras semillas
        if (pureza.getListados() != null) {
            List<ListadoDTO> otrasSemillasDTO = pureza.getListados().stream()
                    .map(this::mapearListadoADTO)
                    .collect(Collectors.toList());
            dto.setOtrasSemillas(otrasSemillasDTO);
        }

        return dto;
    }

    // Helper para mapear Listado a ListadoDTO
    private ListadoDTO mapearListadoADTO(Listado listado) {
        ListadoDTO dto = new ListadoDTO();
        dto.setListadoID(listado.getListadoID());
        dto.setListadoTipo(listado.getListadoTipo());
        dto.setListadoInsti(listado.getListadoInsti());
        dto.setListadoNum(listado.getListadoNum());
        
        if (listado.getCatalogo() != null) {
            dto.setCatalogo(mapearCatalogoADTO(listado.getCatalogo()));
        }
        
        return dto;
    }

    // Helper para crear Listado desde ListadoRequestDTO
    private Listado crearListadoDesdeSolicitud(ListadoRequestDTO solicitud, Pureza pureza) {
        Listado listado = new Listado();
        listado.setListadoTipo(solicitud.getListadoTipo());
        listado.setListadoInsti(solicitud.getListadoInsti());
        listado.setListadoNum(solicitud.getListadoNum());
        listado.setPureza(pureza);
        
        if (solicitud.getIdCatalogo() != null) {
            Catalogo catalogo = entityManager.getReference(Catalogo.class, solicitud.getIdCatalogo());
            listado.setCatalogo(catalogo);
        }
        
        return listado;
    }

    // Helper para mapear Catalogo a CatalogoDTO
    private CatalogoDTO mapearCatalogoADTO(Catalogo catalogo) {
        CatalogoDTO dto = new CatalogoDTO();
        dto.setCatalogoID(catalogo.getCatalogoID());
        dto.setNombreComun(catalogo.getNombreComun());
        dto.setNombreCientifico(catalogo.getNombreCientifico());
        dto.setMaleza(catalogo.getMaleza());
        return dto;
    }
}
