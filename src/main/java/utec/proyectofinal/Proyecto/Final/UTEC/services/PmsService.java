package utec.proyectofinal.Proyecto.Final.UTEC.services;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
            
            // Manejar cambios de estado según rol del usuario
            Estado estadoOriginal = pms.getEstado();
            
            if (estadoOriginal == Estado.APROBADO && analisisService.esAnalista()) {
                // Si es ANALISTA editando un análisis APROBADO, cambiar a PENDIENTE_APROBACION
                pms.setEstado(Estado.PENDIENTE_APROBACION);
            }
            // Si es ADMIN editando análisis APROBADO, mantiene el estado APROBADO
            // Para otros estados se mantiene igual
            
            actualizarEntidadDesdeSolicitud(pms, solicitud);
            Pms actualizado = pmsRepository.save(pms);
            
            // Registrar automáticamente en el historial
            analisisHistorialService.registrarModificacion(actualizado);
            
            return mapearEntidadADTO(actualizado);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    // Eliminar Pms (cambia estado a INACTIVO)
    public void eliminarPms(Long id) {
        Optional<Pms> existente = pmsRepository.findById(id);

        if (existente.isPresent()) {
            Pms pms = existente.get();
            pms.setEstado(Estado.INACTIVO);
            pmsRepository.save(pms);
        } else {
            throw new RuntimeException("Pms no encontrado con ID: " + id);
        }
    }

    // Listar todos los Pms activos
    public List<PmsDTO> obtenerTodos() {
        List<Pms> activos = pmsRepository.findAll()
                .stream()
                .filter(p -> p.getEstado() != Estado.INACTIVO)
                .collect(Collectors.toList());

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

    // Actualizar PMS con redondeo (solo cuando todas las repeticiones estén completas)
    @Transactional
    public PmsDTO actualizarPmsConRedondeo(Long id, PmsRedondeoRequestDTO solicitud) {
        Optional<Pms> pmsExistente = pmsRepository.findById(id);
        
        if (pmsExistente.isPresent()) {
            Pms pms = pmsExistente.get();
            
            // Validar estado del análisis
            if (pms.getEstado() != Estado.PENDIENTE_APROBACION && pms.getEstado() != Estado.APROBADO) {
                throw new RuntimeException("Solo se pueden actualizar valores finales de PMS en estado PENDIENTE_APROBACION o APROBADO");
            }
            
            // Validar que se hayan completado todas las repeticiones válidas
            if (!todasLasRepeticionesCompletas(pms)) {
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
            return; // No se han completado todas las repeticiones de la tanda
        }
        
        // CALCULAR ESTADÍSTICAS CON TODAS LAS REPETICIONES (independiente de validez)
        EstadisticasTandaDTO estadisticas = calcularEstadisticasTanda(repeticionesTanda);
        
        // Determinar umbral de CV según tipo de semilla
        BigDecimal umbralCV = pms.getEsSemillaBrozosa() ? 
            new BigDecimal("6.0") : new BigDecimal("4.0");

        // Validar repeticiones de la tanda según CV (SOLO para marcar validez)
        if (estadisticas.getCoeficienteVariacion().compareTo(umbralCV) <= 0) {
            // CV aceptable - marcar todas las repeticiones como válidas
            marcarRepeticionesComoValidas(repeticionesTanda, true);
            
            // Si todas las tandas están completas, cambiar estado según rol del usuario
            if (todasLasTandasCompletas(pms)) {
                if (analisisService.esAnalista()) {
                    pms.setEstado(Estado.PENDIENTE_APROBACION);
                } else {
                    // Si es admin, aprobar automáticamente
                    pms.setEstado(Estado.APROBADO);
                }
            }
        } else {
            // CV no aceptable - marcar repeticiones como inválidas
            marcarRepeticionesComoValidas(repeticionesTanda, false);
            
            // Incrementar número de tandas si aún no se alcanza el límite de repeticiones
            if (puedeIncrementarTandas(pms)) {
                pms.setNumTandas(pms.getNumTandas() + 1);
                System.out.println("CV no aceptable. Se incrementa el número de tandas a: " + pms.getNumTandas());
            } else {
                System.out.println("CV no aceptable pero se alcanzó el límite máximo de 16 repeticiones. No se pueden agregar más tandas.");
                // El análisis queda con repeticiones inválidas pero no se puede continuar
            }
        }
        
        // SIEMPRE actualizar estadísticas del PMS con TODAS las repeticiones
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

        // Calcular promedio
        BigDecimal suma = repeticiones.stream()
            .map(RepPms::getPeso)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal promedio = suma.divide(
            new BigDecimal(repeticiones.size()), 
            MathContext.DECIMAL128
        );

        // Calcular desviación estándar
        BigDecimal sumaCuadrados = repeticiones.stream()
            .map(rep -> rep.getPeso().subtract(promedio).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal varianza = sumaCuadrados.divide(
            new BigDecimal(repeticiones.size()), 
            MathContext.DECIMAL128
        );
        
        BigDecimal desviacion = new BigDecimal(Math.sqrt(varianza.doubleValue()))
            .setScale(4, RoundingMode.HALF_UP);

        // Calcular coeficiente de variación (CV = desviacion / promedio * 100)
        BigDecimal coeficienteVariacion = desviacion
            .divide(promedio, MathContext.DECIMAL128)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP);

        // Calcular PMS sin redondeo (promedio * 10)
        BigDecimal pmsSinRedondeo = promedio.multiply(new BigDecimal("10"))
            .setScale(2, RoundingMode.HALF_UP);

        return new EstadisticasTandaDTO(promedio, desviacion, coeficienteVariacion, pmsSinRedondeo);
    }

    private void marcarRepeticionesComoValidas(List<RepPms> repeticiones, boolean valido) {
        repeticiones.forEach(rep -> rep.setValido(valido));
        repPmsRepository.saveAll(repeticiones);
    }

    private void actualizarEstadisticasGenerales(Pms pms) {
        // Obtener TODAS las repeticiones del PMS (sin filtrar por validez)
        List<RepPms> todasLasRepeticiones = repPmsRepository.findByPmsId(pms.getAnalisisID());
        
        if (!todasLasRepeticiones.isEmpty()) {
            EstadisticasTandaDTO estadisticasGenerales = calcularEstadisticasTanda(todasLasRepeticiones);
            
            // Actualizar estadísticas del PMS con TODAS las repeticiones
            pms.setPromedio100g(estadisticasGenerales.getPromedio());
            pms.setDesvioStd(estadisticasGenerales.getDesviacion());
            pms.setCoefVariacion(estadisticasGenerales.getCoeficienteVariacion());
            pms.setPmssinRedon(estadisticasGenerales.getPmsSinRedondeo());
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
        // Verificar si agregar una tanda más (con numRepeticionesEsperadas) superaría el límite de 16
        long repeticionesConNuevaTanda = totalRepeticiones + pms.getNumRepeticionesEsperadas();
        return repeticionesConNuevaTanda <= 16;
    }

    private boolean todasLasRepeticionesCompletas(Pms pms) {
        // Verificar que exista al menos una tanda con repeticiones válidas completas
        for (int tandaNum = 1; tandaNum <= pms.getNumTandas(); tandaNum++) {
            final int tanda = tandaNum;
            List<RepPms> repeticionesValidas = repPmsRepository.findByPmsId(pms.getAnalisisID()).stream()
                .filter(rep -> rep.getNumTanda().equals(tanda) && Boolean.TRUE.equals(rep.getValido()))
                .collect(Collectors.toList());
            
            if (repeticionesValidas.size() >= pms.getNumRepeticionesEsperadas()) {
                return true;
            }
        }
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
