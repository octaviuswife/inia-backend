package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de respuesta al configurar 2FA
 * Contiene el QR code y el secret key para configurar Google Authenticator
 */
@Data
@AllArgsConstructor
public class Setup2FAResponseDTO {
    private String secret;          // Secret key en Base32 (para manual setup)
    private String qrCodeDataUrl;   // QR code en formato Base64 data URL
    private String issuer;          // Nombre de la app (INIA)
    private String accountName;     // Nombre del usuario
}
