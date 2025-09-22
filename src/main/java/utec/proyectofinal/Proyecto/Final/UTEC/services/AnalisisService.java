package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Finaliza un análisis según el rol del usuario actual
     * - Si es analista, cambia estado a PENDIENTE_APROBACION
     * - Si es admin, cambia estado a APROBADO directamente
     * 
     * @param analisis El análisis a finalizar
     * @return El análisis actualizado
     */
    public Analisis finalizarAnalisis(Analisis analisis) {
        if (esAnalista()) {
            // Analista: enviar a pendiente de aprobación
            analisis.setEstado(Estado.PENDIENTE_APROBACION);
        } else {
            // Admin: aprobar directamente
            analisis.setEstado(Estado.APROBADO);
        }
        
        // Registrar en el historial
        analisisHistorialService.registrarModificacion(analisis);
        
        return analisis;
    }

    /**
     * Aprueba un análisis (verifica que esté en PENDIENTE_APROBACION)
     * 
     * @param analisis El análisis a aprobar
     * @return El análisis actualizado
     * @throws RuntimeException si el análisis no está en estado PENDIENTE_APROBACION
     */
    public Analisis aprobarAnalisis(Analisis analisis) {
        // Validar que esté en estado PENDIENTE_APROBACION
        if (analisis.getEstado() != Estado.PENDIENTE_APROBACION) {
            throw new RuntimeException("El análisis debe estar en estado PENDIENTE_APROBACION para ser aprobado");
        }
        
        analisis.setEstado(Estado.APROBADO);
        
        // Registrar en el historial
        analisisHistorialService.registrarModificacion(analisis);
        
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
}