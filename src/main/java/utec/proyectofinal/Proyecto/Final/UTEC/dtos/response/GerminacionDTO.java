package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GerminacionDTO extends AnalisisDTO{
    // Campos específicos de Germinación
    private LocalDate fechaInicioGerm; // Fecha de inicio específica de germinación
    private List<LocalDate> fechaConteos;
    private LocalDate fechaUltConteo; // Fecha del último conteo
    private String numDias;
    
    // Nuevos campos de control
    private Integer numeroRepeticiones;
    private Integer numeroConteos;
}
