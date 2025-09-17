package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;


import lombok.Data;

import java.time.LocalDate;

@Data
public class TetrazolioDTO extends AnalisisDTO {
    private Integer numSemillasPorRep;
    private String pretratamiento;
    private String concentracion;
    private Integer tincionHs;
    private Integer tincionTemp;
    private LocalDate fecha;
}
