package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class CultivarRequestDTO {
    private Long especieID;
    private String nombre;
    private Boolean activo = true;
}