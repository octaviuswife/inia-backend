package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class ClienteDTO {
    private Integer clienteID;
    private String nombre;
    private String contacto;
    private String email;
}
