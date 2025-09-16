package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RepTetrazolioViabilidadDTO {
    private Long repTetrazolioViabID;
    private LocalDate fecha;
    private Integer viablesNum;
    private Integer noViablesNum;
    private Integer duras;
}
