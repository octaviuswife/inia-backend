package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

/**
 * DTO para resetear contraseña con código de recuperación y 2FA
 */
@Data
public class ResetPasswordRequestDTO {
    private String email;              // Email del usuario
    private String recoveryCode;       // Código de 8 caracteres enviado por email
    private String totpCode;           // Código de 6 dígitos de Google Authenticator
    private String newPassword;        // Nueva contraseña
}
