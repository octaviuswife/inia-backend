package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;

@Data
public class ContactoDTO {
    private Long contactoID;
    private String nombre;
    private String contacto;
    private TipoContacto tipo;
    private Boolean activo;
}