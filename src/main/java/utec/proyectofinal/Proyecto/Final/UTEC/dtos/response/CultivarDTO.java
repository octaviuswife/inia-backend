package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;

@Data
public class CultivarDTO {
    private Long cultivarID;
    private Long especieID;
    private String especieNombre;
    private String nombre;
    private Boolean activo;
}
