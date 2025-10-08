package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class TablaGermDTO {
    private Long tablaGermID;

    private List<RepGermDTO> repGerm;

    private Integer total;
    private List<BigDecimal> promedioSinRedondeo;
    private List<BigDecimal> promediosSinRedPorConteo;

    // Campos de porcentaje con redondeo (5 campos ingresados manualmente)
    private BigDecimal porcentajeNormalesConRedondeo;
    private BigDecimal porcentajeAnormalesConRedondeo;
    private BigDecimal porcentajeDurasConRedondeo;
    private BigDecimal porcentajeFrescasConRedondeo;
    private BigDecimal porcentajeMuertasConRedondeo;

    private List<ValoresGermDTO> valoresGerm;

    private LocalDate fechaFinal;

    // Campo de control para finalización
    private Boolean finalizada;

    // Campos movidos desde Germinacion
    private String tratamiento;
    private String productoYDosis;
    private Integer numSemillasPRep;
    private String metodo;
    private Double temperatura;
    private String prefrio;
    private String pretratamiento;
}