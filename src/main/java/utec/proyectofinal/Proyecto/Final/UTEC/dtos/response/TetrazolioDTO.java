package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class TetrazolioDTO extends AnalisisDTO {
    private Integer numSemillasPorRep;
    private String pretratamiento;
    private String concentracion;
    private Integer tincionHs;
    private Integer tincionTemp;
    private LocalDate fecha;
    private Integer numRepeticionesEsperadas;
    private BigDecimal porcViablesRedondeo;
    private BigDecimal porcNoViablesRedondeo;
    private BigDecimal porcDurasRedondeo;
    private BigDecimal viabilidadInase;
}
