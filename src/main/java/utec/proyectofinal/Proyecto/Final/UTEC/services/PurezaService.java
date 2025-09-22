package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Pureza con estado REGISTRADO
    public PurezaDTO crearPureza(PurezaRequestDTO solicitud) {
        // Validar pesos antes de crear
        validarPesos(solicitud.getPesoInicial_g(), solicitud.getPesoTotal_g());
        
        Pureza pureza = mapearSolicitudAEntidad(solicitud);
        pureza.setEstado(Estado.REGISTRADO);
        Pureza purezaGuardada = purezaRepository.save(pureza);
        
        // Registrar automáticamente en el historial
        analisisHistorialService.registrarCreacion(purezaGuardada);
        
        return mapearEntidadADTO(purezaGuardada);
    }

    // Editar Pureza
    public PurezaDTO actualizarPureza(Long id, PurezaRequestDTO solicitud) {
        Pureza pureza = purezaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));

        // Si el análisis está APROBADO y el usuario actual es ANALISTA, cambiar a PENDIENTE_APROBACION
        if (pureza.getEstado() == Estado.APROBADO && analisisService.esAnalista()) {
            pureza.setEstado(Estado.PENDIENTE_APROBACION);
        }

        // Validar pesos antes de actualizar
        BigDecimal pesoInicial = solicitud.getPesoInicial_g() != null ? solicitud.getPesoInicial_g() : pureza.getPesoInicial_g();
        BigDecimal pesoTotal = solicitud.getPesoTotal_g() != null ? solicitud.getPesoTotal_g() : pureza.getPesoTotal_g();
        validarPesos(pesoInicial, pesoTotal);

        actualizarEntidadDesdeSolicitud(pureza, solicitud);
        Pureza purezaActualizada = purezaRepository.save(pureza);
        
        // Registrar automáticamente en el historial
        analisisHistorialService.registrarModificacion(purezaActualizada);
        
        return mapearEntidadADTO(purezaActualizada);
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

        pureza.setInasePura(solicitud.getInasePura());
        pureza.setInaseMateriaInerte(solicitud.getInaseMateriaInerte());
        pureza.setInaseOtrosCultivos(solicitud.getInaseOtrosCultivos());
        pureza.setInaseMalezas(solicitud.getInaseMalezas());
        pureza.setInaseMalezasToleradas(solicitud.getInaseMalezasToleradas());
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

        if (solicitud.getInasePura() != null) pureza.setInasePura(solicitud.getInasePura());
        if (solicitud.getInaseMateriaInerte() != null) pureza.setInaseMateriaInerte(solicitud.getInaseMateriaInerte());
        if (solicitud.getInaseOtrosCultivos() != null) pureza.setInaseOtrosCultivos(solicitud.getInaseOtrosCultivos());
        if (solicitud.getInaseMalezas() != null) pureza.setInaseMalezas(solicitud.getInaseMalezas());
        if (solicitud.getInaseMalezasToleradas() != null) pureza.setInaseMalezasToleradas(solicitud.getInaseMalezasToleradas());
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

        dto.setInasePura(pureza.getInasePura());
        dto.setInaseMateriaInerte(pureza.getInaseMateriaInerte());
        dto.setInaseOtrosCultivos(pureza.getInaseOtrosCultivos());
        dto.setInaseMalezas(pureza.getInaseMalezas());
        dto.setInaseMalezasToleradas(pureza.getInaseMalezasToleradas());
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

    /**
     * Valida las reglas de negocio relacionadas con los pesos en el análisis de pureza
     * @param pesoInicial_g Peso inicial de la muestra
     * @param pesoTotal_g Peso total después del análisis
     * @throws RuntimeException si alguna validación falla
     */
    private void validarPesos(BigDecimal pesoInicial_g, BigDecimal pesoTotal_g) {
        if (pesoInicial_g == null || pesoTotal_g == null) {
            return; // No validar si los valores son nulos
        }

        // Validación 1: El peso total no puede ser mayor al inicial
        if (pesoTotal_g.compareTo(pesoInicial_g) > 0) {
            throw new RuntimeException("El peso total (" + pesoTotal_g + "g) no puede ser mayor al peso inicial (" + pesoInicial_g + "g)");
        }

        // Validación 2: Alerta si se pierde más del 5% de la muestra
        BigDecimal diferenciaPeso = pesoInicial_g.subtract(pesoTotal_g);
        BigDecimal porcentajePerdida = diferenciaPeso.divide(pesoInicial_g, 4, java.math.RoundingMode.HALF_UP)
                                                    .multiply(new BigDecimal("100"));
        
        BigDecimal limitePermitido = new BigDecimal("5.0");
        if (porcentajePerdida.compareTo(limitePermitido) > 0) {
            throw new RuntimeException("ALERTA: La muestra ha perdido " + porcentajePerdida.setScale(2, java.math.RoundingMode.HALF_UP) + 
                                     "% de su peso inicial, lo cual excede el límite permitido del 5%. " +
                                     "Pérdida: " + diferenciaPeso.setScale(2, java.math.RoundingMode.HALF_UP) + "g");
        }
    }

    /**
     * Finalizar análisis según el rol del usuario
     * - Analistas: pasa a PENDIENTE_APROBACION
     * - Administradores: pasa directamente a APROBADO
     */
    public PurezaDTO finalizarAnalisis(Long id) {
        Pureza pureza = purezaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));
        
        // Usar el servicio común para finalizar el análisis
        analisisService.finalizarAnalisis(pureza);
        
        // Guardar cambios
        Pureza purezaActualizada = purezaRepository.save(pureza);
        
        return mapearEntidadADTO(purezaActualizada);
    }

    /**
     * Aprobar análisis (solo administradores)
     */
    public PurezaDTO aprobarAnalisis(Long id) {
        Pureza pureza = purezaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pureza no encontrada con id: " + id));
        
        // Usar el servicio común para aprobar el análisis
        analisisService.aprobarAnalisis(pureza);
        
        // Guardar cambios
        Pureza purezaActualizada = purezaRepository.save(pureza);
        
        return mapearEntidadADTO(purezaActualizada);
    }
}
