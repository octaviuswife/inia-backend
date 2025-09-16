package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;


import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepTetrazolioViabilidad;

import java.time.LocalDate;
import java.util.List;

@Data
public class TetrazolioDTO extends AnalisisDTO {
    private Integer numSemillasPorRep;
    private String pretratamiento;
    private String concentracion;
    private Integer tincionHs;
    private Integer tincionTemp;
    private LocalDate fecha;

    private List<RepTetrazolioViabilidad> repeticiones;
}
