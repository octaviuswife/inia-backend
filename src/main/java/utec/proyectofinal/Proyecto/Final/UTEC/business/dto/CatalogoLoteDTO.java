package utec.proyectofinal.Proyecto.Final.UTEC.business.dto;

import lombok.Data;

import java.util.List;

@Data
public class CatalogoLoteDTO {
    
    private Long catalogoLoteID;
    private String tipo;
    private List<String> datos;
    private Boolean activo;
}