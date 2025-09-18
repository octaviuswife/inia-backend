package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ContGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ContGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ValoresGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ContGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ContGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

@Service
public class ContGermService {

    @Autowired
    private ContGermRepository contGermRepository;

    @Autowired
    private ValoresGermRepository valoresGermRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear nuevo conteo asociado a una germinación
    public ContGermDTO crearContGerm(Long germinacionId, ContGermRequestDTO solicitud) {
        try {
            System.out.println("Creando conteo para germinación ID: " + germinacionId);
            
            // Validar que la germinación existe
            Germinacion germinacion = entityManager.find(Germinacion.class, germinacionId);
            if (germinacion == null) {
                throw new RuntimeException("Germinación no encontrada con ID: " + germinacionId);
            }
            
            // Crear el conteo
            ContGerm contGerm = mapearSolicitudAEntidad(solicitud, germinacion);
            ContGerm contGermGuardado = contGermRepository.save(contGerm);
            
            // Crear automáticamente los ValoresGerm para INIA e INASE
            crearValoresGermAutomaticos(contGermGuardado);
            
            System.out.println("Conteo creado exitosamente con ID: " + contGermGuardado.getContGermID());
            return mapearEntidadADTO(contGermGuardado);
        } catch (Exception e) {
            System.err.println("Error al crear conteo: " + e.getMessage());
            throw new RuntimeException("Error al crear el conteo: " + e.getMessage());
        }
    }

    // Obtener conteo por ID
    public ContGermDTO obtenerContGermPorId(Long id) {
        Optional<ContGerm> contGerm = contGermRepository.findById(id);
        if (contGerm.isPresent()) {
            return mapearEntidadADTO(contGerm.get());
        } else {
            throw new RuntimeException("Conteo no encontrado con ID: " + id);
        }
    }

    // Actualizar conteo
    public ContGermDTO actualizarContGerm(Long id, ContGermRequestDTO solicitud) {
        Optional<ContGerm> contGermExistente = contGermRepository.findById(id);
        
        if (contGermExistente.isPresent()) {
            ContGerm contGerm = contGermExistente.get();
            actualizarEntidadDesdeSolicitud(contGerm, solicitud);
            ContGerm contGermActualizado = contGermRepository.save(contGerm);
            return mapearEntidadADTO(contGermActualizado);
        } else {
            throw new RuntimeException("Conteo no encontrado con ID: " + id);
        }
    }

    // Eliminar conteo (eliminar realmente)
    public void eliminarContGerm(Long id) {
        Optional<ContGerm> contGermExistente = contGermRepository.findById(id);
        
        if (contGermExistente.isPresent()) {
            contGermRepository.deleteById(id);
            System.out.println("Conteo eliminado con ID: " + id);
        } else {
            throw new RuntimeException("Conteo no encontrado con ID: " + id);
        }
    }

