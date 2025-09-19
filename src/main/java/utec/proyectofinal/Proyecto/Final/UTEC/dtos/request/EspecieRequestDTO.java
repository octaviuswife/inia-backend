package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class EspecieRequestDTO {
    private String nombreCientifico;
    private String nombreComun;
    private Boolean activo = true;
}