package utec.proyectofinal.Proyecto.Final.UTEC.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SeguridadService seguridadService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 [UserDetailsService] Cargando usuario: " + username);

        // Buscar por nombre (case insensitive)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByNombreIgnoreCase(username);

        if (usuarioOpt.isEmpty()) {
            System.out.println("❌ [UserDetailsService] Usuario no encontrado: " + username);
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        Usuario usuario = usuarioOpt.get();

        System.out.println("✅ [UserDetailsService] Usuario encontrado: " + usuario.getNombre());
        System.out.println("📊 [UserDetailsService] Estado: " + usuario.getEstado());
        System.out.println("📊 [UserDetailsService] Activo: " + usuario.getActivo());

        // Verificar estado del usuario
        if (!usuario.getActivo() || !"ACTIVO".equals(usuario.getEstado().toString())) {
            System.out.println("❌ [UserDetailsService] Usuario no activo");
            throw new UsernameNotFoundException("Usuario inactivo o pendiente de aprobación");
        }

        // Obtener roles
        String[] roles = seguridadService.listarRolesPorUsuario(usuario);
        System.out.println("🎫 [UserDetailsService] Roles: " + Arrays.toString(roles));

        // Convertir roles a authorities con prefijo ROLE_
        Collection<GrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> {
                    // Si el rol ya tiene ROLE_, no agregarlo de nuevo
                    String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    return new SimpleGrantedAuthority(roleWithPrefix);
                })
                .collect(Collectors.toList());

        System.out.println("✅ [UserDetailsService] Authorities: " + authorities);

        // Retornar UserDetails
        return new org.springframework.security.core.userdetails.User(
                usuario.getNombre(),
                usuario.getContrasenia(),
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}