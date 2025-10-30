package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoDOSN;

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

    private List<CuscutaRegistroDTO> cuscutaRegistros;

    private List<ListadoDTO> listados;
}
