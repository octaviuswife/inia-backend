package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UsuarioDTO {
    private Integer usuarioID;
    private String nombre;
    private String nombres;
    private String apellidos;
    private String email;
    
    // Campo original (mantener por compatibilidad)
    private Rol rol;
    
    // Array de roles para el frontend
    private List<String> roles;
    
    private EstadoUsuario estado;
    
    // Alias para estadoSolicitud (lo que espera el frontend)
    private String estadoSolicitud;
    
    private Boolean activo;
    
    // Campo original
    private LocalDateTime fechaCreacion;
    
    // Alias para fechaRegistro (lo que espera el frontend)
    private String fechaRegistro;
    
    private LocalDateTime fechaUltimaConexion;
    private String nombreCompleto;
}
