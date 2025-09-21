package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;

@Service
public class RepGermService {

    @Autowired
    private RepGermRepository repGermRepository;
    
    @Autowired
    private TablaGermRepository tablaGermRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear nueva repetición asociada a una tabla
    public RepGermDTO crearRepGerm(Long tablaGermId, RepGermRequestDTO solicitud) {
        try {
            System.out.println("Creando repetición para tabla ID: " + tablaGermId);
            
            // Validar que la tabla existe
            TablaGerm tablaGerm = entityManager.find(TablaGerm.class, tablaGermId);
            if (tablaGerm == null) {
                throw new RuntimeException("Tabla no encontrada con ID: " + tablaGermId);
            }
            
            // Validar que la tabla no esté finalizada
            if (tablaGerm.getFinalizada() != null && tablaGerm.getFinalizada()) {
                throw new RuntimeException("No se pueden agregar repeticiones a una tabla finalizada");
            }
            
            // Validar número máximo de repeticiones permitidas
            if (tablaGerm.getGerminacion() != null && tablaGerm.getGerminacion().getNumeroRepeticiones() != null) {
                long repeticionesExistentes = repGermRepository.countByTablaGermId(tablaGermId);
                if (repeticionesExistentes >= tablaGerm.getGerminacion().getNumeroRepeticiones()) {
                    throw new RuntimeException("No se pueden crear más repeticiones. Máximo permitido: " + 
                        tablaGerm.getGerminacion().getNumeroRepeticiones());
                }
            }
            
            // Crear la repetición
            RepGerm repGerm = mapearSolicitudAEntidad(solicitud, tablaGerm);
            RepGerm repGermGuardada = repGermRepository.save(repGerm);
            
            // Actualizar totales de la tabla padre cuando se crea una nueva repetición
            actualizarTotalesTablaGerm(tablaGerm);
            
            System.out.println("Repetición creada exitosamente con ID: " + repGermGuardada.getRepGermID());
            return mapearEntidadADTO(repGermGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear repetición: " + e.getMessage());
            throw new RuntimeException("Error al crear la repetición: " + e.getMessage());
        }
    }

    // Obtener repetición por ID
    public RepGermDTO obtenerRepGermPorId(Long id) {
        Optional<RepGerm> repGerm = repGermRepository.findById(id);
        if (repGerm.isPresent()) {
            return mapearEntidadADTO(repGerm.get());
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Actualizar repetición
    public RepGermDTO actualizarRepGerm(Long id, RepGermRequestDTO solicitud) {
        Optional<RepGerm> repGermExistente = repGermRepository.findById(id);
        
        if (repGermExistente.isPresent()) {
            RepGerm repGerm = repGermExistente.get();
            
            actualizarEntidadDesdeSolicitud(repGerm, solicitud);
            RepGerm repGermActualizada = repGermRepository.save(repGerm);
            
            // Actualizar totales de la tabla padre cuando se actualiza una repetición
            actualizarTotalesTablaGerm(repGermActualizada.getTablaGerm());
            
            return mapearEntidadADTO(repGermActualizada);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Eliminar repetición (eliminar realmente, no cambio de estado)
    public void eliminarRepGerm(Long id) {
        Optional<RepGerm> repGermExistente = repGermRepository.findById(id);
        
        if (repGermExistente.isPresent()) {
            RepGerm repGerm = repGermExistente.get();
            
            repGermRepository.deleteById(id);
            
            System.out.println("Repetición eliminada con ID: " + id);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    // Obtener todas las repeticiones de una tabla
    public List<RepGermDTO> obtenerRepeticionesPorTabla(Long tablaGermId) {
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGermId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Contar repeticiones de una tabla
    public Long contarRepeticionesPorTabla(Long tablaGermId) {
        return repGermRepository.countByTablaGermId(tablaGermId);
    }

    // Mapear de RequestDTO a Entity
    private RepGerm mapearSolicitudAEntidad(RepGermRequestDTO solicitud, TablaGerm tablaGerm) {
        RepGerm repGerm = new RepGerm();
        repGerm.setNumRep(solicitud.getNumRep());
        
        // Inicializar lista normales con el número de conteos definido en la germinación
        Integer numeroConteos = (tablaGerm.getGerminacion() != null && tablaGerm.getGerminacion().getNumeroConteos() != null) 
            ? tablaGerm.getGerminacion().getNumeroConteos() 
            : 1; // valor por defecto
            
        List<Integer> normalesInicializadas = new ArrayList<>(Collections.nCopies(numeroConteos, 0));
        
        // Si el usuario envió valores para normales, preservar su posición y completar con 0 el resto
        if (solicitud.getNormales() != null && !solicitud.getNormales().isEmpty()) {
            for (int i = 0; i < Math.min(solicitud.getNormales().size(), numeroConteos); i++) {
                if (solicitud.getNormales().get(i) != null) {
                    normalesInicializadas.set(i, solicitud.getNormales().get(i));
                }
            }
        }
        
        repGerm.setNormales(normalesInicializadas);
        
        repGerm.setAnormales(solicitud.getAnormales() != null ? solicitud.getAnormales() : 0);
        repGerm.setDuras(solicitud.getDuras() != null ? solicitud.getDuras() : 0);
        repGerm.setFrescas(solicitud.getFrescas() != null ? solicitud.getFrescas() : 0);
        repGerm.setMuertas(solicitud.getMuertas() != null ? solicitud.getMuertas() : 0);
        
        // Calcular total automáticamente
        Integer totalCalculado = calcularTotal(normalesInicializadas, repGerm.getAnormales(), 
                                             repGerm.getDuras(), repGerm.getFrescas(), repGerm.getMuertas());
        repGerm.setTotal(totalCalculado);
        
        repGerm.setTablaGerm(tablaGerm);
        
        // Validar que el total no supere numSemillasPRep
        if (tablaGerm.getNumSemillasPRep() != null && totalCalculado > tablaGerm.getNumSemillasPRep()) {
            throw new RuntimeException("El total de la repetición (" + totalCalculado + 
                ") no puede superar el número de semillas por repetición (" + tablaGerm.getNumSemillasPRep() + ")");
        }
        
        return repGerm;
    }
    
    // Método para calcular el total de una repetición
    private Integer calcularTotal(List<Integer> normales, Integer anormales, Integer duras, Integer frescas, Integer muertas) {
        int total = 0;
        
        // Sumar todos los valores de normales
        if (normales != null) {
            for (Integer normal : normales) {
                if (normal != null) {
                    total += normal;
                }
            }
        }
        
        // Sumar los demás campos
        total += (anormales != null ? anormales : 0);
        total += (duras != null ? duras : 0);
        total += (frescas != null ? frescas : 0);
        total += (muertas != null ? muertas : 0);
        
        return total;
    }

    // Actualizar Entity desde RequestDTO
    private void actualizarEntidadDesdeSolicitud(RepGerm repGerm, RepGermRequestDTO solicitud) {
        repGerm.setNumRep(solicitud.getNumRep());
        
        // Gestionar la lista normales preservando el tamaño según numeroConteos
        Integer numeroConteos = (repGerm.getTablaGerm().getGerminacion() != null && 
                               repGerm.getTablaGerm().getGerminacion().getNumeroConteos() != null) 
            ? repGerm.getTablaGerm().getGerminacion().getNumeroConteos() 
            : 1;
        
        List<Integer> normalesActualizadas = new ArrayList<>(Collections.nCopies(numeroConteos, 0));
        
        // Si el usuario envió valores para normales, preservar su posición y completar con 0 el resto
        if (solicitud.getNormales() != null && !solicitud.getNormales().isEmpty()) {
            for (int i = 0; i < Math.min(solicitud.getNormales().size(), numeroConteos); i++) {
                if (solicitud.getNormales().get(i) != null) {
                    normalesActualizadas.set(i, solicitud.getNormales().get(i));
                }
            }
        }
        
        repGerm.setNormales(normalesActualizadas);
        
        repGerm.setAnormales(solicitud.getAnormales() != null ? solicitud.getAnormales() : 0);
        repGerm.setDuras(solicitud.getDuras() != null ? solicitud.getDuras() : 0);
        repGerm.setFrescas(solicitud.getFrescas() != null ? solicitud.getFrescas() : 0);
        repGerm.setMuertas(solicitud.getMuertas() != null ? solicitud.getMuertas() : 0);
        
        // Calcular total automáticamente
        Integer totalCalculado = calcularTotal(normalesActualizadas, repGerm.getAnormales(), 
                                             repGerm.getDuras(), repGerm.getFrescas(), repGerm.getMuertas());
        repGerm.setTotal(totalCalculado);
        
        // Validar que el total no supere numSemillasPRep
        if (repGerm.getTablaGerm().getNumSemillasPRep() != null && totalCalculado > repGerm.getTablaGerm().getNumSemillasPRep()) {
            throw new RuntimeException("El total de la repetición (" + totalCalculado + 
                ") no puede superar el número de semillas por repetición (" + repGerm.getTablaGerm().getNumSemillasPRep() + ")");
        }
        
        // El total se calcula automáticamente, no se toma del DTO
        repGerm.setTotal(totalCalculado);
        // La tabla asociada no se cambia en actualizaciones
    }

    // Mapear de Entity a DTO
    private RepGermDTO mapearEntidadADTO(RepGerm repGerm) {
        RepGermDTO dto = new RepGermDTO();
        dto.setRepGermID(repGerm.getRepGermID());
        dto.setNumRep(repGerm.getNumRep());
        dto.setNormales(repGerm.getNormales());
        dto.setAnormales(repGerm.getAnormales());
        dto.setDuras(repGerm.getDuras());
        dto.setFrescas(repGerm.getFrescas());
        dto.setMuertas(repGerm.getMuertas());
        dto.setTotal(repGerm.getTotal());
        
        // Incluir ID de la tabla asociada
        if (repGerm.getTablaGerm() != null) {
            dto.setTablaGermId(repGerm.getTablaGerm().getTablaGermID());
        }
        
        return dto;
    }
    
    // Actualizar totales de la tabla padre cuando cambia una repetición
    private void actualizarTotalesTablaGerm(TablaGerm tablaGerm) {
        // Obtener todas las repeticiones de esta tabla
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGerm.getTablaGermID());
        
        // Calcular total como suma de todos los totales de repeticiones
        int totalCalculado = repeticiones.stream()
            .mapToInt(rep -> rep.getTotal() != null ? rep.getTotal() : 0)
            .sum();
            
        tablaGerm.setTotal(totalCalculado);
        
        // Verificar si todas las repeticiones están completas para calcular promedio
        if (todasLasRepeticionesCompletas(tablaGerm, repeticiones)) {
            calcularPromediosSinRedondeo(tablaGerm, repeticiones);
        }
        
        // Guardar la tabla actualizada
        tablaGermRepository.save(tablaGerm);
    }
    
    // Verificar si todas las repeticiones están completas
    private boolean todasLasRepeticionesCompletas(TablaGerm tablaGerm, List<RepGerm> repeticiones) {
        if (tablaGerm.getGerminacion() == null || tablaGerm.getGerminacion().getNumeroRepeticiones() == null) {
            return false;
        }
        
        // Verificar que tenemos el número esperado de repeticiones
        if (repeticiones.size() < tablaGerm.getGerminacion().getNumeroRepeticiones()) {
            return false;
        }
        
        // Verificar que todas las repeticiones tienen datos completos
        return repeticiones.stream().allMatch(rep -> 
            rep.getTotal() != null && rep.getTotal() > 0
        );
    }
    
    // Calcular promedios sin redondeo automáticamente
    // 5 promedios: normales (suma de todos los valores de todas las listas), anormales, duras, frescas, muertas
    private void calcularPromediosSinRedondeo(TablaGerm tablaGerm, List<RepGerm> repeticiones) {
        if (repeticiones.isEmpty()) {
            tablaGerm.setPromedioSinRedondeo(new ArrayList<>());
            return;
        }
        
        int numRepeticiones = repeticiones.size();
        List<BigDecimal> promedios = new ArrayList<>();
        
        // 1. Promedio de normales (suma de todos los valores de todas las listas de normales)
        int sumaNormales = repeticiones.stream()
            .flatMapToInt(rep -> rep.getNormales() != null ? rep.getNormales().stream().mapToInt(Integer::intValue) : java.util.stream.IntStream.empty())
            .sum();
        BigDecimal promedioNormales = BigDecimal.valueOf(sumaNormales).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioNormales);
        
        // 2. Promedio de anormales
        int sumaAnormales = repeticiones.stream()
            .mapToInt(rep -> rep.getAnormales() != null ? rep.getAnormales() : 0)
            .sum();
        BigDecimal promedioAnormales = BigDecimal.valueOf(sumaAnormales).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioAnormales);
        
        // 3. Promedio de duras
        int sumaDuras = repeticiones.stream()
            .mapToInt(rep -> rep.getDuras() != null ? rep.getDuras() : 0)
            .sum();
        BigDecimal promedioDuras = BigDecimal.valueOf(sumaDuras).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioDuras);
        
        // 4. Promedio de frescas
        int sumaFrescas = repeticiones.stream()
            .mapToInt(rep -> rep.getFrescas() != null ? rep.getFrescas() : 0)
            .sum();
        BigDecimal promedioFrescas = BigDecimal.valueOf(sumaFrescas).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioFrescas);
        
        // 5. Promedio de muertas
        int sumaMuertas = repeticiones.stream()
            .mapToInt(rep -> rep.getMuertas() != null ? rep.getMuertas() : 0)
            .sum();
        BigDecimal promedioMuertas = BigDecimal.valueOf(sumaMuertas).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioMuertas);
        
        tablaGerm.setPromedioSinRedondeo(promedios);
    }
}