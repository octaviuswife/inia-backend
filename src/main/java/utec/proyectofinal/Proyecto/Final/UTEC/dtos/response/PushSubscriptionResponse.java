package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PushSubscriptionResponse {
    private Long id;
    private Long usuarioId;
    private String endpoint;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
