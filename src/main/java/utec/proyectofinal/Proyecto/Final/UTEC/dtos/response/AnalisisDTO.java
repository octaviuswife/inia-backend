package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.util.List;

@Data
public abstract class AnalisisDTO {

    private Integer analisisID;

    private String lote;

    private Estado estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Boolean publicadoParcial;
    private Boolean cumpleEstandar;

    private String comentarios;

    private List<AnalisisHistorialDTO> historial;
}
