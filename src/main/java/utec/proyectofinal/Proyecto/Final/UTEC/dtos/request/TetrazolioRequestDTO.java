package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TetrazolioRequestDTO {
    private Long idLote;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean cumpleEstandar;
    private String comentarios;

    private Integer numSemillasPorRep;
    private String pretratamiento;
    private String concentracion;
    private Integer tincionHs;
    private Integer tincionTemp;
    private LocalDate fecha;
}