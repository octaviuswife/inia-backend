package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections;

/**
 * Proyección para resultados de análisis pendientes de aprobación desde query nativa.
 * Spring Data JPA mapea automáticamente los alias de la query a estos getters.
 */
public interface AnalisisPorAprobarProjection {
    Long getAnalisisID();
    String getTipoAnalisis(); // String porque viene de la query SQL
    Long getLoteID();
    String getNomLote();
    String getFicha();
    String getEspecieNombre();
    String getCultivarNombre();
    String getFechaInicio();
    String getFechaFin();
}
