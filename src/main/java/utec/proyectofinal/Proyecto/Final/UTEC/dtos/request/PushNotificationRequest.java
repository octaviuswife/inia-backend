package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PushNotificationRequest {
    
    @NotBlank(message = "El título es obligatorio")
    private String title;
    
    @NotBlank(message = "El cuerpo del mensaje es obligatorio")
    private String body;
    
    private String icon;
    private String badge;
    private String image;
    private String url;
    private Long analisisId;
}
