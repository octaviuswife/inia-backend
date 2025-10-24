package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

/**
 * Servicio común para operaciones relacionadas con análisis
 * Maneja la lógica compartida entre diferentes tipos de análisis
 */
@Service
public class AnalisisService {

    @Autowired
    private AnalisisHistorialService analisisHistorialService;

    @Autowired
    private NotificacionService notificacionService;

    /**
     * Establece la fecha de inicio automáticamente al crear un análisis
     * 
     * @param analisis El análisis recién creado
     */
    public void establecerFechaInicio(Analisis analisis) {
        analisis.setFechaInicio(LocalDateTime.now());
    }

    /**
     * Finaliza un análisis según el rol del usuario actual y establece fecha fin
     * - Si es analista, cambia estado a PENDIENTE_APROBACION
     * - Si es admin, cambia estado a APROBADO directamente
     * 
     * @param analisis El análisis a finalizar
     * @return El análisis actualizado
     */
    public Analisis finalizarAnalisis(Analisis analisis) {
        // Validar que el análisis esté activo
        if (!analisis.getActivo()) {
            throw new RuntimeException("No se puede finalizar un análisis inactivo");
        }
        
        // Chequear si el estado del análisis no es APROBADO o A_REPETIR
        if (analisis.getEstado() == Estado.APROBADO || analisis.getEstado() == Estado.A_REPETIR) {
            throw new RuntimeException("El análisis ya está finalizado o marcado para repetir");
        }

        if (esAnalista()) {
            // Analista: enviar a pendiente de aprobación
            analisis.setEstado(Estado.PENDIENTE_APROBACION);
        } else {
            // Admin: aprobar directamente
            analisis.setEstado(Estado.APROBADO);
        }
        
        // Establecer fecha de finalización
        analisis.setFechaFin(LocalDateTime.now());
        
        // Registrar en el historial
        analisisHistorialService.registrarModificacion(analisis);
        
        // Crear notificación automática para finalización de análisis
        try {
            notificacionService.notificarAnalisisFinalizado(analisis.getAnalisisID());
        } catch (Exception e) {
            // Log error but don't fail the analysis finalization
            System.err.println("Error creating notification for analysis finalization: " + e.getMessage());
        }
        
        return analisis;
    }

    /**
     * Aprueba un análisis (verifica que esté en PENDIENTE_APROBACION)
     * 
     * @param analisis El análisis a aprobar
     * @return El análisis actualizado
     * @throws RuntimeException si el análisis no está en estado PENDIENTE_APROBACION o está inactivo
     */
    public Analisis aprobarAnalisis(Analisis analisis) {
        // Validar que el análisis esté activo
        if (!analisis.getActivo()) {
            throw new RuntimeException("No se puede aprobar un análisis inactivo");
        }
        
        // Validar que esté en estado PENDIENTE_APROBACION
        if (analisis.getEstado() != Estado.PENDIENTE_APROBACION) {
            throw new RuntimeException("El análisis debe estar en estado PENDIENTE_APROBACION para ser aprobado");
        }
        
        analisis.setEstado(Estado.APROBADO);
        
        // Establecer fecha de finalización si no está establecida
        if (analisis.getFechaFin() == null) {
            analisis.setFechaFin(LocalDateTime.now());
        }
        
        // Registrar en el historial
        analisisHistorialService.registrarModificacion(analisis);
        
        // Crear notificación automática para aprobación de análisis
        try {
            notificacionService.notificarAnalisisAprobado(analisis.getAnalisisID());
        } catch (Exception e) {
            // Log error but don't fail the analysis approval
            System.err.println("Error creating notification for analysis approval: " + e.getMessage());
        }
        
        return analisis;
    }
    
