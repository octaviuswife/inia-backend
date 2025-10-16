package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class LoteSimpleDTO {
    private Long loteID;
    private String ficha;
    private String nomLote;
    private Boolean activo;
    private String cultivarNombre;
    private String especieNombre;
}