package utec.proyectofinal.Proyecto.Final.UTEC.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PasswordService;

import java.util.List;
import java.util.Optional;

@Service
public class SeguridadService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordService passwordService;

    public Optional<Usuario> autenticarUsuario(String usuario, String password) {
        Optional<Usuario> objUsuario = usuarioRepository.findByNombreIgnoreCase(usuario);

        if (objUsuario.isEmpty()) {
            throw new RuntimeException("USUARIO_INCORRECTO");
        }

        Usuario usuarioEncontrado = objUsuario.get();

        if (!usuarioEncontrado.getActivo()) {
            throw new RuntimeException("USUARIO_INACTIVO");
        }

        if (!passwordService.matchPassword(password, usuarioEncontrado.getContrasenia())) {
            throw new RuntimeException("CONTRASENIA_INCORRECTA");
        }

        return objUsuario;
    }

    public String[] listarRolesPorUsuario(Usuario usuario) {
        List<String> roles = usuario.getRoles();
        return roles.toArray(new String[0]);
    }

    public boolean existeUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreIgnoreCase(nombreUsuario).isPresent();
    }

    public boolean existeEmailActivo(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email).isPresent();
    }
}
