package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class MalezasYCultivosCatalogoRequestDTO {
    private String nombreComun;
    private String nombreCientifico;
    private Boolean maleza;
    private Boolean activo = true;
}