    /**
     * Marca un análisis para repetir (solo administradores)
     * - Se permite marcar para repetir análisis APROBADOS o que cumplan validaciones
     * - Cambia estado a A_REPETIR
     * 
     * @param analisis El análisis a marcar para repetir
     * @return El análisis actualizado
     * @throws RuntimeException si el análisis está inactivo o no cumple las validaciones
     */
    public Analisis marcarParaRepetir(Analisis analisis) {
        // Validar que el análisis esté activo
        if (!analisis.getActivo()) {
            throw new RuntimeException("No se puede marcar para repetir un análisis inactivo");
        }
        
        // Se permite marcar para repetir análisis APROBADOS o que cumplan validaciones
        // (El validador específico se encargará de verificar requisitos por tipo de análisis)
        
        analisis.setEstado(Estado.A_REPETIR);
        
        // Registrar en el historial
        analisisHistorialService.registrarModificacion(analisis);
        
        // Crear notificación automática para rechazo de análisis (marcado para repetir)
        try {
            notificacionService.notificarAnalisisRepetir(analisis.getAnalisisID());
        } catch (Exception e) {
            // Log error but don't fail the analysis rejection
            System.err.println("Error creating notification for analysis rejection: " + e.getMessage());
        }
        
        return analisis;
    }

    /**
     * Verifica si el usuario actual es un analista
     * 
     * @return true si el usuario tiene rol ANALISTA
     */
    public boolean esAnalista() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANALISTA"));
        }
        return false;
    }

    /**
     * Maneja el cambio de estado cuando se edita un análisis finalizado
     * - Si es admin: mantiene el estado actual (FINALIZADO/APROBADO)
     * - Si es analista: cambia a PENDIENTE_APROBACION para nueva revisión
     * 
     * @param analisis El análisis que se está editando
     */
    public void manejarEdicionAnalisisFinalizado(Analisis analisis) {
        // Solo procesar si el análisis está finalizado o aprobado
        if (analisis.getEstado() == Estado.APROBADO) {
            if (esAnalista()) {
                // Analista: cambiar a pendiente de aprobación para nueva revisión
                analisis.setEstado(Estado.PENDIENTE_APROBACION);
                
                // Registrar en el historial
                analisisHistorialService.registrarModificacion(analisis);
                
                // Crear notificación para informar que necesita nueva aprobación
                try {
                    notificacionService.notificarAnalisisPendienteAprobacion(analisis.getAnalisisID());
                } catch (Exception e) {
                    System.err.println("Error creating notification for analysis pending approval: " + e.getMessage());
                }
            }
            // Si es admin: no hacer nada, mantener estado actual
        }
    }

    /**
     * Método genérico para finalizar análisis con validación específica opcional
     * 
     * @param <T> Tipo del análisis que extiende Analisis
     * @param <D> Tipo del DTO de respuesta
     * @param id ID del análisis
     * @param repository Repositorio del tipo específico
     * @param mapper Función para mapear entidad a DTO
     * @param validator Validación específica opcional (puede ser null)
     * @return DTO del análisis finalizado
     */
    public <T extends Analisis, D> D finalizarAnalisisGenerico(
            Long id, 
            JpaRepository<T, Long> repository,
            Function<T, D> mapper,
            Consumer<T> validator) {
        
        T analisis = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        // Ejecutar validación específica si existe
        if (validator != null) {
            validator.accept(analisis);
        }
        
        // Finalizar usando la lógica común
        finalizarAnalisis(analisis);
        
        // Guardar cambios
        T analisisActualizado = repository.save(analisis);
        
        return mapper.apply(analisisActualizado);
    }

    /**
     * Método genérico para aprobar análisis con validación específica opcional
     * 
     * @param <T> Tipo del análisis que extiende Analisis
     * @param <D> Tipo del DTO de respuesta
     * @param id ID del análisis
     * @param repository Repositorio del tipo específico
     * @param mapper Función para mapear entidad a DTO
     * @param validator Validación específica opcional (puede ser null)
     * @param buscarPorLote Función para buscar análisis por lote (puede ser null)
     * @return DTO del análisis aprobado
     */
    public <T extends Analisis, D> D aprobarAnalisisGenerico(
            Long id,
            JpaRepository<T, Long> repository,
            Function<T, D> mapper,
            Consumer<T> validator,
            Function<Long, java.util.List<T>> buscarPorLote) {
        
        T analisis = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        // Si el análisis está marcado como A_REPETIR y tiene lote asociado
        if (analisis.getEstado() == Estado.A_REPETIR && analisis.getLote() != null && buscarPorLote != null) {
            // Buscar otros análisis del mismo tipo para el mismo lote
            java.util.List<T> analisisDelMismoLote = buscarPorLote.apply(analisis.getLote().getLoteID());
            
            // Verificar si existe algún análisis válido (estado diferente de A_REPETIR y activo)
            boolean existeAnalisisValido = analisisDelMismoLote.stream()
                .filter(a -> !a.getAnalisisID().equals(analisis.getAnalisisID())) // Excluir el análisis actual
                .filter(a -> a.getActivo()) // Solo análisis activos
                .anyMatch(a -> a.getEstado() != Estado.A_REPETIR); // Estado diferente de A_REPETIR
            
            if (existeAnalisisValido) {
                throw new RuntimeException("Ya existe un análisis válido de este tipo para el lote " + 
                    analisis.getLote().getFicha() + ". No se puede aprobar este análisis marcado para repetir.");
            }
        }
        
        // Ejecutar validación específica si existe
        if (validator != null) {
            validator.accept(analisis);
        }
        
        // Aprobar usando la lógica común
        aprobarAnalisis(analisis);
        
        // Guardar cambios
        T analisisActualizado = repository.save(analisis);
        
        return mapper.apply(analisisActualizado);
    }

    /**
     * Método genérico para marcar análisis a repetir con validación específica opcional
     * 
     * @param <T> Tipo del análisis que extiende Analisis
     * @param <D> Tipo del DTO de respuesta
     * @param id ID del análisis
     * @param repository Repositorio del tipo específico
     * @param mapper Función para mapear entidad a DTO
     * @param validator Validación específica opcional (puede ser null)
     * @return DTO del análisis marcado para repetir
     */
    public <T extends Analisis, D> D marcarParaRepetirGenerico(
            Long id,
            JpaRepository<T, Long> repository,
            Function<T, D> mapper,
            Consumer<T> validator) {
        
        T analisis = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        // Aplicar validación específica si se proporciona
        if (validator != null) {
            validator.accept(analisis);
        }
        
        // Marcar para repetir usando el método común
        marcarParaRepetir(analisis);
        
        // Guardar y devolver
        T analisisActualizado = repository.save(analisis);
        return mapper.apply(analisisActualizado);
    }

    /**
     * Desactiva un análisis (cambia activo a false)
     * Método genérico que puede ser usado por cualquier tipo de análisis
     * 
     * @param <T> Tipo del análisis que extiende Analisis
     * @param id ID del análisis
     * @param repository Repositorio del tipo de análisis
     */
    public <T extends Analisis> void desactivarAnalisis(
            Long id,
            JpaRepository<T, Long> repository) {
        
        T analisis = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        analisis.setActivo(false);
        repository.save(analisis);
    }

    /**
     * Reactiva un análisis (cambia activo a true)
     * Método genérico que puede ser usado por cualquier tipo de análisis
     * 
     * @param <T> Tipo del análisis que extiende Analisis
     * @param <D> Tipo del DTO de respuesta
     * @param id ID del análisis
     * @param repository Repositorio del tipo de análisis
     * @param mapper Función para mapear entidad a DTO
     * @return DTO del análisis reactivado
     */
    public <T extends Analisis, D> D reactivarAnalisis(
            Long id,
            JpaRepository<T, Long> repository,
            Function<T, D> mapper) {
        
        T analisis = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Análisis no encontrado con ID: " + id));
        
        if (analisis.getActivo()) {
            throw new RuntimeException("El análisis ya está activo");
        }
        
        analisis.setActivo(true);
        T analisisReactivado = repository.save(analisis);
        return mapper.apply(analisisReactivado);
    }
}