    // Obtener todos los conteos de una germinación
    public List<ContGermDTO> obtenerConteosPorGerminacion(Long germinacionId) {
        List<ContGerm> conteos = contGermRepository.findByGerminacionId(germinacionId);
        return conteos.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Contar conteos de una germinación
    public Long contarConteosPorGerminacion(Long germinacionId) {
        return contGermRepository.countByGerminacionId(germinacionId);
    }

    // Crear ValoresGerm automáticos para INIA e INASE con valores en 0
    private void crearValoresGermAutomaticos(ContGerm contGerm) {
        System.out.println("Creando ValoresGerm automáticos para conteo ID: " + contGerm.getContGermID());
        
        // Crear ValoresGerm para INIA
        ValoresGerm valoresInia = new ValoresGerm();
        valoresInia.setInstituto(Instituto.INIA);
        valoresInia.setContGerm(contGerm);
        inicializarValoresEnCero(valoresInia);
        valoresGermRepository.save(valoresInia);
        
        // Crear ValoresGerm para INASE
        ValoresGerm valoresInase = new ValoresGerm();
        valoresInase.setInstituto(Instituto.INASE);
        valoresInase.setContGerm(contGerm);
        inicializarValoresEnCero(valoresInase);
        valoresGermRepository.save(valoresInase);
        
        System.out.println("ValoresGerm creados para INIA e INASE");
    }

    // Inicializar todos los valores en 0
    private void inicializarValoresEnCero(ValoresGerm valores) {
        valores.setNormales(BigDecimal.ZERO);
        valores.setAnormales(BigDecimal.ZERO);
        valores.setDuras(BigDecimal.ZERO);
        valores.setFrescas(BigDecimal.ZERO);
        valores.setMuertas(BigDecimal.ZERO);
        valores.setGerminacion(BigDecimal.ZERO);
    }

    // Calcular automáticamente los valores de INIA basados en las repeticiones
    public void calcularYActualizarValoresINIA(ContGerm contGerm) {
        System.out.println("Calculando valores automáticos de INIA para conteo ID: " + contGerm.getContGermID());
        
        // Obtener los valores de INIA para este conteo
        Optional<ValoresGerm> valoresIniaOpt = valoresGermRepository.findByContGermIdAndInstituto(
            contGerm.getContGermID(), Instituto.INIA);
        
        if (valoresIniaOpt.isPresent()) {
            ValoresGerm valoresInia = valoresIniaOpt.get();
            
            // Obtener todas las repeticiones del conteo (refrescar desde BD)
            Optional<ContGerm> contGermActualizado = contGermRepository.findById(contGerm.getContGermID());
            List<RepGerm> repeticiones = contGermActualizado.isPresent() ? 
                contGermActualizado.get().getRepGerm() : new ArrayList<>();
            
            if (repeticiones == null || repeticiones.isEmpty()) {
                // Si no hay repeticiones, inicializar todo en 0
                inicializarValoresEnCero(valoresInia);
            } else {
                // Calcular totales de todas las repeticiones
                CalculosRepeticiones calculos = calcularTotalesRepeticiones(repeticiones);
                
                // Calcular porcentajes basados en el total general
                if (calculos.getTotalGeneral() > 0) {
                    BigDecimal totalGeneral = new BigDecimal(calculos.getTotalGeneral());
                    
                    valoresInia.setNormales(calcularPorcentaje(calculos.getTotalNormales(), totalGeneral));
                    valoresInia.setAnormales(calcularPorcentaje(calculos.getTotalAnormales(), totalGeneral));
                    valoresInia.setDuras(calcularPorcentaje(calculos.getTotalDuras(), totalGeneral));
                    valoresInia.setFrescas(calcularPorcentaje(calculos.getTotalFrescas(), totalGeneral));
                    valoresInia.setMuertas(calcularPorcentaje(calculos.getTotalMuertas(), totalGeneral));
                    
                    // Germinación es igual que normales
                    valoresInia.setGerminacion(valoresInia.getNormales());
                    
                    System.out.println("Valores INIA calculados - Normales: " + valoresInia.getNormales() + 
                                     "%, Anormales: " + valoresInia.getAnormales() + 
                                     "%, Duras: " + valoresInia.getDuras() + "%");
                } else {
                    inicializarValoresEnCero(valoresInia);
                }
            }
            
            valoresGermRepository.save(valoresInia);
            System.out.println("Valores de INIA actualizados automáticamente");
        } else {
            System.err.println("No se encontraron valores de INIA para el conteo ID: " + contGerm.getContGermID());
        }
    }

    // Calcular totales de todas las repeticiones
    private CalculosRepeticiones calcularTotalesRepeticiones(List<RepGerm> repeticiones) {
        int totalNormales = 0;
        int totalAnormales = 0;
        int totalDuras = 0;
        int totalFrescas = 0;
        int totalMuertas = 0;
        int totalGeneral = 0;
        
        for (RepGerm repeticion : repeticiones) {
            // Sumar todos los elementos de la lista de normales
            if (repeticion.getNormales() != null) {
                totalNormales += repeticion.getNormales().stream().mapToInt(Integer::intValue).sum();
            }
            
            // Sumar los demás campos
            if (repeticion.getAnormales() != null) totalAnormales += repeticion.getAnormales();
            if (repeticion.getDuras() != null) totalDuras += repeticion.getDuras();
            if (repeticion.getFrescas() != null) totalFrescas += repeticion.getFrescas();
            if (repeticion.getMuertas() != null) totalMuertas += repeticion.getMuertas();
            if (repeticion.getTotal() != null) totalGeneral += repeticion.getTotal();
        }
        
        return new CalculosRepeticiones(totalNormales, totalAnormales, totalDuras, 
                                       totalFrescas, totalMuertas, totalGeneral);
    }

    // Calcular porcentaje con 2 decimales
    private BigDecimal calcularPorcentaje(int valor, BigDecimal totalGeneral) {
        if (totalGeneral.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal valorDecimal = new BigDecimal(valor);
        return valorDecimal.multiply(new BigDecimal("100"))
                          .divide(totalGeneral, 2, RoundingMode.HALF_UP);
    }

    // Clase interna para manejar los cálculos
    private static class CalculosRepeticiones {
        private final int totalNormales;
        private final int totalAnormales;
        private final int totalDuras;
        private final int totalFrescas;
        private final int totalMuertas;
        private final int totalGeneral;

        public CalculosRepeticiones(int totalNormales, int totalAnormales, int totalDuras,
                                   int totalFrescas, int totalMuertas, int totalGeneral) {
            this.totalNormales = totalNormales;
            this.totalAnormales = totalAnormales;
            this.totalDuras = totalDuras;
            this.totalFrescas = totalFrescas;
            this.totalMuertas = totalMuertas;
            this.totalGeneral = totalGeneral;
        }

        public int getTotalNormales() { return totalNormales; }
        public int getTotalAnormales() { return totalAnormales; }
        public int getTotalDuras() { return totalDuras; }
        public int getTotalFrescas() { return totalFrescas; }
        public int getTotalMuertas() { return totalMuertas; }
        public int getTotalGeneral() { return totalGeneral; }
    }

    // Mapear de RequestDTO a Entity
    private ContGerm mapearSolicitudAEntidad(ContGermRequestDTO solicitud, Germinacion germinacion) {
        ContGerm contGerm = new ContGerm();
        contGerm.setTotal(solicitud.getTotal());
        contGerm.setPromedioConRedondeo(solicitud.getPromedioConRedondeo());
        contGerm.setGerminacion(germinacion);
        
        // Inicializar listas vacías
        contGerm.setRepGerm(new ArrayList<>());
        contGerm.setValoresGerm(new ArrayList<>());
        
        return contGerm;
    }

    // Actualizar Entity desde RequestDTO
    private void actualizarEntidadDesdeSolicitud(ContGerm contGerm, ContGermRequestDTO solicitud) {
        contGerm.setTotal(solicitud.getTotal());
        contGerm.setPromedioConRedondeo(solicitud.getPromedioConRedondeo());
        // La germinación asociada no se cambia en actualizaciones
    }

    // Mapear de Entity a DTO
    private ContGermDTO mapearEntidadADTO(ContGerm contGerm) {
        ContGermDTO dto = new ContGermDTO();
        dto.setContGermID(contGerm.getContGermID());
        dto.setTotal(contGerm.getTotal());
        dto.setPromedioConRedondeo(contGerm.getPromedioConRedondeo());
        
        // Mapear repeticiones si existen
        if (contGerm.getRepGerm() != null) {
            dto.setRepGerm(contGerm.getRepGerm()); // Por ahora mantenemos la entidad directa
        }
        
        // Mapear valores de germinación (se gestionan por separado)
        // dto.setValoresGerm se manejará mediante endpoints separados
        
        return dto;
    }
}