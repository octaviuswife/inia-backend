package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PorcentajesRedondeoRequestDTO {
    // Campos de porcentaje con redondeo (5 campos ingresados manualmente)
    private BigDecimal porcentajeNormalesConRedondeo;
    private BigDecimal porcentajeAnormalesConRedondeo;
    private BigDecimal porcentajeDurasConRedondeo;
    private BigDecimal porcentajeFrescasConRedondeo;
    private BigDecimal porcentajeMuertasConRedondeo;
}