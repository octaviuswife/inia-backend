package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CuscutaRegistroRequestDTO {
    private Instituto instituto;
    private BigDecimal cuscuta_g;
    private Integer cuscutaNum;
    private LocalDate fechaCuscuta;
}
