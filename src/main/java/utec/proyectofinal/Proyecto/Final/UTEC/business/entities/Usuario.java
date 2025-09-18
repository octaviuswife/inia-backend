package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;
import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "Usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usuarioID;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre;
    
    @Column(nullable = false, length = 100)
    private String nombres;
    
    @Column(nullable = false, length = 100)
    private String apellidos;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String contrasenia;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_ultima_conexion")
    private LocalDateTime fechaUltimaConexion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    // Método para obtener roles como lista (compatibilidad con JWT)
    public List<String> getRoles() {
        return Arrays.asList("ROLE_" + rol.name());
    }
    
    // Método para obtener el nombre completo
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
    
    // Métodos de conveniencia para verificar roles
    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
    
    public boolean esAnalista() {
        return rol == Rol.ANALISTA;
    }
    
    public boolean esObservador() {
        return rol == Rol.OBSERVADOR;
    }
    
    public boolean puedeCrearEditar() {
        return rol == Rol.ADMIN || rol == Rol.ANALISTA;
    }
    
    public boolean puedeAprobar() {
        return rol == Rol.ADMIN;
    }
}
