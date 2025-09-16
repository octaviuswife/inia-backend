package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

@Data
public class UsuarioDTO {
    private Integer usuarioID;

    private String nombre;
    private String email;

    private String contrasenia;
    private Rol rol;
}
