package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {
    private String title;
    private String body;
    private String icon;
    private String url;
    private String tag;
    private Map<String, String> data;  // ← AGREGAR ESTO para metadata
}