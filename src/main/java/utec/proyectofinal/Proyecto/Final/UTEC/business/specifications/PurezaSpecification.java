package utec.proyectofinal.Proyecto.Final.UTEC.business.specifications;

import org.springframework.data.jpa.domain.Specification;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PurezaSpecification {

    /**
     * Crea una especificación para filtrar análisis de pureza
     * 
     * @param searchTerm Término de búsqueda (ficha del lote, ID)
     * @param activo Estado activo (true/false/null para todos)
     * @param estado Estado del análisis (REGISTRADO, PENDIENTE_APROBACION, APROBADO, A_REPETIR)
     * @param loteId ID del lote
     * @return Specification para usar con el repositorio
     */
    public static Specification<Pureza> conFiltros(String searchTerm, Boolean activo, String estado, Long loteId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro de búsqueda: busca en ID de análisis y ficha del lote
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                
                // Buscar por ID (si es numérico)
                Predicate idPredicate = null;
                try {
                    Long id = Long.parseLong(searchTerm.trim());
                    idPredicate = criteriaBuilder.equal(root.get("analisisID"), id);
                } catch (NumberFormatException e) {
                    // No es un número, ignorar búsqueda por ID
                }
                
                // Buscar por ficha del lote
                Predicate fichaPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lote").get("ficha")), searchPattern);
                
                if (idPredicate != null) {
                    predicates.add(criteriaBuilder.or(idPredicate, fichaPredicate));
                } else {
                    predicates.add(fichaPredicate);
                }
            }

            // Filtro por estado activo
            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            // Filtro por estado del análisis
            if (estado != null && !estado.trim().isEmpty() && !"todos".equalsIgnoreCase(estado)) {
                try {
                    Estado estadoEnum = Estado.valueOf(estado.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("estado"), estadoEnum));
                } catch (IllegalArgumentException e) {
                    // Estado inválido, ignorar
                }
            }

            // Filtro por lote
            if (loteId != null) {
                predicates.add(criteriaBuilder.equal(root.get("lote").get("loteID"), loteId));
            }

            // Ordenar por fecha de inicio descendente
            query.orderBy(criteriaBuilder.desc(root.get("fechaInicio")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
