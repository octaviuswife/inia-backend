package utec.proyectofinal.Proyecto.Final.UTEC.services;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EstadisticasTandaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsListadoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Service
public class PmsService {

    @Autowired
    private PmsRepository pmsRepository;

    @Autowired
    private RepPmsRepository repPmsRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private AnalisisHistorialService analisisHistorialService;
    
    @Autowired
    private AnalisisService analisisService;

        @Transactional
    // Crear Pms con estado REGISTRADO
    public PmsDTO crearPms(PmsRequestDTO solicitud) {
        // Validar que se especifique el número de repeticiones esperadas
        if (solicitud.getNumRepeticionesEsperadas() == null || solicitud.getNumRepeticionesEsperadas() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones esperadas (mayor a 0).");
        }
        
        // Validar que en el peor caso (múltiples tandas inválidas) no se supere el límite de 16 repeticiones totales
        // Como mínimo necesitamos 1 tanda válida, así que validamos que las repeticiones por tanda no excedan 16
        if (solicitud.getNumRepeticionesEsperadas() > 16) {
            throw new RuntimeException("El número de repeticiones por tanda no puede superar 16.");
        }
        
        Pms pms = mapearSolicitudAEntidad(solicitud);
        pms.setEstado(Estado.REGISTRADO);

        Pms guardado = pmsRepository.save(pms);
        
        // Registrar automáticamente en el historial
        analisisHistorialService.registrarCreacion(guardado);
        
        return mapearEntidadADTO(guardado);
    }

