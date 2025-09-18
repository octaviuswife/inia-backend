package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ContGermRequestDTO {
    private Integer total;
    private List<BigDecimal> promedioConRedondeo;
}