package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoDOSN;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DosnDTO extends AnalisisDTO {
    private Boolean cumpleEstandar;
    private LocalDate fechaINIA;
    private BigDecimal gramosAnalizadosINIA;

    private List<TipoDOSN> tipoINIA;

    private LocalDate fechaINASE;
    private BigDecimal gramosAnalizadosINASE;

    private List<TipoDOSN> tipoINASE;

    private BigDecimal cuscuta_g;
    private Integer cuscutaNum;
    private LocalDate fechaCuscuta;
    private Instituto institutoCuscuta;

    private List<ListadoDTO> listados;
}
