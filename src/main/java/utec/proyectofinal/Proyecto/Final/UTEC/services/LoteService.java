package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cliente;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Deposito;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear Lote con activo = true
    public LoteDTO crearLote(LoteRequestDTO solicitud) {
        try {
            System.out.println("Creando lote con solicitud: " + solicitud);
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
            lote.setNumeroFicha(solicitud.getNumeroFicha());
            lote.setFicha(solicitud.getFicha());
            lote.setTipo(solicitud.getTipo());
            lote.setEmpresa(solicitud.getEmpresa());
            lote.setCodigoCC(solicitud.getCodigoCC());
            lote.setCodigoFF(solicitud.getCodigoFF());
            lote.setFechaEntrega(solicitud.getFechaEntrega());
            lote.setFechaRecibo(solicitud.getFechaRecibo());
            lote.setUnidadEmbolsado(solicitud.getUnidadEmbolsado());
            lote.setRemitente(solicitud.getRemitente());
            lote.setObservaciones(solicitud.getObservaciones());
            lote.setKilosLimpios(solicitud.getKilosLimpios());
            lote.setHumedad(solicitud.getHumedad());
            lote.setCantidad(solicitud.getCantidad());
            lote.setOrigen(solicitud.getOrigen());
            lote.setEstado(solicitud.getEstado());
            lote.setFechaCosecha(solicitud.getFechaCosecha());

            System.out.println("Mapeando entidades relacionadas...");
            // Mapear entidades relacionadas usando EntityManager
            if (solicitud.getCultivarID() != null) {
                try {
                    System.out.println("Buscando cultivar con ID: " + solicitud.getCultivarID());
                    Cultivar cultivar = entityManager.find(Cultivar.class, solicitud.getCultivarID());
                    if (cultivar != null) {
                        lote.setCultivar(cultivar);
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
                    Cliente cliente = entityManager.find(Cliente.class, solicitud.getClienteID());
                    if (cliente != null) {
                        lote.setCliente(cliente);
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
                    Deposito deposito = entityManager.find(Deposito.class, solicitud.getDepositoID());
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
        lote.setNumeroFicha(solicitud.getNumeroFicha());
        lote.setFicha(solicitud.getFicha());
        lote.setTipo(solicitud.getTipo());
        lote.setEmpresa(solicitud.getEmpresa());
        lote.setCodigoCC(solicitud.getCodigoCC());
        lote.setCodigoFF(solicitud.getCodigoFF());
        lote.setFechaEntrega(solicitud.getFechaEntrega());
        lote.setFechaRecibo(solicitud.getFechaRecibo());
        lote.setUnidadEmbolsado(solicitud.getUnidadEmbolsado());
        lote.setRemitente(solicitud.getRemitente());
        lote.setObservaciones(solicitud.getObservaciones());
        lote.setKilosLimpios(solicitud.getKilosLimpios());
        lote.setHumedad(solicitud.getHumedad());
        lote.setCantidad(solicitud.getCantidad());
        lote.setOrigen(solicitud.getOrigen());
        lote.setEstado(solicitud.getEstado());
        lote.setFechaCosecha(solicitud.getFechaCosecha());

        // Actualizar entidades relacionadas usando EntityManager
        if (solicitud.getCultivarID() != null) {
            try {
                Cultivar cultivar = entityManager.find(Cultivar.class, solicitud.getCultivarID());
                lote.setCultivar(cultivar);
            } catch (Exception e) {
                // Si no se encuentra el cultivar, no lo actualizamos
            }
        } else {
            lote.setCultivar(null);
        }

        if (solicitud.getClienteID() != null) {
            try {
                Cliente cliente = entityManager.find(Cliente.class, solicitud.getClienteID());
                lote.setCliente(cliente);
            } catch (Exception e) {
                // Si no se encuentra el cliente, no lo actualizamos
            }
        } else {
            lote.setCliente(null);
        }

        if (solicitud.getDepositoID() != null) {
            try {
                Deposito deposito = entityManager.find(Deposito.class, solicitud.getDepositoID());
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
        dto.setNumeroFicha(lote.getNumeroFicha());
        dto.setFicha(lote.getFicha());
        dto.setTipo(lote.getTipo());
        dto.setEmpresa(lote.getEmpresa());
        dto.setCodigoCC(lote.getCodigoCC());
        dto.setCodigoFF(lote.getCodigoFF());
        dto.setFechaEntrega(lote.getFechaEntrega());
        dto.setFechaRecibo(lote.getFechaRecibo());
        dto.setUnidadEmbolsado(lote.getUnidadEmbolsado());
        dto.setRemitente(lote.getRemitente());
        dto.setObservaciones(lote.getObservaciones());
        dto.setKilosLimpios(lote.getKilosLimpios());
        dto.setHumedad(lote.getHumedad());
        dto.setCantidad(lote.getCantidad());
        dto.setOrigen(lote.getOrigen());
        dto.setEstado(lote.getEstado());
        dto.setFechaCosecha(lote.getFechaCosecha());
        dto.setActivo(lote.getActivo());

        // Mapear entidades relacionadas
        if (lote.getCultivar() != null) {
            dto.setCultivar(lote.getCultivar().getNombre());
            if (lote.getCultivar().getEspecie() != null) {
                dto.setEspecie(lote.getCultivar().getEspecie().getNombreComun());
            }
        }

        if (lote.getCliente() != null) {
            dto.setCliente(lote.getCliente().getNombre());
        }

        if (lote.getDeposito() != null) {
            dto.setDeposito(lote.getDeposito().getNombre());
        }

        return dto;
    }

    // Mapear de Entity a DTO simple (solo ID, numeroFicha, ficha, activo)
    private LoteSimpleDTO mapearEntidadASimpleDTO(Lote lote) {
        LoteSimpleDTO dto = new LoteSimpleDTO();
        dto.setLoteID(lote.getLoteID());
        dto.setNumeroFicha(lote.getNumeroFicha());
        dto.setFicha(lote.getFicha());
        dto.setActivo(lote.getActivo());
        return dto;
    }
}