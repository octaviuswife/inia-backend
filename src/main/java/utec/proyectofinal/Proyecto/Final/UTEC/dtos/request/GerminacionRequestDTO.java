package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GerminacionRequestDTO extends AnalisisRequestDTO {
    // La entidad Germinación ahora es solo un contenedor de TablaGerm
    // Los campos específicos se manejan a nivel de TablaGerm
}