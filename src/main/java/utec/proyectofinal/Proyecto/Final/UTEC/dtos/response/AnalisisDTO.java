package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class AnalisisDTO {

    private Long analisisID;

    private String lote;

    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private String comentarios;

    private List<AnalisisHistorialDTO> historial;
}
