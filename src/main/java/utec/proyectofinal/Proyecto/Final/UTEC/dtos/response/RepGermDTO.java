package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.util.List;

@Data
public class RepGermDTO {
    private Long repGermID;

    private Integer numRep;

    private List<Integer> normales;

    private Integer anormales;
    private Integer duras;
    private Integer frescas;
    private Integer muertas;
    private Integer total;
}
