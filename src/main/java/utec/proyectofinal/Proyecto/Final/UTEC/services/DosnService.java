package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;   

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.CuscutaRegistro;
import utec.proyectofinal.Proyecto.Final.UTEC.business.mappers.MappingUtils;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ListadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.specifications.DosnSpecification;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CuscutaRegistroRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CuscutaRegistroDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnListadoDTO;

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
    private MalezasCatalogoRepository malezasCatalogoRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Autowired
    private AnalisisService analisisService;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;

    // Crear Dosn
    @Transactional
    public DosnDTO crearDosn(DosnRequestDTO solicitud) {
        Dosn dosn = mapearSolicitudAEntidad(solicitud);
        dosn.setEstado(Estado.EN_PROCESO);

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

    // Eliminar Dosn (desactivar - cambiar activo a false)
    public void eliminarDosn(Long id) {
        Dosn dosn = dosnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dosn no encontrada con id: " + id));

        dosn.setActivo(false);
        dosnRepository.save(dosn);
    }

    // Desactivar DOSN (cambiar activo a false)
    public void desactivarDosn(Long id) {
        analisisService.desactivarAnalisis(id, dosnRepository);
    }

    // Reactivar DOSN (cambiar activo a true)
    public DosnDTO reactivarDosn(Long id) {
        return analisisService.reactivarAnalisis(id, dosnRepository, this::mapearEntidadADTO);
    }

    // Listar todas las Dosn activas
    public ResponseListadoDosn obtenerTodasDosnActivas() {
        List<DosnDTO> dosnDTOs = dosnRepository.findByActivoTrue()
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

    // Listar Dosn con paginado (para listado)
    public Page<DosnListadoDTO> obtenerDosnPaginadas(Pageable pageable) {
        Page<Dosn> dosnPage = dosnRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
        return dosnPage.map(this::mapearEntidadAListadoDTO);
    }

    // Listar Dosn con paginado y filtro de activo
    public Page<DosnListadoDTO> obtenerDosnPaginadasConFiltro(Pageable pageable, String filtroActivo) {
        Page<Dosn> dosnPage;
        
        switch (filtroActivo.toLowerCase()) {
            case "activos":
                dosnPage = dosnRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
                break;
            case "inactivos":
                dosnPage = dosnRepository.findByActivoFalseOrderByFechaInicioDesc(pageable);
                break;
            default: // "todos"
                dosnPage = dosnRepository.findAllByOrderByFechaInicioDesc(pageable);
                break;
        }
        
        return dosnPage.map(this::mapearEntidadAListadoDTO);
    }

    /**
     * Listar DOSN con paginado y filtros dinámicos
     */
    public Page<DosnListadoDTO> obtenerDosnPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String estado,
            Long loteId) {
        
        Specification<Dosn> spec = DosnSpecification.conFiltros(searchTerm, activo, estado, loteId);
        Page<Dosn> dosnPage = dosnRepository.findAll(spec, pageable);
        return dosnPage.map(this::mapearEntidadAListadoDTO);
    }

    // Mapear entidad a DTO de listado simple
    private DosnListadoDTO mapearEntidadAListadoDTO(Dosn dosn) {
        DosnListadoDTO dto = new DosnListadoDTO();
        dto.setAnalisisID(dosn.getAnalisisID());
        dto.setEstado(dosn.getEstado());
        dto.setFechaInicio(dosn.getFechaInicio());
        dto.setFechaFin(dosn.getFechaFin());
        dto.setActivo(dosn.getActivo());
        
        // Campo específico de DOSN
        dto.setCumpleEstandar(dosn.getCumpleEstandar());
        
        if (dosn.getLote() != null) {
            dto.setIdLote(dosn.getLote().getLoteID());
            dto.setLote(dosn.getLote().getNomLote()); // Usar nomLote en lugar de ficha
            
            // Obtener especie del lote - Usar nombreComun primero, luego nombreCientifico
            if (dosn.getLote().getCultivar() != null && dosn.getLote().getCultivar().getEspecie() != null) {
                String nombreEspecie = dosn.getLote().getCultivar().getEspecie().getNombreComun();
                // Si nombreComun está vacío, intentar con nombreCientifico
                if (nombreEspecie == null || nombreEspecie.trim().isEmpty()) {
                    nombreEspecie = dosn.getLote().getCultivar().getEspecie().getNombreCientifico();
                }
                dto.setEspecie(nombreEspecie);
            }
        }
        
        if (dosn.getAnalisisID() != null) {
            var historial = analisisHistorialService.obtenerHistorialAnalisis(dosn.getAnalisisID());
            if (!historial.isEmpty()) {
                var primerRegistro = historial.get(historial.size() - 1);
                dto.setUsuarioCreador(primerRegistro.getUsuario());
                var ultimoRegistro = historial.get(0);
                dto.setUsuarioModificador(ultimoRegistro.getUsuario());
            }
        }
        return dto;
    }

    // === Mappers ===

    private Dosn mapearSolicitudAEntidad(DosnRequestDTO solicitud) {
        Dosn dosn = new Dosn();

        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                Lote lote = loteOpt.get();
                
                // Validar que el lote esté activo
                if (!lote.getActivo()) {
                    throw new RuntimeException("No se puede crear un análisis para un lote inactivo");
                }
                
                dosn.setLote(lote);
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

        // Mapear registros de Cuscuta
        if (solicitud.getCuscutaRegistros() != null && !solicitud.getCuscutaRegistros().isEmpty()) {
            List<CuscutaRegistro> cuscutaRegistros = solicitud.getCuscutaRegistros().stream()
                    .map(req -> crearCuscutaRegistroDesdeSolicitud(req, dosn))
                    .collect(Collectors.toList());
            dosn.setCuscutaRegistros(cuscutaRegistros);
        }

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

        // Actualizar registros de Cuscuta
        if (solicitud.getCuscutaRegistros() != null) {
            // Inicializar la lista si es null
            if (dosn.getCuscutaRegistros() == null) {
                dosn.setCuscutaRegistros(new ArrayList<>());
            }

            // Limpiar registros existentes
            dosn.getCuscutaRegistros().clear();

            // Si hay nuevos registros, crearlos y agregarlos
            if (!solicitud.getCuscutaRegistros().isEmpty()) {
                List<CuscutaRegistro> nuevosCuscutaRegistros = solicitud.getCuscutaRegistros().stream()
                        .map(req -> crearCuscutaRegistroDesdeSolicitud(req, dosn))
                        .collect(Collectors.toList());

                dosn.getCuscutaRegistros().addAll(nuevosCuscutaRegistros);
            }
        }

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
        
        // Datos completos del lote si existe
        if (dosn.getLote() != null) {
            dto.setIdLote(dosn.getLote().getLoteID());
            dto.setLote(dosn.getLote().getNomLote());
            dto.setFicha(dosn.getLote().getFicha());
            
            // Información del cultivar y especie
            if (dosn.getLote().getCultivar() != null) {
                dto.setCultivarNombre(dosn.getLote().getCultivar().getNombre());
                
                if (dosn.getLote().getCultivar().getEspecie() != null) {
                    dto.setEspecieNombre(dosn.getLote().getCultivar().getEspecie().getNombreComun());
                }
            }
        }

        dto.setFechaINIA(dosn.getFechaINIA());
        dto.setGramosAnalizadosINIA(dosn.getGramosAnalizadosINIA());
        dto.setTipoINIA(dosn.getTipoINIA());

        dto.setFechaINASE(dosn.getFechaINASE());
        dto.setGramosAnalizadosINASE(dosn.getGramosAnalizadosINASE());
        dto.setTipoINASE(dosn.getTipoINASE());

        // Mapear registros de Cuscuta
        if (dosn.getCuscutaRegistros() != null) {
            List<CuscutaRegistroDTO> cuscutaRegistroDTOs = dosn.getCuscutaRegistros().stream()
                    .map(this::mapearCuscutaRegistroADTO)
                    .collect(Collectors.toList());
            dto.setCuscutaRegistros(cuscutaRegistroDTOs);
        }

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
        Listado listado = MappingUtils.fromListadoRequest(solicitud, malezasCatalogoRepository, especieRepository);
        listado.setDosn(dosn);
        return listado;
    }

    private CuscutaRegistro crearCuscutaRegistroDesdeSolicitud(CuscutaRegistroRequestDTO solicitud, Dosn dosn) {
        CuscutaRegistro registro = new CuscutaRegistro();
        registro.setInstituto(solicitud.getInstituto());
        registro.setCuscuta_g(solicitud.getCuscuta_g());
        registro.setCuscutaNum(solicitud.getCuscutaNum());
        registro.setFechaCuscuta(solicitud.getFechaCuscuta());
        registro.setDosn(dosn);
        return registro;
    }

    private CuscutaRegistroDTO mapearCuscutaRegistroADTO(CuscutaRegistro registro) {
        CuscutaRegistroDTO dto = new CuscutaRegistroDTO();
        dto.setId(registro.getId());
        dto.setInstituto(registro.getInstituto());
        dto.setCuscuta_g(registro.getCuscuta_g());
        dto.setCuscutaNum(registro.getCuscutaNum());
        dto.setFechaCuscuta(registro.getFechaCuscuta());
        return dto;
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
                this::validarAntesDeFinalizar // Validación específica para DOSN
        );
    }
    
    /**
     * Validación básica previa a la finalización de un DOSN.
     * Requiere al menos una forma de evidencia: resultados INIA o INASE (gramos > 0),
     * datos de cuscuta o listados no vacíos. Si no hay evidencia lanza RuntimeException.
     */
    private void validarAntesDeFinalizar(Dosn dosn) {
        boolean tieneINIA = dosn.getFechaINIA() != null
                && dosn.getGramosAnalizadosINIA() != null
                && dosn.getGramosAnalizadosINIA().compareTo(BigDecimal.ZERO) > 0;

        boolean tieneINASE = dosn.getFechaINASE() != null
                && dosn.getGramosAnalizadosINASE() != null
                && dosn.getGramosAnalizadosINASE().compareTo(BigDecimal.ZERO) > 0;

        boolean tieneCuscuta = dosn.getCuscutaRegistros() != null 
                && !dosn.getCuscutaRegistros().isEmpty()
                && dosn.getCuscutaRegistros().stream().anyMatch(reg ->
                    (reg.getCuscuta_g() != null && reg.getCuscuta_g().compareTo(BigDecimal.ZERO) > 0)
                    || (reg.getCuscutaNum() != null && reg.getCuscutaNum() > 0)
                );

        boolean tieneListados = dosn.getListados() != null && !dosn.getListados().isEmpty();

        if (!tieneINIA && !tieneINASE && !tieneCuscuta && !tieneListados) {
            throw new RuntimeException("No se puede finalizar: el DOSN carece de evidencia. Agregue resultados INIA/INASE, listados, o datos de cuscuta antes de finalizar.");
        }

        if (dosn.getGramosAnalizadosINIA() != null && dosn.getGramosAnalizadosINIA().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Gramos analizados INIA debe ser mayor que 0");
        }
        if (dosn.getGramosAnalizadosINASE() != null && dosn.getGramosAnalizadosINASE().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Gramos analizados INASE debe ser mayor que 0");
        }
    }

    /**
     * Aprobar análisis (solo administradores)
     */
    public DosnDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
                id,
                dosnRepository,
                this::mapearEntidadADTO,
                this::validarAntesDeFinalizar, // Mismas validaciones que finalizar
                (idLote) -> dosnRepository.findByIdLote(idLote.intValue()) // Función para buscar por lote
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
                this::validarAntesDeFinalizar // Mismas validaciones que finalizar
        );
    }
}


