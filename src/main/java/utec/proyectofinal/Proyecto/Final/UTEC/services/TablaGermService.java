package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ValoresGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

@Service
public class TablaGermService {

    @Autowired
    private TablaGermRepository tablaGermRepository;

    @Autowired
    private RepGermRepository repGermRepository;

    @Autowired
    private ValoresGermRepository valoresGermRepository;

    @Autowired
    private GerminacionRepository germinacionRepository;

    // Crear nueva tabla asociada a una germinación
    public TablaGermDTO crearTablaGerm(Long germinacionId, TablaGermRequestDTO solicitud) {
        try {
            Optional<Germinacion> germinacionOpt = germinacionRepository.findById(germinacionId);
            if (germinacionOpt.isEmpty()) {
                throw new RuntimeException("Germinación no encontrada con ID: " + germinacionId);
            }
            
            Germinacion germinacion = germinacionOpt.get();

            // Validar que la tabla anterior esté finalizada (si existe alguna tabla)
            List<TablaGerm> tablasExistentes = tablaGermRepository.findByGerminacionId(germinacionId);
            if (!tablasExistentes.isEmpty()) {
                // Verificar que todas las tablas existentes estén finalizadas
                boolean algunaTablaNoFinalizada = tablasExistentes.stream()
                    .anyMatch(tabla -> tabla.getFinalizada() == null || !tabla.getFinalizada());
                
                if (algunaTablaNoFinalizada) {
                    throw new RuntimeException("No se puede crear una nueva tabla hasta que todas las tablas anteriores estén finalizadas");
                }
            }

            TablaGerm tablaGerm = mapearSolicitudAEntidad(solicitud, germinacion);
            
            // Calcular total automáticamente desde las repeticiones existentes
            calcularYActualizarTotales(tablaGerm);
            
            TablaGerm tablaGermGuardada = tablaGermRepository.save(tablaGerm);

            // Crear ValoresGerm para INIA e INASE con valores iniciales en 0
            crearValoresGermAutomaticos(tablaGermGuardada);

            return mapearEntidadADTO(tablaGermGuardada);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear tabla de germinación: " + e.getMessage(), e);
        }
    }

    // Obtener tabla por ID
    public TablaGermDTO obtenerTablaGermPorId(Long id) {
        Optional<TablaGerm> tablaGerm = tablaGermRepository.findById(id);
        if (tablaGerm.isPresent()) {
            return mapearEntidadADTO(tablaGerm.get());
        } else {
            throw new RuntimeException("Tabla de germinación no encontrada con ID: " + id);
        }
    }

    // Actualizar tabla
    public TablaGermDTO actualizarTablaGerm(Long id, TablaGermRequestDTO solicitud) {
        Optional<TablaGerm> tablaGermExistente = tablaGermRepository.findById(id);
        
        if (tablaGermExistente.isPresent()) {
            TablaGerm tablaGerm = tablaGermExistente.get();
            
            actualizarEntidadDesdeSolicitud(tablaGerm, solicitud);
            
            // Calcular total automáticamente desde las repeticiones
            calcularYActualizarTotales(tablaGerm);
            
            TablaGerm tablaGermActualizada = tablaGermRepository.save(tablaGerm);

            return mapearEntidadADTO(tablaGermActualizada);
        } else {
            throw new RuntimeException("Tabla de germinación no encontrada con ID: " + id);
        }
    }

    // Eliminar tabla (eliminar realmente)
    public void eliminarTablaGerm(Long id) {
        Optional<TablaGerm> tablaGermExistente = tablaGermRepository.findById(id);
        
        if (tablaGermExistente.isPresent()) {
            // Eliminar valores asociados primero
            List<ValoresGerm> valores = valoresGermRepository.findByTablaGermId(id);
            valoresGermRepository.deleteAll(valores);
            
            tablaGermRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tabla de germinación no encontrada con ID: " + id);
        }
    }

