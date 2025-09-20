package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepPmsDTO {
    private Long repPMSID;
    private Integer numRep;
    private Integer numTanda;
    private BigDecimal peso;
    private Boolean valido;
}
