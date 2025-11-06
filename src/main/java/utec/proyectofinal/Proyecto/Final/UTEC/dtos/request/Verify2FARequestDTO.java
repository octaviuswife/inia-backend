package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

/**
 * DTO para verificar código 2FA durante el login
 */
@Data
public class Verify2FARequestDTO {
    private String totpCode;           // Código de 6 dígitos de Google Authenticator
    private String deviceFingerprint;  // Fingerprint del dispositivo (opcional)
    private Boolean trustDevice;       // Si el usuario quiere confiar en este dispositivo
}
