package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AnalisisRequestDTO {
    private Long idLote;
    // fechaInicio y fechaFin son automáticas, no se incluyen en el request
    private Boolean cumpleEstandar;
    private String comentarios;
}