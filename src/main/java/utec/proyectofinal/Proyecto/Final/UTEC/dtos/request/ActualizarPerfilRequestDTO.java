package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class ActualizarPerfilRequestDTO {
    private String nombres;        // nombre(s) de pila
    private String apellidos;      // apellidos
    private String email;          // email único
    private String contraseniaActual;  // contraseña actual (para verificación)
    private String contraseniaNueva;   // nueva contraseña (opcional)
}