    // Editar Pms
    @Transactional
    public PmsDTO actualizarPms(Long id, PmsRequestDTO solicitud) {
        Optional<Pms> existente = pmsRepository.findById(id);

        if (existente.isPresent()) {
            Pms pms = existente.get();
            
            // Manejar cambios de estado según rol del usuario usando el servicio común
            analisisService.manejarEdicionAnalisisFinalizado(pms);
            
            actualizarEntidadDesdeSolicitud(pms, solicitud);
            Pms actualizado = pmsRepository.save(pms);
            
            // Registrar automáticamente en el historial
            analisisHistorialService.registrarModificacion(actualizado);
            
            return mapearEntidadADTO(actualizado);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    // Eliminar Pms (desactivar - cambia activo a false)
    public void eliminarPms(Long id) {
        Optional<Pms> existente = pmsRepository.findById(id);

        if (existente.isPresent()) {
            Pms pms = existente.get();
            pms.setActivo(false);
            pmsRepository.save(pms);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    // Desactivar PMS (cambiar activo a false)
    public void desactivarPms(Long id) {
        analisisService.desactivarAnalisis(id, pmsRepository);
    }

    // Reactivar PMS (cambiar activo a true)
    public PmsDTO reactivarPms(Long id) {
        return analisisService.reactivarAnalisis(id, pmsRepository, this::mapearEntidadADTO);
    }

    // Listar todos los Pms activos
    public List<PmsDTO> obtenerTodos() {
        List<Pms> activos = pmsRepository.findByActivoTrue();

        return activos.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener Pms por ID
    public PmsDTO obtenerPorId(Long id) {
        Optional<Pms> pms = pmsRepository.findById(id);
        if (pms.isPresent()) {
            return mapearEntidadADTO(pms.get());
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    public List<PmsDTO> obtenerPmsPorIdLote(Long idLote) {
        List<Pms> lista = pmsRepository.findByIdLote(idLote.intValue());

        return lista.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Listar PMS con paginado (para listado)
    public Page<PmsListadoDTO> obtenerPmsPaginadas(Pageable pageable) {
        Page<Pms> pmsPage = pmsRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
        return pmsPage.map(this::mapearEntidadAListadoDTO);
    }

    // Listar PMS con paginado y filtro de activo
    public Page<PmsListadoDTO> obtenerPmsPaginadasConFiltro(Pageable pageable, String filtroActivo) {
        Page<Pms> pmsPage;
        
        switch (filtroActivo.toLowerCase()) {
            case "activos":
                pmsPage = pmsRepository.findByActivoTrueOrderByFechaInicioDesc(pageable);
                break;
            case "inactivos":
                pmsPage = pmsRepository.findByActivoFalseOrderByFechaInicioDesc(pageable);
                break;
            default: // "todos"
                pmsPage = pmsRepository.findAllByOrderByFechaInicioDesc(pageable);
                break;
        }
        
        return pmsPage.map(this::mapearEntidadAListadoDTO);
    }

    private PmsListadoDTO mapearEntidadAListadoDTO(Pms pms) {
        PmsListadoDTO dto = new PmsListadoDTO();
        dto.setAnalisisID(pms.getAnalisisID());
        dto.setEstado(pms.getEstado());
        dto.setFechaInicio(pms.getFechaInicio());
        dto.setFechaFin(pms.getFechaFin());
        if (pms.getLote() != null) {
            dto.setIdLote(pms.getLote().getLoteID());
            dto.setLote(pms.getLote().getFicha());
        }
        if (pms.getAnalisisID() != null) {
            var historial = analisisHistorialService.obtenerHistorialAnalisis(pms.getAnalisisID());
            if (!historial.isEmpty()) {
                var primerRegistro = historial.get(historial.size() - 1);
                dto.setUsuarioCreador(primerRegistro.getUsuario());
                var ultimoRegistro = historial.get(0);
                dto.setUsuarioModificador(ultimoRegistro.getUsuario());
            }
        }
        return dto;
    }

    // Actualizar PMS con redondeo (solo cuando todas las repeticiones estén completas)
    @Transactional
    public PmsDTO actualizarPmsConRedondeo(Long id, PmsRedondeoRequestDTO solicitud) {
        Optional<Pms> pmsExistente = pmsRepository.findById(id);
        
        if (pmsExistente.isPresent()) {
            Pms pms = pmsExistente.get();
            
            System.out.println("=== DEBUG actualizarPmsConRedondeo ===");
            System.out.println("PMS ID: " + id);
            System.out.println("Estado actual: " + pms.getEstado());
            System.out.println("Número de tandas: " + pms.getNumTandas());
            System.out.println("Repeticiones esperadas: " + pms.getNumRepeticionesEsperadas());
            
            // Validar estado del análisis - permitir EN_PROCESO, PENDIENTE_APROBACION y APROBADO
            if (pms.getEstado() != Estado.EN_PROCESO && 
                pms.getEstado() != Estado.PENDIENTE_APROBACION && 
                pms.getEstado() != Estado.APROBADO) {
                System.out.println("ERROR: Estado no válido: " + pms.getEstado());
                throw new RuntimeException("Solo se pueden actualizar valores finales de PMS en estado EN_PROCESO, PENDIENTE_APROBACION o APROBADO. Estado actual: " + pms.getEstado());
            }
            
            // Validar que se hayan completado todas las repeticiones válidas
            boolean repeticionesCompletas = todasLasRepeticionesCompletas(pms);
            System.out.println("Repeticiones completas: " + repeticionesCompletas);
            if (!repeticionesCompletas) {
                System.out.println("ERROR: No todas las repeticiones están completas");
                throw new RuntimeException("No se pueden actualizar los valores finales hasta completar todas las repeticiones válidas");
            }
            
            // Actualizar solo el valor con redondeo
            pms.setPmsconRedon(solicitud.getPmsconRedon());
            
            Pms pmsActualizado = pmsRepository.save(pms);
            System.out.println("PMS con redondeo actualizado exitosamente para PMS ID: " + id);
            return mapearEntidadADTO(pmsActualizado);
        } else {
            throw new RuntimeException("Análisis de PMS no encontrado con ID: " + id);
        }
    }

    // Procesar cálculos cuando se complete una tanda
    public void procesarCalculosTanda(Long pmsId, Integer numTanda) {
        Pms pms = pmsRepository.findById(pmsId)
            .orElseThrow(() -> new RuntimeException("PMS no encontrado con ID: " + pmsId));
        
        List<RepPms> repeticionesTanda = repPmsRepository.findByPmsId(pmsId).stream()
            .filter(rep -> rep.getNumTanda().equals(numTanda))
            .collect(Collectors.toList());
        
        if (repeticionesTanda.size() < pms.getNumRepeticionesEsperadas()) {
            // Si la tanda no está completa, solo actualizar estadísticas generales
            actualizarEstadisticasGenerales(pms);
            pmsRepository.save(pms);
            return;
        }
        
        // PASO 1: Calcular estadísticas con TODAS las repeticiones de la tanda
        EstadisticasTandaDTO estadisticasIniciales = calcularEstadisticasTanda(repeticionesTanda);
        
        // PASO 2: Identificar y marcar outliers (±2σ de la media)
        BigDecimal media = estadisticasIniciales.getPromedio();
        BigDecimal desviacion = estadisticasIniciales.getDesviacion();
        BigDecimal umbralInferior = media.subtract(desviacion.multiply(new BigDecimal("2")));
        BigDecimal umbralSuperior = media.add(desviacion.multiply(new BigDecimal("2")));
        
        // Marcar repeticiones como válidas o inválidas según ±2σ
        for (RepPms rep : repeticionesTanda) {
            boolean esValida = rep.getPeso().compareTo(umbralInferior) >= 0 && 
                              rep.getPeso().compareTo(umbralSuperior) <= 0;
            rep.setValido(esValida);
        }
        repPmsRepository.saveAll(repeticionesTanda);
        
        // PASO 3: Filtrar solo repeticiones válidas y recalcular estadísticas
        List<RepPms> repeticionesValidas = repeticionesTanda.stream()
            .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
            .collect(Collectors.toList());
        
        if (repeticionesValidas.isEmpty()) {
            // Si no hay repeticiones válidas, incrementar tandas si es posible
            if (puedeIncrementarTandas(pms)) {
                pms.setNumTandas(pms.getNumTandas() + 1);
                System.out.println("No hay repeticiones válidas. Se incrementa el número de tandas a: " + pms.getNumTandas());
            }
            actualizarEstadisticasGenerales(pms);
            pmsRepository.save(pms);
            return;
        }
        
        // PASO 4: Calcular estadísticas finales solo con repeticiones válidas
        EstadisticasTandaDTO estadisticasFinales = calcularEstadisticasTanda(repeticionesValidas);
        
        // PASO 5: Evaluar CV según tipo de semilla
        BigDecimal umbralCV = pms.getEsSemillaBrozosa() ? 
            new BigDecimal("6.0") : new BigDecimal("4.0");
        
        if (estadisticasFinales.getCoeficienteVariacion().compareTo(umbralCV) > 0) {
            // CV no aceptable - incrementar tandas si es posible
            if (puedeIncrementarTandas(pms)) {
                pms.setNumTandas(pms.getNumTandas() + 1);
                System.out.println("CV no aceptable (" + estadisticasFinales.getCoeficienteVariacion() + " > " + umbralCV + "). Se incrementa el número de tandas a: " + pms.getNumTandas());
            } else {
                System.out.println("CV no aceptable pero se alcanzó el límite máximo de 16 repeticiones. No se pueden agregar más tandas.");
            }
        }
        // Si CV es aceptable, no hacer nada automático - el usuario debe finalizar manualmente
        
        // PASO 6: Actualizar estadísticas generales del PMS
        actualizarEstadisticasGenerales(pms);
        
        pmsRepository.save(pms);
    }
    
    // Método público para actualizar estadísticas generales desde servicios externos
    public void actualizarEstadisticasPms(Long pmsId) {
        Pms pms = pmsRepository.findById(pmsId)
            .orElseThrow(() -> new RuntimeException("PMS no encontrado con ID: " + pmsId));
        
        actualizarEstadisticasGenerales(pms);
        pmsRepository.save(pms);
    }



    // ==============================
    // Métodos auxiliares de mapeo
    // ==============================

    private Pms mapearSolicitudAEntidad(PmsRequestDTO solicitud) {
        Pms pms = new Pms();

        pms.setComentarios(solicitud.getComentarios());

        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                pms.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        // Campos específicos de PMS
        pms.setNumRepeticionesEsperadas(solicitud.getNumRepeticionesEsperadas());
        pms.setNumTandas(1); // Siempre inicia con 1 tanda
        pms.setEsSemillaBrozosa(solicitud.getEsSemillaBrozosa());

        return pms;
    }

    private void actualizarEntidadDesdeSolicitud(Pms pms, PmsRequestDTO solicitud) {

        pms.setComentarios(solicitud.getComentarios());

        if (solicitud.getIdLote() != null) {
            Optional<Lote> loteOpt = loteRepository.findById(solicitud.getIdLote());
            if (loteOpt.isPresent()) {
                pms.setLote(loteOpt.get());
            } else {
                throw new RuntimeException("Lote no encontrado con ID: " + solicitud.getIdLote());
            }
        }

        // Campos específicos de PMS (solo campos de configuración, no los calculados)
        // El número de repeticiones esperadas Y el número de tandas NO se pueden editar una vez creado
        // numTandas se maneja automáticamente por la lógica del sistema
        pms.setEsSemillaBrozosa(solicitud.getEsSemillaBrozosa());
    }

    private PmsDTO mapearEntidadADTO(Pms pms) {
        PmsDTO dto = new PmsDTO();

        dto.setAnalisisID(pms.getAnalisisID());
        dto.setEstado(pms.getEstado());
        dto.setFechaInicio(pms.getFechaInicio());
        dto.setFechaFin(pms.getFechaFin());
        dto.setComentarios(pms.getComentarios());

        if (pms.getLote() != null) {
            dto.setIdLote(pms.getLote().getLoteID());
            dto.setLote(pms.getLote().getFicha());
        }

        // Campos específicos de PMS de configuración
        dto.setNumRepeticionesEsperadas(pms.getNumRepeticionesEsperadas());
        dto.setNumTandas(pms.getNumTandas());
        dto.setEsSemillaBrozosa(pms.getEsSemillaBrozosa());
        
        // Campos calculados
        dto.setPromedio100g(pms.getPromedio100g());
        dto.setDesvioStd(pms.getDesvioStd());
        dto.setCoefVariacion(pms.getCoefVariacion());
        dto.setPmssinRedon(pms.getPmssinRedon());
        dto.setPmsconRedon(pms.getPmsconRedon());

        // Mapear historial de análisis
        dto.setHistorial(analisisHistorialService.obtenerHistorialAnalisis(pms.getAnalisisID()));

        return dto;
    }

    // ==============================
    // Métodos auxiliares de cálculo estadístico y validación
    // ==============================

    private EstadisticasTandaDTO calcularEstadisticasTanda(List<RepPms> repeticiones) {
        if (repeticiones.isEmpty()) {
            throw new RuntimeException("No se pueden calcular estadísticas de una tanda vacía");
        }

        // Calcular promedio con 4 decimales
        BigDecimal suma = repeticiones.stream()
            .map(RepPms::getPeso)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal promedio = suma.divide(
            new BigDecimal(repeticiones.size()), 
            4, RoundingMode.HALF_UP  // 4 decimales para promedio
        );

        // Calcular desviación estándar (muestral: dividir por n-1)
        BigDecimal sumaCuadrados = repeticiones.stream()
            .map(rep -> rep.getPeso().subtract(promedio).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Usar desviación estándar muestral (n-1) para muestras pequeñas
        int n = repeticiones.size();
        BigDecimal divisor = n > 1 ? new BigDecimal(n - 1) : new BigDecimal(1);
        
        BigDecimal varianza = sumaCuadrados.divide(
            divisor, 
            MathContext.DECIMAL128
        );
        
        BigDecimal desviacion = new BigDecimal(Math.sqrt(varianza.doubleValue()))
            .setScale(4, RoundingMode.HALF_UP);

        // Calcular coeficiente de variación (CV = desviacion / promedio * 100) con 4 decimales
        BigDecimal coeficienteVariacion = desviacion
            .divide(promedio, MathContext.DECIMAL128)
            .multiply(new BigDecimal("100"))
            .setScale(4, RoundingMode.HALF_UP);  // Cambiar de 2 a 4 decimales

        // Calcular PMS sin redondeo (promedio * 10) con 4 decimales
        BigDecimal pmsSinRedondeo = promedio.multiply(new BigDecimal("10"))
            .setScale(4, RoundingMode.HALF_UP);  // Cambiar de 2 a 4 decimales

        return new EstadisticasTandaDTO(promedio, desviacion, coeficienteVariacion, pmsSinRedondeo);
    }

    private void actualizarEstadisticasGenerales(Pms pms) {
        // Obtener SOLO las repeticiones VÁLIDAS del PMS
        List<RepPms> repeticionesValidas = repPmsRepository.findByPmsId(pms.getAnalisisID()).stream()
            .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
            .collect(Collectors.toList());
        
        if (!repeticionesValidas.isEmpty()) {
            EstadisticasTandaDTO estadisticasGenerales = calcularEstadisticasTanda(repeticionesValidas);
            
            // Actualizar estadísticas del PMS solo con repeticiones válidas
            pms.setPromedio100g(estadisticasGenerales.getPromedio());
            pms.setDesvioStd(estadisticasGenerales.getDesviacion());
            pms.setCoefVariacion(estadisticasGenerales.getCoeficienteVariacion());
            pms.setPmssinRedon(estadisticasGenerales.getPmsSinRedondeo());
        } else {
            // Si no hay repeticiones válidas, limpiar estadísticas
            pms.setPromedio100g(null);
            pms.setDesvioStd(null);
            pms.setCoefVariacion(null);
            pms.setPmssinRedon(null);
        }
    }

    private boolean todasLasTandasCompletas(Pms pms) {
        for (int tandaNum = 1; tandaNum <= pms.getNumTandas(); tandaNum++) {
            final int tanda = tandaNum;
            List<RepPms> repeticionesTanda = repPmsRepository.findByPmsId(pms.getAnalisisID()).stream()
                .filter(rep -> rep.getNumTanda().equals(tanda) && Boolean.TRUE.equals(rep.getValido()))
                .collect(Collectors.toList());
            
            if (repeticionesTanda.size() < pms.getNumRepeticionesEsperadas()) {
                return false;
            }
        }
        return true;
    }

    private boolean puedeIncrementarTandas(Pms pms) {
        long totalRepeticiones = repPmsRepository.countByPmsId(pms.getAnalisisID());
        // Permitir agregar tandas mientras no se superen las 16 repeticiones totales
        return totalRepeticiones < 16;
    }

    private boolean todasLasRepeticionesCompletas(Pms pms) {
        System.out.println("=== DEBUG todasLasRepeticionesCompletas ===");
        
        // Obtener todas las repeticiones
        List<RepPms> todasLasRepeticiones = repPmsRepository.findByPmsId(pms.getAnalisisID());
        long totalRepeticiones = todasLasRepeticiones.size();
        
        System.out.println("Total de repeticiones: " + totalRepeticiones);
        System.out.println("Número de tandas: " + pms.getNumTandas());
        System.out.println("Repeticiones esperadas por tanda: " + pms.getNumRepeticionesEsperadas());
        
        // Verificar que exista al menos una tanda con repeticiones válidas completas
        for (int tandaNum = 1; tandaNum <= pms.getNumTandas(); tandaNum++) {
            final int tanda = tandaNum;
            List<RepPms> repeticionesTanda = todasLasRepeticiones.stream()
                .filter(rep -> rep.getNumTanda().equals(tanda))
                .collect(Collectors.toList());
            
            List<RepPms> repeticionesValidas = repeticionesTanda.stream()
                .filter(rep -> Boolean.TRUE.equals(rep.getValido()))
                .collect(Collectors.toList());
            
            System.out.println("Tanda " + tanda + ": " + repeticionesTanda.size() + " repeticiones totales, " + 
                             repeticionesValidas.size() + " válidas (necesita " + pms.getNumRepeticionesEsperadas() + ")");
            
            // Si la tanda está completa con repeticiones válidas
            if (repeticionesValidas.size() >= pms.getNumRepeticionesEsperadas()) {
                // Calcular CV de esta tanda para verificar si es válida
                EstadisticasTandaDTO estadisticas = calcularEstadisticasTanda(repeticionesValidas);
                BigDecimal umbralCV = pms.getEsSemillaBrozosa() ? 
                    new BigDecimal("6.0") : new BigDecimal("4.0");
                
                System.out.println("CV de tanda " + tanda + ": " + estadisticas.getCoeficienteVariacion() + 
                                 " (umbral: " + umbralCV + ")");
                
                // Si el CV es aceptable, las repeticiones están completas
                if (estadisticas.getCoeficienteVariacion().compareTo(umbralCV) <= 0) {
                    System.out.println("Tanda " + tanda + " tiene CV válido. Repeticiones completas.");
                    return true;
                }
            }
        }
        
        // Si no hay tandas válidas, verificar si se alcanzó el límite de 16 repeticiones
        if (totalRepeticiones >= 16) {
            System.out.println("Se alcanzó el límite de 16 repeticiones sin CV válido. Permitir finalización.");
            return true; // Permitir finalizar aunque no sea válido si se alcanzó el límite
        }
        
        System.out.println("No hay tandas con CV válido y no se alcanzó el límite. Retornando false.");
        return false;
    }

    // Finalizar análisis PMS - cambia estado según rol del usuario
    public PmsDTO finalizarAnalisis(Long id) {
        return analisisService.finalizarAnalisisGenerico(
            id, 
            pmsRepository, 
            this::mapearEntidadADTO,
            pms -> {
                // Validación específica de PMS: completitud de repeticiones
                if (!todasLasRepeticionesCompletas(pms)) {
                    throw new RuntimeException("No se puede finalizar el análisis hasta completar todas las repeticiones válidas");
                }
                // Validación específica de PMS: debe tener promedio con redondeo ingresado
                if (pms.getPmsconRedon() == null) {
                    throw new RuntimeException("Debe ingresar el promedio con redondeo (PMS con redondeo) antes de finalizar el análisis");
                }
            }
        );
    }

    // Aprobar análisis PMS (solo para administradores)
    public PmsDTO aprobarAnalisis(Long id) {
        return analisisService.aprobarAnalisisGenerico(
            id,
            pmsRepository,
            this::mapearEntidadADTO,
            null // No hay validación específica para aprobar
        );
    }

    // Marcar análisis para repetir (solo administradores)
    public PmsDTO marcarParaRepetir(Long id) {
        return analisisService.marcarParaRepetirGenerico(
            id,
            pmsRepository,
            this::mapearEntidadADTO,
            null // No hay validación específica para marcar a repetir
        );
    }
}
