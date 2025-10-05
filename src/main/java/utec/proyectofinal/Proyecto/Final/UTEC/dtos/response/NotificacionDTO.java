package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion;

import java.time.LocalDateTime;

@Data
public class NotificacionDTO {
    private Long id;
    private String nombre;
    private String mensaje;
    private Boolean leido;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private Long usuarioId;
    private String usuarioNombre;
    private Long analisisId;
    private TipoNotificacion tipo;
}