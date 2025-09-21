package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GerminacionRequestDTO extends AnalisisRequestDTO {
    // Campos específicos de Germinación
    private LocalDate fechaInicioGerminacion;
    private List<LocalDate> fechaConteos;
    private LocalDate fechaFinGerminacion;
    // numDias se calculará automáticamente como fechaFinGerminacion - fechaInicioGerminacion
    
    // Nuevos campos de control
    private Integer numeroRepeticiones;
    private Integer numeroConteos;
}