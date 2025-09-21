package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GerminacionDTO extends AnalisisDTO{
    private LocalDate fechaInicio;

    private List<LocalDate> fechaConteos;

    private LocalDate fechaFin;

    private String numDias;
    
    // Nuevos campos de control
    private Integer numeroRepeticiones;
    private Integer numeroConteos;
}
