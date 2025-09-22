package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.time.LocalDateTime;

@Data
public class UsuarioDTO {
    private Integer usuarioID;
    private String nombre;
    private String nombres;
    private String apellidos;
    private String email;
    private Rol rol;
    private EstadoUsuario estado;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaConexion;
    private String nombreCompleto;
}
