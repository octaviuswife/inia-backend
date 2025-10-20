package utec.proyectofinal.Proyecto.Final.UTEC.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisPendienteDTO {
    private Long loteID;
    private String nomLote;
    private String ficha;
    private String especieNombre;
    private String cultivarNombre;
    private TipoAnalisis tipoAnalisis;
}
