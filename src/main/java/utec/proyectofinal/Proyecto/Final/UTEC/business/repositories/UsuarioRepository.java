package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByNombre(String nombre);
    Optional<Usuario> findByNombreIgnoreCase(String nombre);
    
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    
    List<Usuario> findByEstado(EstadoUsuario estado);
    Optional<Usuario> findByRol(Rol rol);
    List<Usuario> findAllByRol(Rol rol);
    
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByEmailIgnoreCase(String email);
}