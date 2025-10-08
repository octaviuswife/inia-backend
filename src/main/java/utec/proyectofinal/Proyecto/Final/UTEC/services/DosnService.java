package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;   

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasYCultivosCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.mappers.MappingUtils;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ListadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasYCultivosCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;

@Service
public class DosnService {

    @Autowired
    private DosnRepository dosnRepository;

    @Autowired
    private ListadoRepository listadoRepository;

    @Autowired
    private CatalogoRepository catalogoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MalezasYCultivosCatalogoRepository malezasYCultivosCatalogoRepository;

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;

    // Crear Dosn
    @Transactional
    public DosnDTO crearDosn(DosnRequestDTO solicitud) {
        Dosn dosn = mapearSolicitudAEntidad(solicitud);
        dosn.setEstado(Estado.REGISTRADO);

        // Establecer fecha de inicio automáticamente
        analisisService.establecerFechaInicio(dosn);

        Dosn dosnGuardada = dosnRepository.save(dosn);

        // Registrar automáticamente en el historial
        analisisHistorialService.registrarCreacion(dosnGuardada);

        return mapearEntidadADTO(dosnGuardada);
    }

    // Editar Dosn
    @Transactional
    public DosnDTO actualizarDosn(Long id, DosnRequestDTO solicitud) {
        Dosn dosn = dosnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dosn no encontrada con id: " + id));

        // Manejar cambios de estado según rol del usuario
        Estado estadoOriginal = dosn.getEstado();

        if (estadoOriginal == Estado.APROBADO && analisisService.esAnalista()) {
            // Si es ANALISTA editando un análisis APROBADO, cambiar a PENDIENTE_APROBACION
            dosn.setEstado(Estado.PENDIENTE_APROBACION);
        }
        // Si es ADMIN editando análisis APROBADO, mantiene el estado APROBADO
        // Para otros estados se mantiene igual

        actualizarEntidadDesdeSolicitud(dosn, solicitud);

        // Guardar la entidad actualizada
        Dosn dosnActualizada = dosnRepository.save(dosn);

        // Registrar automáticamente en el historial
        analisisHistorialService.registrarModificacion(dosnActualizada);

        return mapearEntidadADTO(dosnActualizada);
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
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                dosn.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        dosn.setCumpleEstandar(solicitud.getCumpleEstandar());
        dosn.setComentarios(solicitud.getComentarios());

        // Las fechas fechaInicio y fechaFin son automáticas
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
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                dosn.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        if (solicitud.getCumpleEstandar() != null) dosn.setCumpleEstandar(solicitud.getCumpleEstandar());
        if (solicitud.getComentarios() != null) dosn.setComentarios(solicitud.getComentarios());

        if (solicitud.getFechaINIA() != null) dosn.setFechaINIA(solicitud.getFechaINIA());
        if (solicitud.getGramosAnalizadosINIA() != null)
            dosn.setGramosAnalizadosINIA(solicitud.getGramosAnalizadosINIA());
        if (solicitud.getTipoINIA() != null) dosn.setTipoINIA(solicitud.getTipoINIA());

        if (solicitud.getFechaINASE() != null) dosn.setFechaINASE(solicitud.getFechaINASE());
        if (solicitud.getGramosAnalizadosINASE() != null)
            dosn.setGramosAnalizadosINASE(solicitud.getGramosAnalizadosINASE());
        if (solicitud.getTipoINASE() != null) dosn.setTipoINASE(solicitud.getTipoINASE());

        if (solicitud.getCuscuta_g() != null) dosn.setCuscuta_g(solicitud.getCuscuta_g());
        if (solicitud.getCuscutaNum() != null) dosn.setCuscutaNum(solicitud.getCuscutaNum());
        if (solicitud.getFechaCuscuta() != null) dosn.setFechaCuscuta(solicitud.getFechaCuscuta());

        if (solicitud.getListados() != null) {
            // Inicializar la lista si es null
            if (dosn.getListados() == null) {
                dosn.setListados(new ArrayList<>());
            }

            // Limpiar listados existentes
            dosn.getListados().clear();

            // Si hay nuevos listados, crearlos y agregarlos
            if (!solicitud.getListados().isEmpty()) {
                List<Listado> nuevosListados = solicitud.getListados().stream()
                        .map(req -> crearListadoDesdeSolicitud(req, dosn))
                        .collect(Collectors.toList());

                dosn.getListados().addAll(nuevosListados);
            }
        }
    }

    private DosnDTO mapearEntidadADTO(Dosn dosn) {
        DosnDTO dto = new DosnDTO();

        dto.setAnalisisID(dosn.getAnalisisID());
        dto.setEstado(dosn.getEstado());
        dto.setFechaInicio(dosn.getFechaInicio());
        dto.setFechaFin(dosn.getFechaFin());
        dto.setCumpleEstandar(dosn.getCumpleEstandar());
        dto.setComentarios(dosn.getComentarios());
        dto.setIdLote(dosn.getLote() != null ? dosn.getLote().getLoteID() : null);
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

        // Mapear historial de análisis
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(dosn.getAnalisisID()));

        return dto;
    }

    private Listado crearListadoDesdeSolicitud(ListadoRequestDTO solicitud, Dosn dosn) {
        Listado listado = MappingUtils.fromListadoRequest(solicitud, malezasYCultivosCatalogoRepository);
        listado.setDosn(dosn);
        return listado;
    }

    /**
     * Finalizar análisis según el rol del usuario
     * - Analistas: pasa a PENDIENTE_APROBACION
     * - Administradores: pasa directamente a APROBADO
     */
    public DosnDTO finalizarAnalisis(Long id) {
        return analisisService.finalizarAnalisisGenerico(
                id,
                dosnRepository,
                this::mapearEntidadADTO,
                null // No hay validación específica
        );
    }

    /**
     * Aprobar análisis (solo administradores)
     */
    public DosnDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
                id,
                dosnRepository,
                this::mapearEntidadADTO,
                null // No hay validación específica
        );
    }

    /**
     * Marcar análisis para repetir (solo administradores)
     */
    public DosnDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
                id,
                dosnRepository,
                this::mapearEntidadADTO,
                null // No hay validación específica para marcar a repetir
        );
    }
}


