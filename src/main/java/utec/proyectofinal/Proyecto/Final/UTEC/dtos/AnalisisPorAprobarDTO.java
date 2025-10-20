package utec.proyectofinal.Proyecto.Final.UTEC.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisPorAprobarDTO {
    private TipoAnalisis tipo;
    private Long analisisID;
    private Long loteID;
    private String nomLote;
    private String ficha;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
