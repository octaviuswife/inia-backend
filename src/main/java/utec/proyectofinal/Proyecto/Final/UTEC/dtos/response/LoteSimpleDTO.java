package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class LoteSimpleDTO {
    private Long loteID;
    private Integer numeroFicha;
    private String ficha;
    private Boolean activo;
}