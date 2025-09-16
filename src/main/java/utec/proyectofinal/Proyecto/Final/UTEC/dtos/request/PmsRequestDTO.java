package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PmsRequestDTO {
    private Long idLote;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean publicadoParcial;
    private Boolean cumpleEstandar;
    private String comentarios;

    private BigDecimal promedio100g;
    private BigDecimal desvioStd;
    private BigDecimal coefVariacion;
    private BigDecimal pmssinRedon;
    private BigDecimal pmsconRedon;

    private List<RepPmsRequestDTO> repPms;
}

