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
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ValoresGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

@Service
public class TablaGermService {

    @Autowired
    private TablaGermRepository tablaGermRepository;

    @Autowired
    private ValoresGermRepository valoresGermRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Crear nueva tabla asociada a una germinación
    public TablaGermDTO crearTablaGerm(Long germinacionId, TablaGermRequestDTO solicitud) {
        try {
            Germinacion germinacion = entityManager.find(Germinacion.class, germinacionId);
            if (germinacion == null) {
                throw new RuntimeException("Germinación no encontrada con ID: " + germinacionId);
            }

            TablaGerm tablaGerm = mapearSolicitudAEntidad(solicitud, germinacion);
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
            TablaGerm tablaGermActualizada = tablaGermRepository.save(tablaGerm);

            // Calcular automáticamente promedios sin redondeo
            calcularPromediosSinRedondeo(tablaGermActualizada);

            // Actualizar valores INIA con los porcentajes con redondeo ingresados
            actualizarValoresInia(tablaGermActualizada);

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

    // Calcular automáticamente los promedios sin redondeo basados en las repeticiones
    private void calcularPromediosSinRedondeo(TablaGerm tablaGerm) {
        if (tablaGerm.getRepGerm() == null || tablaGerm.getRepGerm().isEmpty()) {
            return;
        }

        List<BigDecimal> promedios = new ArrayList<>();
        
        // Calcular promedio de normales
        double promedioNormales = tablaGerm.getRepGerm().stream()
            .flatMap(rep -> rep.getNormales().stream())
            .mapToDouble(Integer::doubleValue)
            .average()
            .orElse(0.0);
        promedios.add(BigDecimal.valueOf(promedioNormales));

        // Calcular promedio de anormales
        double promedioAnormales = tablaGerm.getRepGerm().stream()
            .mapToDouble(rep -> rep.getAnormales() != null ? rep.getAnormales().doubleValue() : 0.0)
            .average()
            .orElse(0.0);
        promedios.add(BigDecimal.valueOf(promedioAnormales));

        // Calcular promedio de duras
        double promedioDuras = tablaGerm.getRepGerm().stream()
            .mapToDouble(rep -> rep.getDuras() != null ? rep.getDuras().doubleValue() : 0.0)
            .average()
            .orElse(0.0);
        promedios.add(BigDecimal.valueOf(promedioDuras));

        // Calcular promedio de frescas
        double promedioFrescas = tablaGerm.getRepGerm().stream()
            .mapToDouble(rep -> rep.getFrescas() != null ? rep.getFrescas().doubleValue() : 0.0)
            .average()
            .orElse(0.0);
        promedios.add(BigDecimal.valueOf(promedioFrescas));

        // Calcular promedio de muertas
        double promedioMuertas = tablaGerm.getRepGerm().stream()
            .mapToDouble(rep -> rep.getMuertas() != null ? rep.getMuertas().doubleValue() : 0.0)
            .average()
            .orElse(0.0);
        promedios.add(BigDecimal.valueOf(promedioMuertas));

        tablaGerm.setPromedioSinRedondeo(promedios);
        tablaGermRepository.save(tablaGerm);
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
        tablaGerm.setTotal(solicitud.getTotal());
        tablaGerm.setPromedioSinRedondeo(solicitud.getPromedioSinRedondeo());
        
        // Campos de porcentaje con redondeo
        tablaGerm.setPorcentajeNormalesConRedondeo(solicitud.getPorcentajeNormalesConRedondeo());
        tablaGerm.setPorcentajeAnormalesConRedondeo(solicitud.getPorcentajeAnormalesConRedondeo());
        tablaGerm.setPorcentajeDurasConRedondeo(solicitud.getPorcentajeDurasConRedondeo());
        tablaGerm.setPorcentajeFrescasConRedondeo(solicitud.getPorcentajeFrescasConRedondeo());
        tablaGerm.setPorcentajeMuertasConRedondeo(solicitud.getPorcentajeMuertasConRedondeo());
        
        tablaGerm.setFechaFinal(solicitud.getFechaFinal());
        
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
        tablaGerm.setTotal(solicitud.getTotal());
        tablaGerm.setPromedioSinRedondeo(solicitud.getPromedioSinRedondeo());
        
        // Campos de porcentaje con redondeo
        tablaGerm.setPorcentajeNormalesConRedondeo(solicitud.getPorcentajeNormalesConRedondeo());
        tablaGerm.setPorcentajeAnormalesConRedondeo(solicitud.getPorcentajeAnormalesConRedondeo());
        tablaGerm.setPorcentajeDurasConRedondeo(solicitud.getPorcentajeDurasConRedondeo());
        tablaGerm.setPorcentajeFrescasConRedondeo(solicitud.getPorcentajeFrescasConRedondeo());
        tablaGerm.setPorcentajeMuertasConRedondeo(solicitud.getPorcentajeMuertasConRedondeo());
        
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
        dto.setRepGerm(tablaGerm.getRepGerm());
        dto.setTotal(tablaGerm.getTotal());
        dto.setPromedioSinRedondeo(tablaGerm.getPromedioSinRedondeo());
        
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
}