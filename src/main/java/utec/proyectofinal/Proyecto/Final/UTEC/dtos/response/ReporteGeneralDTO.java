package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGeneralDTO {
    private Long totalAnalisis;
    private Map<String, Long> analisisPorPeriodo;
    private Map<String, Long> analisisPorEstado;
    private Map<String, Double> porcentajeCompletitud;
    private Double tiempoMedioFinalizacion;
    private Map<String, Long> topAnalisisProblemas;
}
