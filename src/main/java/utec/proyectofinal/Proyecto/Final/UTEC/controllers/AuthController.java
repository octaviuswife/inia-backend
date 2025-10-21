package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación y Gestión de Usuarios", description = "Endpoints para autenticación, registro y gestión de usuarios")
public class AuthController {

    @Autowired
    private SeguridadService seguridadService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginData) {
        try {
            String usuario = loginData.getUsuario();
            String password = loginData.getPassword();
            
            Optional<Usuario> usuarioOpt = seguridadService.autenticarUsuario(usuario, password);

            if (usuarioOpt.isPresent()) {
                Usuario user = usuarioOpt.get();
                String[] roles = seguridadService.listarRolesPorUsuario(user);
                
                String token = jwtUtil.generarToken(user, java.util.Arrays.asList(roles));
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("tipo", "Bearer");
                response.put("usuario", Map.of(
                    "id", user.getUsuarioID(),
                    "nombre", user.getNombre(),
                    "nombres", user.getNombres(),
                    "apellidos", user.getApellidos(),
                    "email", user.getEmail(),
                    "roles", roles
                ));
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
                    
        } catch (RuntimeException e) {
            String mensaje = switch (e.getMessage()) {
                case "USUARIO_INCORRECTO" -> "Usuario no encontrado";
                case "USUARIO_INACTIVO" -> "Usuario inactivo";
                case "USUARIO_PENDIENTE_APROBACION" -> "Usuario pendiente de aprobación por el administrador";
                case "USUARIO_SIN_ROL" -> "Usuario sin rol asignado. Contacte al administrador";
                case "CONTRASENIA_INCORRECTA" -> "Contraseña incorrecta";
                default -> "Error de autenticación";
            };
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", mensaje));
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida si un token JWT es válido")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            
            if (jwtUtil.esTokenValido(token)) {
                String username = jwtUtil.obtenerUsuarioDelToken(token);
                return ResponseEntity.ok(Map.of(
                    "valido", true,
                    "usuario", username
                ));
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "error", "Token inválido"));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "error", "Token malformado"));
        }
    }

    // === ENDPOINTS DE REGISTRO Y GESTIÓN DE USUARIOS ===

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Solicita el registro de un nuevo usuario (pendiente de aprobación)")
    public ResponseEntity<?> registrar(@RequestBody RegistroUsuarioRequestDTO solicitud) {
        try {
            UsuarioDTO usuarioRegistrado = usuarioService.registrarSolicitud(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Solicitud de registro enviada. Pendiente de aprobación por el administrador.",
                        "usuario", usuarioRegistrado
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar solicitudes pendientes", description = "Lista todas las solicitudes de registro pendientes de aprobación")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UsuarioDTO>> listarSolicitudesPendientes() {
        List<UsuarioDTO> solicitudes = usuarioService.listarSolicitudesPendientes();
        return ResponseEntity.ok(solicitudes);
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar usuario", description = "Aprueba un usuario registrado y le asigna un rol")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> aprobarUsuario(@PathVariable Integer id, @RequestBody AprobarUsuarioRequestDTO solicitud) {
        try {
            UsuarioDTO usuarioAprobado = usuarioService.aprobarUsuario(id, solicitud);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario aprobado exitosamente",
                "usuario", usuarioAprobado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar solicitud", description = "Rechaza y elimina una solicitud de registro")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> rechazarSolicitud(@PathVariable Integer id) {
        try {
            usuarioService.rechazarSolicitud(id);
            return ResponseEntity.ok(Map.of("mensaje", "Solicitud rechazada"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios del sistema")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/users/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios activos", description = "Lista todos los usuarios activos del sistema")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UsuarioDTO>> listarUsuariosActivos() {
        List<UsuarioDTO> usuarios = usuarioService.listarUsuariosActivos();
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gestionar usuario", description = "Actualiza el rol o estado de un usuario")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> gestionarUsuario(@PathVariable Integer id, @RequestBody GestionarUsuarioRequestDTO solicitud) {
        try {
            UsuarioDTO usuarioActualizado = usuarioService.gestionarUsuario(id, solicitud);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario actualizado exitosamente",
                "usuario", usuarioActualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // === ENDPOINTS DE GESTIÓN DE PERFIL ===

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener perfil", description = "Obtiene el perfil del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> obtenerPerfil() {
        try {
            UsuarioDTO perfil = usuarioService.obtenerPerfil();
            return ResponseEntity.ok(perfil);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Actualizar perfil", description = "Actualiza el perfil del usuario autenticado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> actualizarPerfil(@RequestBody ActualizarPerfilRequestDTO solicitud) {
        try {
            UsuarioDTO perfilActualizado = usuarioService.actualizarPerfil(solicitud);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Perfil actualizado exitosamente",
                "usuario", perfilActualizado
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // === ADMIN PREDETERMINADO ===

    @PostMapping("/init-admin")
    @Operation(summary = "Crear admin inicial", description = "Crea el administrador predeterminado si no existe")
    public ResponseEntity<?> crearAdminPredeterminado() {
        try {
            UsuarioDTO admin = usuarioService.crearAdminPredeterminado();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                        "mensaje", "Administrador predeterminado creado. Usuario: 'admin', Contraseña: 'admin123'",
                        "usuario", admin
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}