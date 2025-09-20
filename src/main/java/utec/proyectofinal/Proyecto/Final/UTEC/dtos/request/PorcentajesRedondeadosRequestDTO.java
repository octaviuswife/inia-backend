package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PorcentajesRedondeadosRequestDTO {
    private BigDecimal porcViablesRedondeo;
    private BigDecimal porcNoViablesRedondeo;
    private BigDecimal porcDurasRedondeo;
}