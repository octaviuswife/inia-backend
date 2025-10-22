package utec.proyectofinal.Proyecto.Final.UTEC.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class PushNotificationConfig {
    
    @Value("${push.vapid.public.key:}")
    private String publicKey;
    
    @Value("${push.vapid.private.key:}")
    private String privateKey;
    
    @Value("${push.vapid.subject:mailto:admin@inia.org.uy}")
    private String subject;
    
    @Value("${push.ttl:86400}")
    private int ttl;
    
    public boolean isConfigured() {
        return publicKey != null && !publicKey.isEmpty() 
            && privateKey != null && !privateKey.isEmpty();
    }
}
