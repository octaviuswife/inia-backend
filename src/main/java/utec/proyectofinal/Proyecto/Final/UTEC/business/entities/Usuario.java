package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usuarioID;

    private String nombre;
    private String email;

    private String contrasenia;
    private String rol; // esto va con enum despues

}
