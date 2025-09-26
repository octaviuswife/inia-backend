package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoMYCCatalogo;

@Data
public class MalezasYCultivosCatalogoRequestDTO {
    private String nombreComun;
    private String nombreCientifico;
    private TipoMYCCatalogo tipoMYCCatalogo;
}