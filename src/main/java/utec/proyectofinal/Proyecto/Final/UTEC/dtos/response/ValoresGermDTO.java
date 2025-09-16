package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValoresGermDTO {
    private Long valoresGermID;
    private String tipo;
    private BigDecimal normales;
    private BigDecimal anormales;
    private BigDecimal duras;
    private BigDecimal frescas;
    private BigDecimal muertas;
    private BigDecimal germinacion;
}
