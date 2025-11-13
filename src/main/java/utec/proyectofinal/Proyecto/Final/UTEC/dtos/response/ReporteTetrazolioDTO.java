package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTetrazolioDTO {
    private Map<String, Double> viabilidadIniaPorEspecie;
    private Map<String, Double> viabilidadInasePorEspecie;
    private Long totalTetrazolios;
}
