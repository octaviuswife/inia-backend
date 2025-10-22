package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {
    private String title;
    private String body;
    private String icon;
    private String badge;
    private String image;
    private String url;
    private String tag;
    private Long notificationId;
    private Boolean requireInteraction;
    private List<Action> actions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {
        private String action;
        private String title;
        private String icon;
    }
}
