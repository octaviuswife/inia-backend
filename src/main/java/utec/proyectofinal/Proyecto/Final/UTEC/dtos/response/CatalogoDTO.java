package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class CatalogoDTO {
    private Long id;
    private String tipo;
    private String valor;
    private Boolean activo;
}