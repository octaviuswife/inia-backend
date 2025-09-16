package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DosnDTO extends AnalisisDTO {
    private LocalDate fechaINIA;
    private BigDecimal gramosAnalizadosINIA;

    private List<String> tipoINIA;

    private LocalDate fechaINASE;
    private BigDecimal gramosAnalizadosINASE;

    private List<String> tipoINASE;

    private BigDecimal cuscuta_g;
    private Integer cuscutaNum;
    private LocalDate fechaCuscuta;

    private List<ListadoDTO> listados;
}