    // Obtener todas las tablas de una germinación
    public List<TablaGermDTO> obtenerTablasPorGerminacion(Long germinacionId) {
        List<TablaGerm> tablas = tablaGermRepository.findByGerminacionId(germinacionId);
        return tablas.stream().map(this::mapearEntidadADTO).collect(Collectors.toList());
    }

    // Contar tablas de una germinación
    public Long contarTablasPorGerminacion(Long germinacionId) {
        return tablaGermRepository.countByGerminacionId(germinacionId);
    }

    // Actualizar solo los porcentajes con redondeo
    public TablaGermDTO actualizarPorcentajes(Long tablaId, PorcentajesRedondeoRequestDTO solicitud) {
        Optional<TablaGerm> tablaExistente = tablaGermRepository.findById(tablaId);
        
        if (tablaExistente.isPresent()) {
            TablaGerm tabla = tablaExistente.get();
            
           
            
            // Validar que se puedan ingresar porcentajes
            if (!puedeIngresarPorcentajes(tablaId)) {
                throw new RuntimeException("No se pueden ingresar porcentajes hasta completar todas las repeticiones");
            }
            
            // Actualizar solo los porcentajes
            tabla.setPorcentajeNormalesConRedondeo(solicitud.getPorcentajeNormalesConRedondeo());
            tabla.setPorcentajeAnormalesConRedondeo(solicitud.getPorcentajeAnormalesConRedondeo());
            tabla.setPorcentajeDurasConRedondeo(solicitud.getPorcentajeDurasConRedondeo());
            tabla.setPorcentajeFrescasConRedondeo(solicitud.getPorcentajeFrescasConRedondeo());
            tabla.setPorcentajeMuertasConRedondeo(solicitud.getPorcentajeMuertasConRedondeo());
            
            TablaGerm tablaActualizada = tablaGermRepository.save(tabla);
            
            // Actualizar valores INIA con los porcentajes con redondeo ingresados
            actualizarValoresInia(tablaActualizada);
            
            return mapearEntidadADTO(tablaActualizada);
        } else {
            throw new RuntimeException("Tabla no encontrada con ID: " + tablaId);
        }
    }

    // Finalizar tabla (solo si todas las repeticiones están completas)
    public TablaGermDTO finalizarTabla(Long tablaId) {
        Optional<TablaGerm> tablaExistente = tablaGermRepository.findById(tablaId);
        
        if (tablaExistente.isPresent()) {
            TablaGerm tabla = tablaExistente.get();
            
            // Validar que no esté ya finalizada
            if (tabla.getFinalizada() != null && tabla.getFinalizada()) {
                throw new RuntimeException("La tabla ya está finalizada");
            }
            
            // Validar que todas las repeticiones estén completas
            if (!todasLasRepeticionesCompletas(tabla)) {
                throw new RuntimeException("No se puede finalizar la tabla. Faltan repeticiones por completar.");
            }
            
            // Validar que los campos de porcentaje con redondeo estén ingresados
            if (!camposPorcentajeCompletos(tabla)) {
                throw new RuntimeException("No se puede finalizar la tabla. Debe ingresar todos los porcentajes con redondeo.");
            }
            
            // Marcar como finalizada
            tabla.setFinalizada(true);
            TablaGerm tablaActualizada = tablaGermRepository.save(tabla);
            
            System.out.println("Tabla finalizada exitosamente con ID: " + tablaId);
            return mapearEntidadADTO(tablaActualizada);
        } else {
            throw new RuntimeException("Tabla no encontrada con ID: " + tablaId);
        }
    }

