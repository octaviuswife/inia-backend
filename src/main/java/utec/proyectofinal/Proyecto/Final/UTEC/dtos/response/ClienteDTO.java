package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class ClienteDTO {
    private Long clienteID;
    private String nombre;
    private String contacto;
    private String email;
}
