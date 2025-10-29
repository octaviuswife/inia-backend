package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGerminacionDTO {
    private Map<String, Double> mediaGerminacionPorEspecie;
    private Map<String, Double> tiempoPromedioPrimerConteo;
    private Map<String, Double> tiempoPromedioUltimoConteo;
    private Long totalGerminaciones;
}
