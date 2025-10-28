package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CuscutaRegistroDTO {
    private Long id;
    private Instituto instituto;
    private BigDecimal cuscuta_g;
    private Integer cuscutaNum;
    private LocalDate fechaCuscuta;
}
