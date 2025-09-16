package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ContGermDTO {
    private Long contGermID;

    private List<RepGerm> repGerm;

    private Integer total;
    private List<BigDecimal> promedioConRedondeo;

    private List<ValoresGermDTO> valoresGerm;

}
