package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurezaRequestDTO {
    private Long idLote;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean publicadoParcial;
    private Boolean cumpleEstandar;
    private String comentarios;

    private LocalDate fecha;
    private BigDecimal pesoInicial_g;
    private BigDecimal semillaPura_g;
    private BigDecimal materiaInerte_g;
    private BigDecimal otrosCultivos_g;
    private BigDecimal malezas_g;
    private BigDecimal malezasToleradas_g;
    private BigDecimal pesoTotal_g;

    private BigDecimal redonSemillaPura;
    private BigDecimal redonMateriaInerte;
    private BigDecimal redonOtrosCultivos;
    private BigDecimal redonMalezas;
    private BigDecimal redonMalezasToleradas;
    private BigDecimal redonPesoTotal;

    private BigDecimal inaseValor;
    private LocalDate inaseFecha;

    private List<ListadoRequestDTO> otrasSemillas;
}
