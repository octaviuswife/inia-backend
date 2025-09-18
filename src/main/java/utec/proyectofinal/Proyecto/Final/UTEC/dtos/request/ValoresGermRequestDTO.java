package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ValoresGermRequestDTO {
    private BigDecimal normales;
    private BigDecimal anormales;
    private BigDecimal duras;
    private BigDecimal frescas;
    private BigDecimal muertas;
    private BigDecimal germinacion;
}