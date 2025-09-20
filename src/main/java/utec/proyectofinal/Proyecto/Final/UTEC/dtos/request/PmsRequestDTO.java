package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PmsRequestDTO extends AnalisisRequestDTO {
    // Campos de configuración inicial
    private Integer numRepeticionesEsperadas;
    private Integer numTandas; // Por defecto será 1
    private Boolean esSemillaBrozosa;
}

