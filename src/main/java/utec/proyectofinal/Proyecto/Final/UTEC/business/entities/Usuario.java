package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;
import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
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
    @Column(nullable = true)  // Permitir null para usuarios pendientes
    private Rol rol;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.PENDIENTE;
    
    @Column(nullable = false)
    private Boolean activo = true; // Mantenemos por compatibilidad
    
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
        if (rol == null) {
            return Arrays.asList(); // Usuario sin rol asignado
        }
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
    
    // Métodos para gestión de estados
    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }
    
    public boolean estaPendiente() {
        return estado == EstadoUsuario.PENDIENTE;
    }
    
    public boolean estaInactivo() {
        return estado == EstadoUsuario.INACTIVO;
    }
}
