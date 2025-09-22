package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class RegistroUsuarioRequestDTO {
    private String nombre;         // username único
    private String nombres;        // nombre(s) de pila
    private String apellidos;      // apellidos
    private String email;          // email único
    private String contrasenia;    // contraseña
}