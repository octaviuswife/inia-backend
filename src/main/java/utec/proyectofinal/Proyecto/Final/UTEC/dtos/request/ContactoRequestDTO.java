package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;

@Data
public class ContactoRequestDTO {
    private String nombre;
    private String contacto;
    private TipoContacto tipo;
}