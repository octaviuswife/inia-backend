package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

/**
 * DTO para solicitar recuperación de contraseña
 */
@Data
public class ForgotPasswordRequestDTO {
    private String email;  // Email del usuario que olvidó su contraseña
}
