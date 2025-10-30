package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportePurezaDTO {
    private Map<String, Long> contaminantesPorEspecie;
    private Map<String, Double> porcentajeMalezas;
    private Map<String, Double> porcentajeOtrasSemillas;
    private Map<String, Double> porcentajeCumpleEstandar;
    private Long totalPurezas;
}
