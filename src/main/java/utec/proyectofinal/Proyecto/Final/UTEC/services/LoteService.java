package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Contacto;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.DatosHumedad;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ContactoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CultivarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DatosHumedadDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoLote;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;
    

    
    @Autowired
    private CultivarRepository cultivarRepository;
    
    @Autowired
    private ContactoRepository contactoRepository;
    
    @Autowired
    private CatalogoService catalogoService;
    
    // Repositorios para verificar análisis existentes
    @Autowired
    private PmsRepository pmsRepository;
    
    @Autowired
    private GerminacionRepository germinacionRepository;
    
    @Autowired
    private DosnRepository dosnRepository;
    
    @Autowired
    private TetrazolioRepository tetrazolioRepository;
    
    @Autowired
    private PurezaRepository purezaRepository;

    // Crear Lote con activo = true
    public LoteDTO crearLote(LoteRequestDTO solicitud) {
        try {
            System.out.println("Creando lote con solicitud: " + solicitud);
            
            // Validar fechaRecibo no sea posterior a la fecha actual
            validarFechaRecibo(solicitud.getFechaRecibo());
            
            Lote lote = mapearSolicitudAEntidad(solicitud);
            lote.setActivo(true);
            
            System.out.println("Lote mapeado, guardando...");
            Lote loteGuardado = loteRepository.save(lote);
            System.out.println("Lote guardado con ID: " + loteGuardado.getLoteID());
            return mapearEntidadADTO(loteGuardado);
        } catch (Exception e) {
            System.err.println("Error al crear lote: " + e.getMessage());
            throw new RuntimeException("Error al crear lote: " + e.getMessage(), e);
        }
    }

    // Editar Lote
    public LoteDTO actualizarLote(Long id, LoteRequestDTO solicitud) {
        Optional<Lote> loteExistente = loteRepository.findById(id);
        if (loteExistente.isPresent()) {
            // Validar fechaRecibo no sea posterior a la fecha actual
            validarFechaRecibo(solicitud.getFechaRecibo());
            
            Lote lote = loteExistente.get();
            
            // Validar cambios en tipos de análisis antes de actualizar
            if (solicitud.getTiposAnalisisAsignados() != null) {
                validarCambiosTiposAnalisis(lote, solicitud.getTiposAnalisisAsignados());
            }
            
            actualizarEntidadDesdeSolicitud(lote, solicitud);
            Lote loteActualizado = loteRepository.save(lote);
            return mapearEntidadADTO(loteActualizado);
        }
        throw new RuntimeException("Lote no encontrado con id: " + id);
    }
    
    // Método para verificar si un tipo de análisis puede ser removido del lote
    public boolean puedeRemoverTipoAnalisis(Long loteId, TipoAnalisis tipoAnalisis) {
        // Un tipo de análisis puede ser removido solo si no hay análisis creados de ese tipo
        return switch (tipoAnalisis) {
            case PMS -> !pmsRepository.existsByLoteLoteID(loteId);
            case GERMINACION -> !germinacionRepository.existsByLoteLoteID(loteId);
            case DOSN -> !dosnRepository.existsByLoteLoteID(loteId);
            case TETRAZOLIO -> !tetrazolioRepository.existsByLoteLoteID(loteId);
            case PUREZA -> !purezaRepository.existsByLoteLoteID(loteId);
            default -> true;
        };
    }

    // Método para validar cambios en tipos de análisis durante la edición
    private void validarCambiosTiposAnalisis(Lote loteExistente, List<TipoAnalisis> nuevosTypes) {
        List<TipoAnalisis> tiposActuales = loteExistente.getTiposAnalisisAsignados();
        
        if (tiposActuales == null) {
            tiposActuales = new ArrayList<>();
        }
        
        // Verificar tipos que se están removiendo
        for (TipoAnalisis tipoActual : tiposActuales) {
            if (!nuevosTypes.contains(tipoActual)) {
                // Este tipo se está removiendo, verificar si es posible
                if (!puedeRemoverTipoAnalisis(loteExistente.getLoteID(), tipoActual)) {
                    throw new RuntimeException("No se puede remover el tipo de análisis " + tipoActual.name() + 
                                             " porque ya existen análisis creados de este tipo para el lote");
                }
            }
        }
        
        // Los tipos que se están agregando siempre son permitidos
        // (no hay restricciones para agregar nuevos tipos de análisis)
    }

    // Eliminar Lote (cambiar activo a false)
    public void eliminarLote(Long id) {
        Optional<Lote> loteExistente = loteRepository.findById(id);
        if (loteExistente.isPresent()) {
            Lote lote = loteExistente.get();
            lote.setActivo(false);
            loteRepository.save(lote);
        } else {
            throw new RuntimeException("Lote no encontrado con id: " + id);
        }
    }

    // Reactivar Lote (cambiar activo a true)
    public LoteDTO reactivarLote(Long id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote no encontrado con ID: " + id));
        
        if (lote.getActivo()) {
            throw new RuntimeException("El lote ya está activo");
        }
        
        lote.setActivo(true);
        Lote loteReactivado = loteRepository.save(lote);
        return mapearEntidadADTO(loteReactivado);
    }

    // Listar todos los Lotes activos
    public ResponseListadoLoteSimple obtenerTodosLotesActivos() {
        List<Lote> lotes = loteRepository.findByActivoTrue();
        List<LoteSimpleDTO> loteDTOs = lotes.stream()
                .map(this::mapearEntidadASimpleDTO)
                .collect(Collectors.toList());
        
        ResponseListadoLoteSimple respuesta = new ResponseListadoLoteSimple();
        respuesta.setLotes(loteDTOs);
        return respuesta;
    }

    // Listar todos los Lotes inactivos
    public ResponseListadoLoteSimple obtenerTodosLotesInactivos() {
        List<Lote> lotes = loteRepository.findByActivoFalse();
        List<LoteSimpleDTO> loteDTOs = lotes.stream()
                .map(this::mapearEntidadASimpleDTO)
                .collect(Collectors.toList());
        
        ResponseListadoLoteSimple respuesta = new ResponseListadoLoteSimple();
        respuesta.setLotes(loteDTOs);
        return respuesta;
    }

    // Obtener Lote por ID (completo)
    public LoteDTO obtenerLotePorId(Long id) {
        Optional<Lote> lote = loteRepository.findById(id);
        if (lote.isPresent()) {
            return mapearEntidadADTO(lote.get());
        }
        throw new RuntimeException("Lote no encontrado con id: " + id);
    }

    // Listar Lotes con paginado para listado
    public Page<LoteListadoDTO> obtenerLotesPaginadas(Pageable pageable) {
        Page<Lote> lotePage = loteRepository.findByActivo(true, pageable);
        return lotePage.map(this::mapearEntidadAListadoDTO);
    }

    // Listar Lotes Simple con paginado para listado
    public Page<LoteSimpleDTO> obtenerLotesSimplePaginadas(Pageable pageable) {
        Page<Lote> lotePage = loteRepository.findByActivo(true, pageable);
        return lotePage.map(this::mapearEntidadASimpleDTO);
    }

    private LoteListadoDTO mapearEntidadAListadoDTO(Lote lote) {
        LoteListadoDTO dto = new LoteListadoDTO();
        dto.setLoteID(lote.getLoteID());
        dto.setFicha(lote.getFicha());
        dto.setFechaCosecha(lote.getFechaCosecha());
        dto.setActivo(lote.getActivo());
        return dto;
    }

    // Mapear de RequestDTO a Entity para creación
    private Lote mapearSolicitudAEntidad(LoteRequestDTO solicitud) {
        try {
            System.out.println("Iniciando mapeo de solicitud a entidad");
            Lote lote = new Lote();
            
            System.out.println("Mapeando campos básicos...");
            lote.setFicha(solicitud.getFicha());
            lote.setNomLote(solicitud.getNomLote());
            
            // Convertir String a enum TipoLote
            if (solicitud.getTipo() != null) {
                lote.setTipo(TipoLote.valueOf(solicitud.getTipo().toUpperCase()));
            }
            
            // Mapear Empresa
            if (solicitud.getEmpresaID() != null) {
                try {
                    System.out.println("Buscando empresa con ID: " + solicitud.getEmpresaID());
                    Optional<Contacto> empresaOpt = contactoRepository.findById(solicitud.getEmpresaID());
                    if (empresaOpt.isPresent()) {
                        lote.setEmpresa(empresaOpt.get());
                        System.out.println("Empresa encontrada y asignada");
                    } else {
                        System.out.println("Empresa no encontrada con ID: " + solicitud.getEmpresaID());
                    }
                } catch (Exception e) {
                    System.err.println("Error al buscar empresa: " + e.getMessage());
                }
            }
            
            lote.setCodigoCC(solicitud.getCodigoCC());
            lote.setCodigoFF(solicitud.getCodigoFF());
            lote.setFechaEntrega(solicitud.getFechaEntrega());
            lote.setFechaRecibo(solicitud.getFechaRecibo());
            lote.setUnidadEmbolsado(solicitud.getUnidadEmbolsado());
            lote.setRemitente(solicitud.getRemitente());
            lote.setObservaciones(solicitud.getObservaciones());
            lote.setKilosLimpios(solicitud.getKilosLimpios());
            
            // Mapear datos de humedad
            if (solicitud.getDatosHumedad() != null && !solicitud.getDatosHumedad().isEmpty()) {
                List<DatosHumedad> datosHumedad = solicitud.getDatosHumedad().stream()
                    .map(dhr -> {
                        DatosHumedad dh = new DatosHumedad();
                        
                        // Obtener el catálogo de tipo humedad por ID
                        if (dhr.getTipoHumedadID() != null) {
                            Catalogo tipoHumedad = catalogoService.obtenerEntidadPorId(dhr.getTipoHumedadID());
                            dh.setTipoHumedad(tipoHumedad);
                        }
                        
                        dh.setValor(dhr.getValor());
                        dh.setLote(lote);
                        return dh;
                    })
                    .collect(Collectors.toList());
                lote.setDatosHumedad(datosHumedad);
            }
            
            // Mapear número de artículo
            if (solicitud.getNumeroArticuloID() != null) {
                Catalogo numeroArticulo = catalogoService.obtenerEntidadPorId(solicitud.getNumeroArticuloID());
                lote.setNumeroArticulo(numeroArticulo);
            }
            lote.setCantidad(solicitud.getCantidad());
            
            // Mapear origen
            if (solicitud.getOrigenID() != null) {
                try {
                    System.out.println("Buscando origen con ID: " + solicitud.getOrigenID());
                    Catalogo origen = catalogoService.obtenerEntidadPorId(solicitud.getOrigenID());
                    if (origen != null) {
                        lote.setOrigen(origen);
                        System.out.println("Origen encontrado y asignado");
                    } else {
                        System.out.println("Origen no encontrado con ID: " + solicitud.getOrigenID());
                    }
                } catch (Exception e) {
                    System.err.println("Error al buscar origen: " + e.getMessage());
                }
            }
            
            // Mapear estado
            if (solicitud.getEstadoID() != null) {
                try {
                    System.out.println("Buscando estado con ID: " + solicitud.getEstadoID());
                    Catalogo estado = catalogoService.obtenerEntidadPorId(solicitud.getEstadoID());
                    if (estado != null) {
                        lote.setEstado(estado);
                        System.out.println("Estado encontrado y asignado");
                    } else {
                        System.out.println("Estado no encontrado con ID: " + solicitud.getEstadoID());
                    }
                } catch (Exception e) {
                    System.err.println("Error al buscar estado: " + e.getMessage());
                }
            }
            
            lote.setFechaCosecha(solicitud.getFechaCosecha());

            System.out.println("Mapeando entidades relacionadas...");
            // Mapear entidades relacionadas usando repositorios
            if (solicitud.getCultivarID() != null) {
                try {
                    System.out.println("Buscando cultivar con ID: " + solicitud.getCultivarID());
                    Optional<Cultivar> cultivarOpt = cultivarRepository.findById(solicitud.getCultivarID());
                    if (cultivarOpt.isPresent()) {
                        lote.setCultivar(cultivarOpt.get());
                        System.out.println("Cultivar encontrado y asignado");
                    } else {
                        System.out.println("Cultivar no encontrado con ID: " + solicitud.getCultivarID());
                    }
                } catch (Exception e) {
                    System.err.println("Error al buscar cultivar: " + e.getMessage());
                }
            }

            if (solicitud.getClienteID() != null) {
                try {
                    System.out.println("Buscando cliente con ID: " + solicitud.getClienteID());
                    Optional<Contacto> clienteOpt = contactoRepository.findById(solicitud.getClienteID());
                    if (clienteOpt.isPresent()) {
                        lote.setCliente(clienteOpt.get());
                        System.out.println("Cliente encontrado y asignado");
                    } else {
                        System.out.println("Cliente no encontrado con ID: " + solicitud.getClienteID());
                    }
                } catch (Exception e) {
                    System.err.println("Error al buscar cliente: " + e.getMessage());
                }
            }

            if (solicitud.getDepositoID() != null) {
                try {
                    System.out.println("Buscando deposito con ID: " + solicitud.getDepositoID());
                    Catalogo deposito = catalogoService.obtenerEntidadPorId(solicitud.getDepositoID());
                    if (deposito != null) {
                        lote.setDeposito(deposito);
                        System.out.println("Deposito encontrado y asignado");
                    } else {
                        System.out.println("Deposito no encontrado con ID: " + solicitud.getDepositoID());
                    }
                } catch (Exception e) {
                    System.err.println("Error al buscar deposito: " + e.getMessage());
                }
            }

            // Mapear tipos de análisis asignados
            if (solicitud.getTiposAnalisisAsignados() != null && !solicitud.getTiposAnalisisAsignados().isEmpty()) {
                // Usar el método personalizado que elimina duplicados
                lote.setTiposAnalisisAsignados(solicitud.getTiposAnalisisAsignados());
                System.out.println("Tipos de análisis asignados (sin duplicados): " + lote.getTiposAnalisisAsignados());
            }

            System.out.println("Mapeo completado exitosamente");
            return lote;
            
        } catch (Exception e) {
            System.err.println("Error durante el mapeo: " + e.getMessage());
            throw new RuntimeException("Error durante el mapeo de la solicitud", e);
        }
    }

    // Actualizar Entity desde RequestDTO para edición
    private void actualizarEntidadDesdeSolicitud(Lote lote, LoteRequestDTO solicitud) {
        lote.setFicha(solicitud.getFicha());
        lote.setNomLote(solicitud.getNomLote());
        
        // Convertir String a enum TipoLote
        if (solicitud.getTipo() != null) {
            lote.setTipo(TipoLote.valueOf(solicitud.getTipo().toUpperCase()));
        }
        
        // Mapear Empresa
        if (solicitud.getEmpresaID() != null) {
            Optional<Contacto> empresaOpt = contactoRepository.findById(solicitud.getEmpresaID());
            if (empresaOpt.isPresent()) {
                lote.setEmpresa(empresaOpt.get());
            } else {
                throw new RuntimeException("Empresa no encontrada con ID: " + solicitud.getEmpresaID());
            }
        }
        
        lote.setCodigoCC(solicitud.getCodigoCC());
        lote.setCodigoFF(solicitud.getCodigoFF());
        lote.setFechaEntrega(solicitud.getFechaEntrega());
        lote.setFechaRecibo(solicitud.getFechaRecibo());
        lote.setUnidadEmbolsado(solicitud.getUnidadEmbolsado());
        lote.setRemitente(solicitud.getRemitente());
        lote.setObservaciones(solicitud.getObservaciones());
        lote.setKilosLimpios(solicitud.getKilosLimpios());
        
        // Actualizar datos de humedad
        if (solicitud.getDatosHumedad() != null) {
            // Limpiar datos existentes
            if (lote.getDatosHumedad() != null) {
                lote.getDatosHumedad().clear();
            }
            
            // Agregar nuevos datos
            if (!solicitud.getDatosHumedad().isEmpty()) {
                List<DatosHumedad> datosHumedad = solicitud.getDatosHumedad().stream()
                    .map(dhr -> {
                        DatosHumedad dh = new DatosHumedad();
                        
                        // Obtener el catálogo de tipo humedad por ID
                        if (dhr.getTipoHumedadID() != null) {
                            Catalogo tipoHumedad = catalogoService.obtenerEntidadPorId(dhr.getTipoHumedadID());
                            dh.setTipoHumedad(tipoHumedad);
                        }
                        
                        dh.setValor(dhr.getValor());
                        dh.setLote(lote);
                        return dh;
                    })
                    .collect(Collectors.toList());
                lote.setDatosHumedad(datosHumedad);
            }
        }
        
        // Actualizar número de artículo
        if (solicitud.getNumeroArticuloID() != null) {
            Catalogo numeroArticulo = catalogoService.obtenerEntidadPorId(solicitud.getNumeroArticuloID());
            lote.setNumeroArticulo(numeroArticulo);
        } else {
            lote.setNumeroArticulo(null);
        }
        lote.setCantidad(solicitud.getCantidad());
        
        // Actualizar origen
        if (solicitud.getOrigenID() != null) {
            Catalogo origen = catalogoService.obtenerEntidadPorId(solicitud.getOrigenID());
            lote.setOrigen(origen);
        } else {
            lote.setOrigen(null);
        }
        
        // Actualizar estado
        if (solicitud.getEstadoID() != null) {
            Catalogo estado = catalogoService.obtenerEntidadPorId(solicitud.getEstadoID());
            lote.setEstado(estado);
        } else {
            lote.setEstado(null);
        }
        
        lote.setFechaCosecha(solicitud.getFechaCosecha());

        // Actualizar entidades relacionadas usando repositorios
        if (solicitud.getCultivarID() != null) {
            try {
                Optional<Cultivar> cultivarOpt = cultivarRepository.findById(solicitud.getCultivarID());
                if (cultivarOpt.isPresent()) {
                    lote.setCultivar(cultivarOpt.get());
                }
            } catch (Exception e) {
                // Si no se encuentra el cultivar, no lo actualizamos
            }
        } else {
            lote.setCultivar(null);
        }

        if (solicitud.getClienteID() != null) {
            try {
                Optional<Contacto> clienteOpt = contactoRepository.findById(solicitud.getClienteID());
                if (clienteOpt.isPresent()) {
                    lote.setCliente(clienteOpt.get());
                }
            } catch (Exception e) {
                // Si no se encuentra el cliente, no lo actualizamos
            }
        } else {
            lote.setCliente(null);
        }

        if (solicitud.getDepositoID() != null) {
            try {
                Catalogo deposito = catalogoService.obtenerEntidadPorId(solicitud.getDepositoID());
                lote.setDeposito(deposito);
            } catch (Exception e) {
                // Si no se encuentra el deposito, no lo actualizamos
            }
        } else {
            lote.setDeposito(null);
        }
        
        // Mapear tipos de análisis asignados
        if (solicitud.getTiposAnalisisAsignados() != null && !solicitud.getTiposAnalisisAsignados().isEmpty()) {
            // Usar el método personalizado que elimina duplicados
            lote.setTiposAnalisisAsignados(solicitud.getTiposAnalisisAsignados());
        }
    }

    // Mapear de Entity a DTO completo
    private LoteDTO mapearEntidadADTO(Lote lote) {
        LoteDTO dto = new LoteDTO();
        
        dto.setLoteID(lote.getLoteID());
        dto.setFicha(lote.getFicha());
        dto.setNomLote(lote.getNomLote());
        
        // Convertir enum TipoLote a String
        if (lote.getTipo() != null) {
            dto.setTipo(lote.getTipo().name());
        }
        
        // Mapear Empresa
        if (lote.getEmpresa() != null) {
            dto.setEmpresaID(lote.getEmpresa().getContactoID());
            dto.setEmpresaNombre(lote.getEmpresa().getNombre());
        }
        
        dto.setCodigoCC(lote.getCodigoCC());
        dto.setCodigoFF(lote.getCodigoFF());
        dto.setFechaEntrega(lote.getFechaEntrega());
        dto.setFechaRecibo(lote.getFechaRecibo());
        dto.setUnidadEmbolsado(lote.getUnidadEmbolsado());
        dto.setRemitente(lote.getRemitente());
        dto.setObservaciones(lote.getObservaciones());
        dto.setKilosLimpios(lote.getKilosLimpios());
        
        // Mapear datos de humedad
        if (lote.getDatosHumedad() != null && !lote.getDatosHumedad().isEmpty()) {
            List<DatosHumedadDTO> datosHumedadDTO = lote.getDatosHumedad().stream()
                .map(dh -> {
                    DatosHumedadDTO dhDTO = new DatosHumedadDTO();
                    dhDTO.setDatosHumedadID(dh.getDatosHumedadID());
                    
                    if (dh.getTipoHumedad() != null) {
                        dhDTO.setTipoHumedadID(dh.getTipoHumedad().getId());
                        dhDTO.setTipoHumedadValor(dh.getTipoHumedad().getValor());
                    }
                    
                    dhDTO.setValor(dh.getValor());
                    return dhDTO;
                })
                .collect(Collectors.toList());
            dto.setDatosHumedad(datosHumedadDTO);
        }
        
        // Mapear número de artículo
        if (lote.getNumeroArticulo() != null) {
            dto.setNumeroArticuloID(lote.getNumeroArticulo().getId());
            dto.setNumeroArticuloValor(lote.getNumeroArticulo().getValor());
        }
        dto.setCantidad(lote.getCantidad());
        dto.setFechaCosecha(lote.getFechaCosecha());
        dto.setActivo(lote.getActivo());

        // Mapear entidades relacionadas
        if (lote.getCultivar() != null) {
            dto.setCultivarID(lote.getCultivar().getCultivarID());
            dto.setCultivarNombre(lote.getCultivar().getNombre());
        }

        if (lote.getCliente() != null) {
            dto.setClienteID(lote.getCliente().getContactoID());
            dto.setClienteNombre(lote.getCliente().getNombre());
        }

        if (lote.getDeposito() != null) {
            dto.setDepositoID(lote.getDeposito().getId());
            dto.setDepositoValor(lote.getDeposito().getValor());
        }
        
        if (lote.getOrigen() != null) {
            dto.setOrigenID(lote.getOrigen().getId());
            dto.setOrigenValor(lote.getOrigen().getValor());
        }
        
        if (lote.getEstado() != null) {
            dto.setEstadoID(lote.getEstado().getId());
            dto.setEstadoValor(lote.getEstado().getValor());
        }
        
        // Mapear tipos de análisis asignados - usar el getter personalizado que elimina duplicados
        List<TipoAnalisis> tiposAnalisis = lote.getTiposAnalisisAsignados();
        if (tiposAnalisis != null && !tiposAnalisis.isEmpty()) {
            dto.setTiposAnalisisAsignados(tiposAnalisis);
        }

        return dto;
    }

    // Mapear de Entity a DTO simple (solo ID, ficha, activo, cultivar, especie)
    private LoteSimpleDTO mapearEntidadASimpleDTO(Lote lote) {
        LoteSimpleDTO dto = new LoteSimpleDTO();
        dto.setLoteID(lote.getLoteID());
        dto.setFicha(lote.getFicha());
        dto.setNomLote(lote.getNomLote());
        dto.setActivo(lote.getActivo());
        
        // Mapear cultivar nombre directamente
        if (lote.getCultivar() != null) {
            dto.setCultivarNombre(lote.getCultivar().getNombre());
        }
        
        // Mapear especie nombre directamente
        if (lote.getCultivar() != null && lote.getCultivar().getEspecie() != null) {
            dto.setEspecieNombre(lote.getCultivar().getEspecie().getNombreComun());
        }
        
        return dto;
    }

    private void validarFechaRecibo(LocalDate fechaRecibo) {
        if (fechaRecibo != null && fechaRecibo.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de recibo no puede ser posterior a la fecha actual");
        }
    }
    
    // Método para verificar si un lote es elegible para crear un análisis de un tipo específico
    public boolean esLoteElegibleParaTipoAnalisis(Long loteId, TipoAnalisis tipoAnalisis) {
        Optional<Lote> loteOpt = loteRepository.findById(loteId);
        if (loteOpt.isEmpty()) {
            return false;
        }
        
        Lote lote = loteOpt.get();
        
        // 1. Verificar que el lote tenga ese tipo de análisis asignado
        if (lote.getTiposAnalisisAsignados() == null || 
            !lote.getTiposAnalisisAsignados().contains(tipoAnalisis)) {
            return false;
        }
        
        // 2. Verificar estado de análisis existentes de ese tipo
        return puedeCrearAnalisisDelTipo(loteId, tipoAnalisis);
    }
    
    // Método para verificar si se puede crear un análisis de un tipo específico
    public boolean puedeCrearAnalisisDelTipo(Long loteId, TipoAnalisis tipoAnalisis) {
        // Verificar análisis existentes según el tipo
        return switch (tipoAnalisis) {
            case PMS -> !pmsRepository.existsByLoteLoteID(loteId) || 
                       pmsRepository.existsByLoteLoteIDAndEstado(loteId, Estado.A_REPETIR);
            case GERMINACION -> !germinacionRepository.existsByLoteLoteID(loteId) || 
                       germinacionRepository.existsByLoteLoteIDAndEstado(loteId, Estado.A_REPETIR);
            case DOSN -> !dosnRepository.existsByLoteLoteID(loteId) || 
                       dosnRepository.existsByLoteLoteIDAndEstado(loteId, Estado.A_REPETIR);
            case TETRAZOLIO -> !tetrazolioRepository.existsByLoteLoteID(loteId) || 
                       tetrazolioRepository.existsByLoteLoteIDAndEstado(loteId, Estado.A_REPETIR);
            case PUREZA -> !purezaRepository.existsByLoteLoteID(loteId) || 
                       purezaRepository.existsByLoteLoteIDAndEstado(loteId, Estado.A_REPETIR);
            default -> true;
        };
    }
    
    // Método para obtener lotes elegibles para un tipo de análisis específico
    public ResponseListadoLoteSimple obtenerLotesElegiblesParaTipoAnalisis(TipoAnalisis tipoAnalisis) {
        List<Lote> todosLosLotes = loteRepository.findByActivoTrue();
        
        List<LoteSimpleDTO> lotesElegibles = todosLosLotes.stream()
                .filter(lote -> esLoteElegibleParaTipoAnalisis(lote.getLoteID(), tipoAnalisis))
                .map(this::mapearEntidadASimpleDTO)
                .collect(Collectors.toList());
        
        ResponseListadoLoteSimple respuesta = new ResponseListadoLoteSimple();
        respuesta.setLotes(lotesElegibles);
        return respuesta;
    }
    
    // Método para contar análisis pendientes (asignados pero no realizados o todos en A_REPETIR)
    public long contarAnalisisPendientes() {
        List<Lote> lotesActivos = loteRepository.findByActivoTrue();
        long totalPendientes = 0;
        
        for (Lote lote : lotesActivos) {
            if (lote.getTiposAnalisisAsignados() == null) continue;
            
            for (TipoAnalisis tipo : lote.getTiposAnalisisAsignados()) {
                // Verificar si el análisis no existe o todos están marcados como A_REPETIR
                boolean pendiente = switch (tipo) {
                    case PMS -> {
                        // No existe ningún análisis
                        if (!pmsRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        // Existen análisis, verificar si TODOS están en A_REPETIR
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms> analisis = 
                            pmsRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case GERMINACION -> {
                        if (!germinacionRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion> analisis = 
                            germinacionRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case DOSN -> {
                        if (!dosnRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn> analisis = 
                            dosnRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case TETRAZOLIO -> {
                        if (!tetrazolioRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio> analisis = 
                            tetrazolioRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    case PUREZA -> {
                        if (!purezaRepository.existsByLoteLoteID(lote.getLoteID())) {
                            yield true;
                        }
                        List<utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza> analisis = 
                            purezaRepository.findByLoteLoteID(lote.getLoteID());
                        yield analisis.stream().allMatch(a -> a.getEstado() == Estado.A_REPETIR);
                    }
                    default -> false;
                };
                
                if (pendiente) {
                    totalPendientes++;
                }
            }
        }
        
        return totalPendientes;
    }
}
