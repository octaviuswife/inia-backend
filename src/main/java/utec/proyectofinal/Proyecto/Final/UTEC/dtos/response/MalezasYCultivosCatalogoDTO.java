package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoMYCCatalogo;

@Data
public class MalezasYCultivosCatalogoDTO {
    private Long catalogoID;
    private String nombreComun;
    private String nombreCientifico;
    private TipoMYCCatalogo tipoMYCCatalogo;
    // Campo activo removido intencionalmente - no necesario en responses
}
