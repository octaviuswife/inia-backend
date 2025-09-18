package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class TablaGermRequestDTO {
    private Integer total;
    private List<BigDecimal> promedioSinRedondeo;
    
    // Campos de porcentaje con redondeo (5 campos ingresados manualmente)
    private BigDecimal porcentajeNormalesConRedondeo;
    private BigDecimal porcentajeAnormalesConRedondeo;
    private BigDecimal porcentajeDurasConRedondeo;
    private BigDecimal porcentajeFrescasConRedondeo;
    private BigDecimal porcentajeMuertasConRedondeo;

    private LocalDate fechaFinal;

    // Campos movidos desde Germinacion
    private String tratamiento;
    private String productoYDosis;
    private Integer numSemillasPRep;
    private String metodo;
    private Double temperatura;
    private String prefrio;
    private String pretratamiento;
}