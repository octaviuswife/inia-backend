package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections;

/**
 * Proyección para resultados de análisis pendientes desde query nativa.
 * Spring Data JPA mapea automáticamente los alias de la query a estos getters.
 */
public interface AnalisisPendienteProjection {
    Long getLoteID();
    String getNomLote();
    String getFicha();
    String getEspecieNombre();
    String getCultivarNombre();
    String getTipoAnalisis(); // String porque viene de la query SQL
}
