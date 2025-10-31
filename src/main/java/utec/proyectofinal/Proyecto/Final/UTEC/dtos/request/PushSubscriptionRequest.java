package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PushSubscriptionRequest {
    
    @NotBlank(message = "El endpoint es obligatorio")
    private String endpoint;
    
    @NotBlank(message = "La clave p256dh es obligatoria")
    private String p256dh;
    
    @NotBlank(message = "La clave auth es obligatoria")
    private String auth;
    
    private String userAgent;
}
