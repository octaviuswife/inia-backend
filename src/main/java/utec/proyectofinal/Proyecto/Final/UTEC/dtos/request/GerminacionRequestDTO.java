package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GerminacionRequestDTO extends AnalisisRequestDTO {
    // Campos específicos de Germinación
    private LocalDate fechaInicioGerm; // Fecha de inicio específica de germinación
    private List<LocalDate> fechaConteos;
    private LocalDate fechaUltConteo; // Fecha del último conteo
    private String numDias; // Calculado y enviado desde el frontend
    
    // Nuevos campos de control
    private Integer numeroRepeticiones;
    private Integer numeroConteos;
}