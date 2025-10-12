package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.DatosHumedad;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Contacto;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CultivarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ContactoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DatosHumedadDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoLote;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;
    
    @Autowired
    private CatalogoRepository catalogoRepository;
    
    @Autowired
    private CultivarRepository cultivarRepository;
    
    @Autowired
    private ContactoRepository contactoRepository;
    
    @Autowired
    private CatalogoService catalogoService;

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
            e.printStackTrace();
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
            actualizarEntidadDesdeSolicitud(lote, solicitud);
            Lote loteActualizado = loteRepository.save(lote);
            return mapearEntidadADTO(loteActualizado);
        }
        throw new RuntimeException("Lote no encontrado con id: " + id);
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

    // Mapear de RequestDTO a Entity para creación
    private Lote mapearSolicitudAEntidad(LoteRequestDTO solicitud) {
        try {
            System.out.println("Iniciando mapeo de solicitud a entidad");
            Lote lote = new Lote();
            
            System.out.println("Mapeando campos básicos...");
            lote.setFicha(solicitud.getFicha());
            
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

            System.out.println("Mapeo completado exitosamente");
            return lote;
            
        } catch (Exception e) {
            System.err.println("Error durante el mapeo: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error durante el mapeo de la solicitud", e);
        }
    }

    // Actualizar Entity desde RequestDTO para edición
    private void actualizarEntidadDesdeSolicitud(Lote lote, LoteRequestDTO solicitud) {
        lote.setFicha(solicitud.getFicha());
        
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
    }

    // Mapear de Entity a DTO completo
    private LoteDTO mapearEntidadADTO(Lote lote) {
        LoteDTO dto = new LoteDTO();
        
        dto.setLoteID(lote.getLoteID());
        dto.setFicha(lote.getFicha());
        
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

        return dto;
    }

    // Mapear de Entity a DTO simple (solo ID, ficha, activo, cultivar, especie)
    private LoteSimpleDTO mapearEntidadASimpleDTO(Lote lote) {
        LoteSimpleDTO dto = new LoteSimpleDTO();
        dto.setLoteID(lote.getLoteID());
        dto.setFicha(lote.getFicha());
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
            throw new IllegalArgumentException("La fecha de recibo no puede ser posterior a la fecha actual");
        }
    }
}