    // Validar que todas las repeticiones esperadas estén completas
    private boolean todasLasRepeticionesCompletas(TablaGerm tabla) {
        if (tabla.getGerminacion() == null || tabla.getGerminacion().getNumeroRepeticiones() == null) {
            return false;
        }
        
        // Contar repeticiones existentes
        List<RepGerm> repeticiones = tabla.getRepGerm();
        if (repeticiones == null) {
            return false;
        }
        
        int repeticionesEsperadas = tabla.getGerminacion().getNumeroRepeticiones();
        int repeticionesExistentes = repeticiones.size();
        
        // Verificar que tengamos el número esperado de repeticiones
        if (repeticionesExistentes != repeticionesEsperadas) {
            return false;
        }
        
        // Verificar que cada repetición tenga todos sus conteos completos
        Integer numeroConteos = tabla.getGerminacion().getNumeroConteos();
        if (numeroConteos == null) {
            return false;
        }
        
        for (RepGerm repeticion : repeticiones) {
            if (repeticion.getNormales() == null || repeticion.getNormales().size() != numeroConteos) {
                return false;
            }
            
            // Verificar que todos los valores estén completados (no sean null)
            // Los valores 0 son válidos ya que representan conteos no ingresados
            for (Integer normal : repeticion.getNormales()) {
                if (normal == null) {
                    return false;
                }
            }
            
            // Verificar que al menos algunos valores sean mayores a 0 (que haya datos reales ingresados)
            boolean tieneValoresIngresados = repeticion.getNormales().stream()
                .anyMatch(valor -> valor != null && valor > 0);
            
            if (!tieneValoresIngresados) {
                return false; // La repetición no tiene datos reales ingresados
            }
        }
        
        return true;
    }

    // Validar que todos los campos de porcentaje con redondeo estén ingresados
    private boolean camposPorcentajeCompletos(TablaGerm tabla) {
        return tabla.getPorcentajeNormalesConRedondeo() != null &&
               tabla.getPorcentajeAnormalesConRedondeo() != null &&
               tabla.getPorcentajeDurasConRedondeo() != null &&
               tabla.getPorcentajeFrescasConRedondeo() != null &&
               tabla.getPorcentajeMuertasConRedondeo() != null;
    }

    // Validar que se puedan ingresar los porcentajes (todas las repeticiones completas)
    public boolean puedeIngresarPorcentajes(Long tablaId) {
        Optional<TablaGerm> tablaExistente = tablaGermRepository.findById(tablaId);
        
        if (tablaExistente.isPresent()) {
            TablaGerm tabla = tablaExistente.get();
            
            // Los porcentajes se pueden editar incluso si la tabla está finalizada
            // Solo verificamos que todas las repeticiones estén completas
            return todasLasRepeticionesCompletas(tabla);
        }
        
        return false;
    }

