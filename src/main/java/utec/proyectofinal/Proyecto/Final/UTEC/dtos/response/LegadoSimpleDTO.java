package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class LegadoSimpleDTO {
    private Long legadoID;
    private String nomLote;
    private String ficha;
    private String codDoc;
    private String nomDoc;
    private String familia;
    private Boolean activo;
}
