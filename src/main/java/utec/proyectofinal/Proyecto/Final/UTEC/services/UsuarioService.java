package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ActualizarPerfilRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.AprobarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GestionarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificacionService notificacionService;

    /**
     * Buscar usuario por ID
     */
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Registrar nueva solicitud de usuario
     */
    public UsuarioDTO registrarSolicitud(RegistroUsuarioRequestDTO solicitud) {
        // Validar que el username no exista
        if (usuarioRepository.findByNombre(solicitud.getNombre()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        // Validar que el email no exista
        if (usuarioRepository.findByEmail(solicitud.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear nuevo usuario en estado PENDIENTE
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.getNombre());
        usuario.setNombres(solicitud.getNombres());
        usuario.setApellidos(solicitud.getApellidos());
        usuario.setEmail(solicitud.getEmail());
        usuario.setContrasenia(passwordEncoder.encode(solicitud.getContrasenia()));
        usuario.setEstado(EstadoUsuario.PENDIENTE);
        usuario.setRol(null); // Sin rol hasta ser aprobado
        usuario.setActivo(false); // Inactivo hasta ser aprobado

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Crear notificación automática para el nuevo usuario registrado
        try {
            notificacionService.notificarNuevoUsuario(usuarioGuardado.getUsuarioID().longValue());
        } catch (Exception e) {
            // Log error but don't fail the registration
            System.err.println("Error creating notification for new user: " + e.getMessage());
        }
        
        return mapearEntidadADTO(usuarioGuardado);
    }

    /**
     * Listar solicitudes pendientes de aprobación
     */
    public List<UsuarioDTO> listarSolicitudesPendientes() {
        return usuarioRepository.findByEstado(EstadoUsuario.PENDIENTE)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    /**
     * Aprobar usuario y asignar rol
     */
    public UsuarioDTO aprobarUsuario(Integer usuarioId, AprobarUsuarioRequestDTO solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new RuntimeException("Solo se pueden aprobar usuarios en estado PENDIENTE");
        }

        usuario.setRol(solicitud.getRol());
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setActivo(true);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        
        // Crear notificación automática para aprobación de usuario
        try {
            notificacionService.notificarUsuarioAprobado(usuarioActualizado.getUsuarioID().longValue());
        } catch (Exception e) {
            // Log error but don't fail the approval
            System.err.println("Error creating notification for user approval: " + e.getMessage());
        }
        
        return mapearEntidadADTO(usuarioActualizado);
    }

    /**
     * Rechazar solicitud de usuario
     */
    public void rechazarSolicitud(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new RuntimeException("Solo se pueden rechazar usuarios en estado PENDIENTE");
        }

        // Crear notificación automática para rechazo de usuario
        try {
            notificacionService.notificarUsuarioRechazado(usuario.getUsuarioID().longValue());
        } catch (Exception e) {
            // Log error but continue with rejection
            System.err.println("Error creating notification for user rejection: " + e.getMessage());
        }

        usuarioRepository.delete(usuario);
    }

    /**
     * Listar todos los usuarios (para administración)
     */
    public List<UsuarioDTO> listarTodosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    /**
     * Listar usuarios activos
     */
    public List<UsuarioDTO> listarUsuariosActivos() {
        return usuarioRepository.findByEstado(EstadoUsuario.ACTIVO)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    /**
     * Gestionar usuario (cambiar rol o estado)
     */
    public UsuarioDTO gestionarUsuario(Integer usuarioId, GestionarUsuarioRequestDTO solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar rol si se proporciona
        if (solicitud.getRol() != null) {
            usuario.setRol(solicitud.getRol());
        }

        // Actualizar estado si se proporciona
        if (solicitud.getEstado() != null) {
            usuario.setEstado(solicitud.getEstado());
            // Sincronizar campo activo con estado
            usuario.setActivo(solicitud.getEstado() == EstadoUsuario.ACTIVO);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapearEntidadADTO(usuarioActualizado);
    }

    /**
     * Obtener perfil del usuario actual
     */
    public UsuarioDTO obtenerPerfil() {
        Usuario usuario = obtenerUsuarioActual();
        return mapearEntidadADTO(usuario);
    }

    /**
     * Actualizar perfil del usuario actual
     */
    public UsuarioDTO actualizarPerfil(ActualizarPerfilRequestDTO solicitud) {
        Usuario usuario = obtenerUsuarioActual();

        // Verificar contraseña actual si se va a cambiar
        if (solicitud.getContraseniaNueva() != null && !solicitud.getContraseniaNueva().isEmpty()) {
            if (solicitud.getContraseniaActual() == null || solicitud.getContraseniaActual().isEmpty()) {
                throw new RuntimeException("Debe proporcionar la contraseña actual para cambiarla");
            }
            
            if (!passwordEncoder.matches(solicitud.getContraseniaActual(), usuario.getContrasenia())) {
                throw new RuntimeException("Contraseña actual incorrecta");
            }
            
            usuario.setContrasenia(passwordEncoder.encode(solicitud.getContraseniaNueva()));
        }

        // Actualizar nombre de usuario (username) si se proporciona
        if (solicitud.getNombre() != null && !solicitud.getNombre().trim().isEmpty() 
            && !solicitud.getNombre().equalsIgnoreCase(usuario.getNombre())) {
            // Verificar que el nuevo nombre de usuario no exista
            Optional<Usuario> usuarioExistente = usuarioRepository.findByNombreIgnoreCase(solicitud.getNombre());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getUsuarioID().equals(usuario.getUsuarioID())) {
                throw new RuntimeException("El nombre de usuario ya está en uso por otro usuario");
            }
            usuario.setNombre(solicitud.getNombre());
        }

        // Actualizar datos del perfil
        if (solicitud.getNombres() != null && !solicitud.getNombres().trim().isEmpty()) {
            usuario.setNombres(solicitud.getNombres());
        }
        
        if (solicitud.getApellidos() != null && !solicitud.getApellidos().trim().isEmpty()) {
            usuario.setApellidos(solicitud.getApellidos());
        }
        
        if (solicitud.getEmail() != null && !solicitud.getEmail().trim().isEmpty() 
            && !solicitud.getEmail().equalsIgnoreCase(usuario.getEmail())) {
            // Verificar que el nuevo email no exista (case-insensitive)
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmailIgnoreCase(solicitud.getEmail());
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getUsuarioID().equals(usuario.getUsuarioID())) {
                throw new RuntimeException("El email ya está en uso por otro usuario");
            }
            usuario.setEmail(solicitud.getEmail());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return mapearEntidadADTO(usuarioActualizado);
    }

    /**
     * Crear admin predeterminado si no existe
     */
    public UsuarioDTO crearAdminPredeterminado() {
        // Verificar si ya existe un admin
        Optional<Usuario> adminExistente = usuarioRepository.findByRol(Rol.ADMIN);
        if (adminExistente.isPresent()) {
            throw new RuntimeException("Ya existe un administrador en el sistema");
        }

        // Crear admin predeterminado
        Usuario admin = new Usuario();
        admin.setNombre("admin");
        admin.setNombres("Administrador");
        admin.setApellidos("del Sistema");
        admin.setEmail("admin@inia.gub.uy");
        admin.setContrasenia(passwordEncoder.encode("admin123")); // Contraseña temporal
        admin.setRol(Rol.ADMIN);
        admin.setEstado(EstadoUsuario.ACTIVO);
        admin.setActivo(true);

        Usuario adminGuardado = usuarioRepository.save(admin);
        return mapearEntidadADTO(adminGuardado);
    }

    // === Métodos auxiliares ===

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        return usuarioRepository.findByNombre(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en base de datos"));
    }

    private UsuarioDTO mapearEntidadADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setUsuarioID(usuario.getUsuarioID());
        dto.setNombre(usuario.getNombre());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setEmail(usuario.getEmail());
        
        // Campo original
        dto.setRol(usuario.getRol());
        
        // Mapear rol a array de strings para el frontend
        if (usuario.getRol() != null) {
            dto.setRoles(List.of(usuario.getRol().name()));
        } else {
            dto.setRoles(List.of());
        }
        
        dto.setEstado(usuario.getEstado());
        
        // Mapear estado a string para el frontend
        if (usuario.getEstado() != null) {
            dto.setEstadoSolicitud(usuario.getEstado().name());
        }
        
        dto.setActivo(usuario.getActivo());
        
        // Campo original
        dto.setFechaCreacion(usuario.getFechaCreacion());
        
        // Mapear fechaCreacion a ISO string para el frontend
        if (usuario.getFechaCreacion() != null) {
            dto.setFechaRegistro(usuario.getFechaCreacion().toString());
        }
        
        dto.setFechaUltimaConexion(usuario.getFechaUltimaConexion());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        return dto;
    }
}