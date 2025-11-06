package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

/**
 * DTO extendido para login con soporte 2FA
 */
@Data
public class Login2FARequestDTO {
    private String usuario;              // nombre de usuario o email
    private String password;             // contraseña
    private String totpCode;             // código 2FA de 6 dígitos (requerido si el usuario tiene 2FA)
    private String deviceFingerprint;    // fingerprint del dispositivo
    private Boolean trustDevice;         // si quiere marcar el dispositivo como de confianza
}
