package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class CatalogoDTO {
    private Long catalogoID;
    private String nombreComun;
    private String nombreCientifico;
    private Boolean maleza;
}
