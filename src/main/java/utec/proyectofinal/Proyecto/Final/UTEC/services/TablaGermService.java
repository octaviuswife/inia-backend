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
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

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

    @Autowired
    private AnalisisService analisisService;

    // Crear nueva tabla asociada a una germinación
    public TablaGermDTO crearTablaGerm(Long germinacionId, TablaGermRequestDTO solicitud) {
        try {
            Optional<Germinacion> germinacionOpt = germinacionRepository.findById(germinacionId);
            if (germinacionOpt.isEmpty()) {
                throw new RuntimeException("Germinación no encontrada con ID: " + germinacionId);
            }
            
            Germinacion germinacion = germinacionOpt.get();
            
            
            // Validar datos de la solicitud
            validarDatosTablaGerm(solicitud, germinacion);

            // Validar que la tabla anterior esté finalizada (si existe alguna tabla)
            List<TablaGerm> tablasExistentes = tablaGermRepository.findByGerminacionId(germinacionId);
            if (!tablasExistentes.isEmpty()) {
                // Verificar que todas las tablas existentes estén finalizadas
                boolean algunaTablaNoFinalizada = tablasExistentes.stream()
                    .anyMatch(tabla -> tabla.getFinalizada() == null || !tabla.getFinalizada());
                
                if (algunaTablaNoFinalizada) {
                    throw new RuntimeException("No se puede crear una nueva tabla hasta que todas las tablas anteriores estén finalizadas");
                }
                
            
            } else {
                // Si no hay tablas existentes, es la primera tabla: cambiar estado a EN_PROCESO
                germinacion.setEstado(Estado.EN_PROCESO);
                germinacionRepository.save(germinacion);
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
    
    
    
    /**
     * Validar datos de la tabla de germinación
     */
    private void validarDatosTablaGerm(TablaGermRequestDTO solicitud, Germinacion germinacion) {
        // Validar campos obligatorios
        if (solicitud.getFechaFinal() == null) {
            throw new RuntimeException("La fecha final es obligatoria");
        }
        
        if (solicitud.getTratamiento() == null || solicitud.getTratamiento().trim().isEmpty()) {
            throw new RuntimeException("El tratamiento es obligatorio");
        }
        
        if (solicitud.getMetodo() == null || solicitud.getMetodo().trim().isEmpty()) {
            throw new RuntimeException("El método es obligatorio");
        }
        
        if (solicitud.getNumSemillasPRep() == null || solicitud.getNumSemillasPRep() <= 0) {
            throw new RuntimeException("El número de semillas por repetición es obligatorio y debe ser mayor a 0");
        }
        
        if (solicitud.getTemperatura() == null || solicitud.getTemperatura().trim().isEmpty()) {
            throw new RuntimeException("La temperatura es obligatoria");
        }
        
        // Validar fechas de germinación
        if (solicitud.getFechaInicioGerm() == null) {
            throw new RuntimeException("La fecha de inicio de germinación es obligatoria");
        }
        
        if (solicitud.getFechaUltConteo() == null) {
            throw new RuntimeException("La fecha de último conteo es obligatoria");
        }
        
        // Validar que la fecha de último conteo sea posterior a la de inicio
        if (!solicitud.getFechaUltConteo().isAfter(solicitud.getFechaInicioGerm())) {
            throw new RuntimeException("La fecha de último conteo debe ser posterior a la fecha de inicio de germinación");
        }
        
        // Validar fechaFinal (debe estar entre fechaInicioGerm y fechaUltConteo, o después)
        if (solicitud.getFechaFinal().isBefore(solicitud.getFechaInicioGerm())) {
            throw new RuntimeException("La fecha final debe ser posterior o igual a la fecha de inicio de germinación");
        }
        
        if (solicitud.getFechaFinal().isBefore(solicitud.getFechaUltConteo())) {
            throw new RuntimeException("La fecha final debe ser igual o posterior a la fecha de último conteo");
        }
        
        // Validar parámetros de repeticiones y conteos
        if (solicitud.getNumeroRepeticiones() == null || solicitud.getNumeroRepeticiones() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones (mayor a 0)");
        }
        
        if (solicitud.getNumeroRepeticiones() < 1 || solicitud.getNumeroRepeticiones() > 20) {
            throw new RuntimeException("El número de repeticiones debe estar entre 1 y 20");
        }
        
        if (solicitud.getNumeroConteos() == null || solicitud.getNumeroConteos() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de conteos (mayor a 0)");
        }
        
        if (solicitud.getNumeroConteos() < 1 || solicitud.getNumeroConteos() > 15) {
            throw new RuntimeException("El número de conteos debe estar entre 1 y 15");
        }
        
        // Validar fechas de conteos
        if (solicitud.getFechaConteos() == null || solicitud.getFechaConteos().isEmpty()) {
            throw new RuntimeException("Debe especificar al menos una fecha de conteo");
        }
        
        if (solicitud.getFechaConteos().size() != solicitud.getNumeroConteos()) {
            throw new RuntimeException("El número de fechas de conteos debe coincidir con el número de conteos definido");
        }
        
        // Validar días de prefrío y pretratamiento según los flags booleanos
        int diasPrefrio = 0;
        int diasPretratamiento = 0;
        
        if (Boolean.TRUE.equals(solicitud.getTienePrefrio())) {
            if (solicitud.getDiasPrefrio() != null) {
                diasPrefrio = solicitud.getDiasPrefrio();
                if (diasPrefrio < 0) {
                    throw new RuntimeException("Los días de prefrío deben ser mayores o iguales a 0");
                }
            }
        }
        
        if (Boolean.TRUE.equals(solicitud.getTienePretratamiento())) {
            if (solicitud.getDiasPretratamiento() != null) {
                diasPretratamiento = solicitud.getDiasPretratamiento();
                if (diasPretratamiento < 0) {
                    throw new RuntimeException("Los días de pretratamiento deben ser mayores o iguales a 0");
                }
            }
        }
        
        // Calcular fecha del primer conteo permitido
        java.time.LocalDate fechaPrimerConteoPermitido = solicitud.getFechaInicioGerm()
            .plusDays(diasPrefrio + diasPretratamiento);
        
        // Validar que las fechas de conteo estén dentro del rango y respeten los días de prefrío/pretratamiento
        for (int i = 0; i < solicitud.getFechaConteos().size(); i++) {
            java.time.LocalDate fechaConteo = solicitud.getFechaConteos().get(i);
            if (fechaConteo == null) {
                throw new RuntimeException("La fecha de conteo " + (i + 1) + " no puede ser nula");
            }
            
            // Validar que la primera fecha de conteo respete los días de prefrío y pretratamiento
            if (i == 0 && fechaConteo.isBefore(fechaPrimerConteoPermitido)) {
                throw new RuntimeException("El primer conteo debe realizarse después de " + 
                    (diasPrefrio + diasPretratamiento) + " días desde la fecha de inicio (fecha mínima: " + 
                    fechaPrimerConteoPermitido + ")");
            }
            
            // Validar que esté dentro del rango permitido
            if (fechaConteo.isBefore(solicitud.getFechaInicioGerm()) || 
                fechaConteo.isAfter(solicitud.getFechaUltConteo())) {
                throw new RuntimeException("La fecha de conteo " + (i + 1) + 
                    " debe estar entre la fecha de inicio y la fecha de último conteo");
            }
            
            // Validar orden cronológico - debe ser igual o posterior al anterior
            if (i > 0 && fechaConteo.isBefore(solicitud.getFechaConteos().get(i - 1))) {
                throw new RuntimeException("La fecha de conteo " + (i + 1) + 
                    " debe ser igual o posterior a la fecha de conteo " + i);
            }
        }
    }
    
    /**
     * Validar porcentajes con redondeo
     */
    private void validarPorcentajes(PorcentajesRedondeoRequestDTO solicitud) {
        // Validar que todos los porcentajes estén entre 0 y 100
        if (solicitud.getPorcentajeNormalesConRedondeo() != null) {
            validarRangoPorcentaje("Normales", solicitud.getPorcentajeNormalesConRedondeo());
        }
        
        if (solicitud.getPorcentajeAnormalesConRedondeo() != null) {
            validarRangoPorcentaje("Anormales", solicitud.getPorcentajeAnormalesConRedondeo());
        }
        
        if (solicitud.getPorcentajeDurasConRedondeo() != null) {
            validarRangoPorcentaje("Duras", solicitud.getPorcentajeDurasConRedondeo());
        }
        
        if (solicitud.getPorcentajeFrescasConRedondeo() != null) {
            validarRangoPorcentaje("Frescas", solicitud.getPorcentajeFrescasConRedondeo());
        }
        
        if (solicitud.getPorcentajeMuertasConRedondeo() != null) {
            validarRangoPorcentaje("Muertas", solicitud.getPorcentajeMuertasConRedondeo());
        }
        
        // Validar que la suma de todos los porcentajes sea aproximadamente 100
        if (solicitud.getPorcentajeNormalesConRedondeo() != null &&
            solicitud.getPorcentajeAnormalesConRedondeo() != null &&
            solicitud.getPorcentajeDurasConRedondeo() != null &&
            solicitud.getPorcentajeFrescasConRedondeo() != null &&
            solicitud.getPorcentajeMuertasConRedondeo() != null) {
            
            double suma = solicitud.getPorcentajeNormalesConRedondeo().doubleValue() +
                         solicitud.getPorcentajeAnormalesConRedondeo().doubleValue() +
                         solicitud.getPorcentajeDurasConRedondeo().doubleValue() +
                         solicitud.getPorcentajeFrescasConRedondeo().doubleValue() +
                         solicitud.getPorcentajeMuertasConRedondeo().doubleValue();
            
            // Permitir un margen de error de ±1 debido al redondeo
            if (suma < 99.0 || suma > 101.0) {
                throw new RuntimeException("La suma de todos los porcentajes debe ser aproximadamente 100% (actual: " + suma + "%)");
            }
        }
    }
    
    /**
     * Validar que un porcentaje esté en el rango 0-100
     */
    private void validarRangoPorcentaje(String tipo, BigDecimal porcentaje) {
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El porcentaje de " + tipo + " no puede ser negativo");
        }
        
        if (porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("El porcentaje de " + tipo + " no puede ser mayor a 100%");
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
            
            System.out.println("📋 Actualizando tabla ID: " + id);
            System.out.println("  Fecha último conteo anterior: " + tablaGerm.getFechaUltConteo());
            System.out.println("  Fecha último conteo nueva: " + solicitud.getFechaUltConteo());
            
            // Verificar si necesitamos reiniciar campos del último conteo ANTES de actualizar
            boolean debeReiniciarCamposUltimoConteo = false;
            if (solicitud.getFechaUltConteo() != null && tablaGerm.getFechaUltConteo() != null) {
                java.time.LocalDate hoy = java.time.LocalDate.now();
                boolean fechaAnteriorEsPresenteOPasada = !tablaGerm.getFechaUltConteo().isAfter(hoy);
                boolean fechaNuevaEsFutura = solicitud.getFechaUltConteo().isAfter(hoy);
                
                System.out.println("  Fecha anterior es presente/pasada: " + fechaAnteriorEsPresenteOPasada);
                System.out.println("  Fecha nueva es futura: " + fechaNuevaEsFutura);
                
                if (fechaAnteriorEsPresenteOPasada && fechaNuevaEsFutura) {
                    debeReiniciarCamposUltimoConteo = true;
                    System.out.println("  ⚠️ Se detectó cambio de fecha último conteo a futuro - se reiniciarán campos");
                }
            }
            
            // Manejar edición de análisis finalizado según el rol del usuario
            analisisService.manejarEdicionAnalisisFinalizado(tablaGerm.getGerminacion());
            
            // Validar datos de la solicitud
            validarDatosTablaGerm(solicitud, tablaGerm.getGerminacion());
            
            // Si necesitamos reiniciar campos, hacerlo ANTES de actualizar la entidad
            if (debeReiniciarCamposUltimoConteo) {
                reiniciarCamposUltimoConteo(tablaGerm);
            }
            
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
            
            // Manejar edición de análisis finalizado según el rol del usuario
            analisisService.manejarEdicionAnalisisFinalizado(tabla.getGerminacion());
            
            // Validar que se puedan ingresar porcentajes
            if (!puedeIngresarPorcentajes(tablaId)) {
                throw new RuntimeException("No se pueden ingresar porcentajes hasta completar todas las repeticiones");
            }
            
            // Validar porcentajes
            validarPorcentajes(solicitud);
            
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
            
            // Validar que todas las repeticiones cumplan con el rango de tolerancia del 5%
            validarRangoToleranciaRepeticiones(tabla);
            
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
    
    /**
     * Validar que todas las repeticiones estén dentro del rango de tolerancia del 5%
     */
    private void validarRangoToleranciaRepeticiones(TablaGerm tabla) {
        if (tabla.getRepGerm() == null || tabla.getRepGerm().isEmpty()) {
            return;
        }
        
        Integer numSemillasPRep = tabla.getNumSemillasPRep();
        if (numSemillasPRep == null) {
            return;
        }
        
        int limiteMinimo = (int) Math.floor(numSemillasPRep * 0.95);
        int limiteMaximo = (int) Math.floor(numSemillasPRep * 1.05);
        
        List<String> repeticionesFueraDeRango = new ArrayList<>();
        
        for (RepGerm rep : tabla.getRepGerm()) {
            Integer total = rep.getTotal();
            if (total != null && (total < limiteMinimo || total > limiteMaximo)) {
                repeticionesFueraDeRango.add("Repetición " + rep.getNumRep() + " (total: " + total + ")");
            }
        }
        
        if (!repeticionesFueraDeRango.isEmpty()) {
            throw new RuntimeException("No se puede finalizar la tabla. Las siguientes repeticiones están fuera del rango de tolerancia del 5% (" + 
                limiteMinimo + "-" + limiteMaximo + " semillas): " + String.join(", ", repeticionesFueraDeRango) + 
                ". Se perdió más del 5% de las semillas o hay un exceso.");
        }
    }

    // Validar que todas las repeticiones esperadas estén completas
    private boolean todasLasRepeticionesCompletas(TablaGerm tabla) {
        if (tabla == null || tabla.getNumeroRepeticiones() == null) {
            return false;
        }
        
        // Contar repeticiones existentes
        List<RepGerm> repeticiones = tabla.getRepGerm();
        if (repeticiones == null) {
            return false;
        }
        
        int repeticionesEsperadas = tabla.getNumeroRepeticiones();
        int repeticionesExistentes = repeticiones.size();
        
        // Verificar que tengamos el número esperado de repeticiones
        if (repeticionesExistentes != repeticionesEsperadas) {
            return false;
        }
        
        // Verificar que cada repetición tenga todos sus conteos completos
        Integer numeroConteos = tabla.getNumeroConteos();
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
        
        // Campos Boolean para prefrío y pretratamiento con lógica condicional
        tablaGerm.setTienePrefrio(solicitud.getTienePrefrio());
        if (Boolean.TRUE.equals(solicitud.getTienePrefrio())) {
            tablaGerm.setDescripcionPrefrio(solicitud.getDescripcionPrefrio());
            tablaGerm.setDiasPrefrio(solicitud.getDiasPrefrio() != null ? solicitud.getDiasPrefrio() : 0);
        } else {
            tablaGerm.setDescripcionPrefrio(null);
            tablaGerm.setDiasPrefrio(0);
        }
        
        tablaGerm.setTienePretratamiento(solicitud.getTienePretratamiento());
        if (Boolean.TRUE.equals(solicitud.getTienePretratamiento())) {
            tablaGerm.setDescripcionPretratamiento(solicitud.getDescripcionPretratamiento());
            tablaGerm.setDiasPretratamiento(solicitud.getDiasPretratamiento() != null ? solicitud.getDiasPretratamiento() : 0);
        } else {
            tablaGerm.setDescripcionPretratamiento(null);
            tablaGerm.setDiasPretratamiento(0);
        }
        
        // Campos de fechas y control de conteos
        tablaGerm.setFechaInicioGerm(solicitud.getFechaInicioGerm());
        tablaGerm.setFechaConteos(solicitud.getFechaConteos());
        tablaGerm.setFechaUltConteo(solicitud.getFechaUltConteo());
        tablaGerm.setNumDias(solicitud.getNumDias());
        tablaGerm.setNumeroRepeticiones(solicitud.getNumeroRepeticiones());
        tablaGerm.setNumeroConteos(solicitud.getNumeroConteos());
        
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
        
        // Campos Boolean para prefrío y pretratamiento con lógica condicional
        tablaGerm.setTienePrefrio(solicitud.getTienePrefrio());
        if (Boolean.TRUE.equals(solicitud.getTienePrefrio())) {
            tablaGerm.setDescripcionPrefrio(solicitud.getDescripcionPrefrio());
            tablaGerm.setDiasPrefrio(solicitud.getDiasPrefrio() != null ? solicitud.getDiasPrefrio() : 0);
        } else {
            tablaGerm.setDescripcionPrefrio(null);
            tablaGerm.setDiasPrefrio(0);
        }
        
        tablaGerm.setTienePretratamiento(solicitud.getTienePretratamiento());
        if (Boolean.TRUE.equals(solicitud.getTienePretratamiento())) {
            tablaGerm.setDescripcionPretratamiento(solicitud.getDescripcionPretratamiento());
            tablaGerm.setDiasPretratamiento(solicitud.getDiasPretratamiento() != null ? solicitud.getDiasPretratamiento() : 0);
        } else {
            tablaGerm.setDescripcionPretratamiento(null);
            tablaGerm.setDiasPretratamiento(0);
        };
        
        // Actualizar fechas de conteos si se proporcionan
        if (solicitud.getFechaConteos() != null && !solicitud.getFechaConteos().isEmpty()) {
            // Verificar si alguna fecha cambió
            boolean fechasCambiaron = false;
            if (tablaGerm.getFechaConteos() == null || 
                tablaGerm.getFechaConteos().size() != solicitud.getFechaConteos().size()) {
                fechasCambiaron = true;
            } else {
                for (int i = 0; i < solicitud.getFechaConteos().size(); i++) {
                    if (!solicitud.getFechaConteos().get(i).equals(tablaGerm.getFechaConteos().get(i))) {
                        fechasCambiaron = true;
                        // Verificar si la nueva fecha es posterior a la original
                        if (solicitud.getFechaConteos().get(i).isAfter(tablaGerm.getFechaConteos().get(i))) {
                            // Reiniciar datos de las repeticiones para este conteo
                            reiniciarDatosConteo(tablaGerm, i);
                        }
                    }
                }
            }
            
            if (fechasCambiaron) {
                // Validar las nuevas fechas
                validarFechasConteosEnEdicion(solicitud, tablaGerm);
                tablaGerm.setFechaConteos(solicitud.getFechaConteos());
            }
        }
        
        // Actualizar otros campos de fechas
        if (solicitud.getFechaInicioGerm() != null) {
            tablaGerm.setFechaInicioGerm(solicitud.getFechaInicioGerm());
        }
        
        if (solicitud.getFechaUltConteo() != null) {
            tablaGerm.setFechaUltConteo(solicitud.getFechaUltConteo());
        }
        
        if (solicitud.getNumDias() != null) {
            tablaGerm.setNumDias(solicitud.getNumDias());
        }
        
        // Actualizar días de prefrío y pretratamiento
        if (solicitud.getDiasPrefrio() != null) {
            tablaGerm.setDiasPrefrio(solicitud.getDiasPrefrio());
        }
        if (solicitud.getDiasPretratamiento() != null) {
            tablaGerm.setDiasPretratamiento(solicitud.getDiasPretratamiento());
        }
    }
    
    /**
     * Reiniciar datos de un conteo específico en todas las repeticiones
     */
    private void reiniciarDatosConteo(TablaGerm tablaGerm, int indiceConteo) {
        if (tablaGerm.getRepGerm() != null) {
            for (RepGerm rep : tablaGerm.getRepGerm()) {
                if (rep.getNormales() != null && indiceConteo < rep.getNormales().size()) {
                    rep.getNormales().set(indiceConteo, 0);
                    repGermRepository.save(rep);
                }
            }
        }
    }
    
    /**
     * Reiniciar campos del último conteo (anormales, duras, frescas, muertas) cuando 
     * la fecha de último conteo cambia de presente/pasada a futura
     */
    private void reiniciarCamposUltimoConteo(TablaGerm tablaGerm) {
        System.out.println("🔄 Reiniciando campos del último conteo para tabla ID: " + tablaGerm.getTablaGermID());
        
        // Cargar repeticiones explícitamente desde la base de datos
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGerm.getTablaGermID());
        
        System.out.println("  📊 Repeticiones encontradas: " + repeticiones.size());
        
        if (repeticiones != null && !repeticiones.isEmpty()) {
            for (RepGerm rep : repeticiones) {
                System.out.println("  📝 Limpiando repetición " + rep.getNumRep() + 
                    " (ID: " + rep.getRepGermID() + ")" +
                    " - Valores anteriores: anormales=" + rep.getAnormales() + 
                    ", duras=" + rep.getDuras() + 
                    ", frescas=" + rep.getFrescas() + 
                    ", muertas=" + rep.getMuertas());
                
                // Establecer todos los campos del último conteo a 0
                rep.setAnormales(0);
                rep.setDuras(0);
                rep.setFrescas(0);
                rep.setMuertas(0);
                
                RepGerm repGuardada = repGermRepository.save(rep);
                System.out.println("  ✅ Repetición " + rep.getNumRep() + " limpiada - Nuevos valores: anormales=" + 
                    repGuardada.getAnormales() + ", duras=" + repGuardada.getDuras() + 
                    ", frescas=" + repGuardada.getFrescas() + ", muertas=" + repGuardada.getMuertas());
            }
        } else {
            System.out.println("  ⚠️ No se encontraron repeticiones para limpiar");
        }
        
        System.out.println("✅ Campos del último conteo reiniciados completamente");
    }
    
    /**
     * Validar fechas de conteos en edición
     */
    private void validarFechasConteosEnEdicion(TablaGermRequestDTO solicitud, TablaGerm tablaExistente) {
        // Aplicar las mismas validaciones que en creación
        int diasPrefrio = (solicitud.getDiasPrefrio() != null) ? solicitud.getDiasPrefrio() : 
                          (tablaExistente.getDiasPrefrio() != null ? tablaExistente.getDiasPrefrio() : 0);
        int diasPretratamiento = (solicitud.getDiasPretratamiento() != null) ? solicitud.getDiasPretratamiento() : 
                                 (tablaExistente.getDiasPretratamiento() != null ? tablaExistente.getDiasPretratamiento() : 0);
        
        java.time.LocalDate fechaInicio = solicitud.getFechaInicioGerm() != null ? 
            solicitud.getFechaInicioGerm() : tablaExistente.getFechaInicioGerm();
        
        java.time.LocalDate fechaPrimerConteoPermitido = fechaInicio.plusDays(diasPrefrio + diasPretratamiento);
        
        for (int i = 0; i < solicitud.getFechaConteos().size(); i++) {
            java.time.LocalDate fechaConteo = solicitud.getFechaConteos().get(i);
            
            if (i == 0 && fechaConteo.isBefore(fechaPrimerConteoPermitido)) {
                throw new RuntimeException("El primer conteo debe realizarse después de " + 
                    (diasPrefrio + diasPretratamiento) + " días desde la fecha de inicio");
            }
        }
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
        
        // Campos Boolean para prefrío y pretratamiento
        dto.setTienePrefrio(tablaGerm.getTienePrefrio());
        dto.setDescripcionPrefrio(tablaGerm.getDescripcionPrefrio());
        dto.setTienePretratamiento(tablaGerm.getTienePretratamiento());
        dto.setDescripcionPretratamiento(tablaGerm.getDescripcionPretratamiento());
        
        // Campos de fechas y control de conteos
        dto.setFechaInicioGerm(tablaGerm.getFechaInicioGerm());
        dto.setFechaConteos(tablaGerm.getFechaConteos());
        dto.setFechaUltConteo(tablaGerm.getFechaUltConteo());
        dto.setNumDias(tablaGerm.getNumDias());
        dto.setNumeroRepeticiones(tablaGerm.getNumeroRepeticiones());
        dto.setNumeroConteos(tablaGerm.getNumeroConteos());
        dto.setDiasPrefrio(tablaGerm.getDiasPrefrio());
        dto.setDiasPretratamiento(tablaGerm.getDiasPretratamiento());
        
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