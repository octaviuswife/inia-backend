package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RepPmsRequestDTO {
    private Integer numRep;
    private Integer numTanda;
    private BigDecimal peso;
}