    // Crear ValoresGerm automáticos para INIA e INASE con valores en 0
    private void crearValoresGermAutomaticos(TablaGerm tablaGerm) {
        // Crear valores para INIA
        ValoresGerm valoresInia = new ValoresGerm();
        valoresInia.setInstituto(Instituto.INIA);
        valoresInia.setTablaGerm(tablaGerm);
        inicializarValoresEnCero(valoresInia);
        valoresGermRepository.save(valoresInia);

        // Crear valores para INASE
        ValoresGerm valoresInase = new ValoresGerm();
        valoresInase.setInstituto(Instituto.INASE);
        valoresInase.setTablaGerm(tablaGerm);
        inicializarValoresEnCero(valoresInase);
        valoresGermRepository.save(valoresInase);
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



    // Actualizar valores de INIA con los porcentajes con redondeo ingresados manualmente
    private void actualizarValoresInia(TablaGerm tablaGerm) {
        Optional<ValoresGerm> valoresIniaOpt = valoresGermRepository.findByTablaGermIdAndInstituto(
            tablaGerm.getTablaGermID(), Instituto.INIA);
        
        if (valoresIniaOpt.isPresent()) {
            ValoresGerm valoresInia = valoresIniaOpt.get();
            
            // Los valores de INIA son iguales a los porcentajes con redondeo
            if (tablaGerm.getPorcentajeNormalesConRedondeo() != null) {
                valoresInia.setNormales(tablaGerm.getPorcentajeNormalesConRedondeo());
            }
            if (tablaGerm.getPorcentajeAnormalesConRedondeo() != null) {
                valoresInia.setAnormales(tablaGerm.getPorcentajeAnormalesConRedondeo());
            }
            if (tablaGerm.getPorcentajeDurasConRedondeo() != null) {
                valoresInia.setDuras(tablaGerm.getPorcentajeDurasConRedondeo());
            }
            if (tablaGerm.getPorcentajeFrescasConRedondeo() != null) {
                valoresInia.setFrescas(tablaGerm.getPorcentajeFrescasConRedondeo());
            }
            if (tablaGerm.getPorcentajeMuertasConRedondeo() != null) {
                valoresInia.setMuertas(tablaGerm.getPorcentajeMuertasConRedondeo());
            }
            
            // Calcular germinación como la suma de normales
            valoresInia.setGerminacion(tablaGerm.getPorcentajeNormalesConRedondeo() != null ? 
                tablaGerm.getPorcentajeNormalesConRedondeo() : BigDecimal.ZERO);
            
            valoresGermRepository.save(valoresInia);
        }
    }

    // Mapear de RequestDTO a Entity
    private TablaGerm mapearSolicitudAEntidad(TablaGermRequestDTO solicitud, Germinacion germinacion) {
        TablaGerm tablaGerm = new TablaGerm();
        
        tablaGerm.setGerminacion(germinacion);
        // total se calcula automáticamente cuando se agreguen repeticiones
        tablaGerm.setTotal(0);
        
        tablaGerm.setFechaFinal(solicitud.getFechaFinal());
        
        // Campo de control para finalización (por defecto false)
        tablaGerm.setFinalizada(false);
        
        // Campos movidos desde Germinacion
        tablaGerm.setTratamiento(solicitud.getTratamiento());
        tablaGerm.setProductoYDosis(solicitud.getProductoYDosis());
        tablaGerm.setNumSemillasPRep(solicitud.getNumSemillasPRep());
        tablaGerm.setMetodo(solicitud.getMetodo());
        tablaGerm.setTemperatura(solicitud.getTemperatura());
        tablaGerm.setPrefrio(solicitud.getPrefrio());
        tablaGerm.setPretratamiento(solicitud.getPretratamiento());
        
        return tablaGerm;
    }

    // Actualizar Entity desde RequestDTO
    private void actualizarEntidadDesdeSolicitud(TablaGerm tablaGerm, TablaGermRequestDTO solicitud) {
        // total y promedioSinRedondeo se calculan automáticamente
        
        tablaGerm.setFechaFinal(solicitud.getFechaFinal());
        
        // Campos movidos desde Germinacion
        tablaGerm.setTratamiento(solicitud.getTratamiento());
        tablaGerm.setProductoYDosis(solicitud.getProductoYDosis());
        tablaGerm.setNumSemillasPRep(solicitud.getNumSemillasPRep());
        tablaGerm.setMetodo(solicitud.getMetodo());
        tablaGerm.setTemperatura(solicitud.getTemperatura());
        tablaGerm.setPrefrio(solicitud.getPrefrio());
        tablaGerm.setPretratamiento(solicitud.getPretratamiento());
    }

    // Mapear de Entity a DTO
    private TablaGermDTO mapearEntidadADTO(TablaGerm tablaGerm) {
        TablaGermDTO dto = new TablaGermDTO();
        
        dto.setTablaGermID(tablaGerm.getTablaGermID());
        
        // Mapear RepGerm entities a RepGermDTO
        if (tablaGerm.getRepGerm() != null) {
            List<RepGermDTO> repGermDTOs = tablaGerm.getRepGerm().stream()
                .map(this::mapearRepGermADTO)
                .collect(Collectors.toList());
            dto.setRepGerm(repGermDTOs);
        }
        
        dto.setTotal(tablaGerm.getTotal());
        dto.setPromedioSinRedondeo(tablaGerm.getPromedioSinRedondeo());
        dto.setPromediosSinRedPorConteo(tablaGerm.getPromediosSinRedPorConteo());
        
        // Campos de porcentaje con redondeo
        dto.setPorcentajeNormalesConRedondeo(tablaGerm.getPorcentajeNormalesConRedondeo());
        dto.setPorcentajeAnormalesConRedondeo(tablaGerm.getPorcentajeAnormalesConRedondeo());
        dto.setPorcentajeDurasConRedondeo(tablaGerm.getPorcentajeDurasConRedondeo());
        dto.setPorcentajeFrescasConRedondeo(tablaGerm.getPorcentajeFrescasConRedondeo());
        dto.setPorcentajeMuertasConRedondeo(tablaGerm.getPorcentajeMuertasConRedondeo());
        
        // Mapear ValoresGerm
        if (tablaGerm.getValoresGerm() != null) {
            List<ValoresGermDTO> valoresDTO = tablaGerm.getValoresGerm().stream()
                .map(this::mapearValoresGermADTO)
                .collect(Collectors.toList());
            dto.setValoresGerm(valoresDTO);
        }
        
        dto.setFechaFinal(tablaGerm.getFechaFinal());
        
        // Campo de control para finalización
        dto.setFinalizada(tablaGerm.getFinalizada());
        
        // Campos movidos desde Germinacion
        dto.setTratamiento(tablaGerm.getTratamiento());
        dto.setProductoYDosis(tablaGerm.getProductoYDosis());
        dto.setNumSemillasPRep(tablaGerm.getNumSemillasPRep());
        dto.setMetodo(tablaGerm.getMetodo());
        dto.setTemperatura(tablaGerm.getTemperatura());
        dto.setPrefrio(tablaGerm.getPrefrio());
        dto.setPretratamiento(tablaGerm.getPretratamiento());
        
        return dto;
    }
    
    // Mapear RepGerm a DTO
    private RepGermDTO mapearRepGermADTO(RepGerm repGerm) {
        RepGermDTO dto = new RepGermDTO();
        dto.setRepGermID(repGerm.getRepGermID());
        dto.setNumRep(repGerm.getNumRep());
        dto.setNormales(repGerm.getNormales());
        dto.setAnormales(repGerm.getAnormales());
        dto.setDuras(repGerm.getDuras());
        dto.setFrescas(repGerm.getFrescas());
        dto.setMuertas(repGerm.getMuertas());
        dto.setTotal(repGerm.getTotal());
        dto.setTablaGermId(repGerm.getTablaGerm().getTablaGermID());
        return dto;
    }

    // Mapear ValoresGerm a DTO
    private ValoresGermDTO mapearValoresGermADTO(ValoresGerm valores) {
        ValoresGermDTO dto = new ValoresGermDTO();
        dto.setValoresGermID(valores.getValoresGermID());
        dto.setInstituto(valores.getInstituto());
        dto.setNormales(valores.getNormales());
        dto.setAnormales(valores.getAnormales());
        dto.setDuras(valores.getDuras());
        dto.setFrescas(valores.getFrescas());
        dto.setMuertas(valores.getMuertas());
        dto.setGerminacion(valores.getGerminacion());
        dto.setTablaGermId(valores.getTablaGerm().getTablaGermID());
        return dto;
    }
    
    // Calcular y actualizar totales automáticamente
    private void calcularYActualizarTotales(TablaGerm tablaGerm) {
        if (tablaGerm.getTablaGermID() == null) {
            // Nueva tabla, total inicial será 0
            tablaGerm.setTotal(0);
            return;
        }
        
        // Obtener todas las repeticiones de esta tabla
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGerm.getTablaGermID());
        
        // El total es la suma de todos los totales de las repeticiones
        int totalCalculado = repeticiones.stream()
            .mapToInt(rep -> rep.getTotal() != null ? rep.getTotal().intValue() : 0)
            .sum();
            
        tablaGerm.setTotal(totalCalculado);
    }

}