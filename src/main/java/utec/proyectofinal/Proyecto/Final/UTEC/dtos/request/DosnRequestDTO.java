package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DosnRequestDTO {
    private Long idLote;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean publicadoParcial;
    private Boolean cumpleEstandar;
    private String comentarios;

    private LocalDate fechaINIA;
    private BigDecimal gramosAnalizadosINIA;
    private List<String> tipoINIA;

    private LocalDate fechaINASE;
    private BigDecimal gramosAnalizadosINASE;
    private List<String> tipoINASE;

    private BigDecimal cuscuta_g;
    private Integer cuscutaNum;
    private LocalDate fechaCuscuta;

    private List<ListadoRequestDTO> listados;
}
