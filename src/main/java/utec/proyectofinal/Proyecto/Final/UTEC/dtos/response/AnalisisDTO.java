package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public abstract class AnalisisDTO {

    private Integer analisisID;

    private String lote;

    private String tipo;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Boolean publicadoParcial;
    private Boolean cumpleEstandar;

    private String comentarios;

    private List<AnalisisHistorialDTO> historial;
}
