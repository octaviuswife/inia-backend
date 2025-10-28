package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class MalezasCatalogoDTO {
    private Long catalogoID;
    private String nombreComun;
    private String nombreCientifico;
    // Campo activo removido intencionalmente - no necesario en responses
}
