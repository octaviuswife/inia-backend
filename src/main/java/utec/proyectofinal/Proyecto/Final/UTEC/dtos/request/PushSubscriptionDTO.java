package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class PushSubscriptionDTO {
    private String endpoint;
    private Long expirationTime;
    private Keys keys;
    
    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
