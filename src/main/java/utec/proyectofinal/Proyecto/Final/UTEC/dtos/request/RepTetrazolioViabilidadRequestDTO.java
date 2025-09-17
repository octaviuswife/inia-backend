package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RepTetrazolioViabilidadRequestDTO {
    private LocalDate fecha;
    private Integer viablesNum;
    private Integer noViablesNum;
    private Integer duras;
}