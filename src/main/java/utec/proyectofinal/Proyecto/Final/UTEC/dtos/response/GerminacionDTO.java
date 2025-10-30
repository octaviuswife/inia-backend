package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GerminacionDTO extends AnalisisDTO{
    // La entidad Germinación ahora es solo un contenedor de TablaGerm
    // Los campos específicos se manejan a nivel de TablaGerm
}
