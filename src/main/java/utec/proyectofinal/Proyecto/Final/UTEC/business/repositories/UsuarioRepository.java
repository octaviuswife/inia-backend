package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByNombreIgnoreCase(String nombre);
    
    Optional<Usuario> findByEmailIgnoreCase(String email);
    
    boolean existsByNombreIgnoreCase(String nombre);
    
    boolean existsByEmailIgnoreCase(String email);
}