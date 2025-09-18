package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AnalisisRequestDTO {
    private Long idLote;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean cumpleEstandar;
    private String comentarios;
}