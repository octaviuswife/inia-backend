package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ActualizarPerfilRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.AprobarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GestionarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoginRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000") // Ajusta el origen según tu configuración de frontend
@Tag(name = "Autenticación y Gestión de Usuarios", description = "Endpoints para autenticación, registro y gestión de usuarios")
public class AuthController {

    @Autowired
    private SeguridadService seguridadService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT en cookies HttpOnly")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginData, HttpServletResponse response) {
        System.out.println("🔐 [LOGIN] Iniciando proceso de login...");
        System.out.println("🔐 [LOGIN] Usuario recibido: " + loginData.getUsuario());
        
        try {
            String usuario = loginData.getUsuario();
            String password = loginData.getPassword();
            
            Optional<Usuario> usuarioOpt = seguridadService.autenticarUsuario(usuario, password);
            
            System.out.println("🔐 [LOGIN] Autenticación completada. Usuario encontrado: " + usuarioOpt.isPresent());

            if (usuarioOpt.isPresent()) {
                Usuario user = usuarioOpt.get();
                String[] roles = seguridadService.listarRolesPorUsuario(user);
                
                // Debug: Ver qué roles se están asignando
                System.out.println("🔐 [LOGIN] Usuario: " + user.getNombre());
                System.out.println("🔐 [LOGIN] Roles asignados: " + java.util.Arrays.toString(roles));
                System.out.println("🔐 [LOGIN] Estado usuario: " + user.getEstado());
                System.out.println("🔐 [LOGIN] Rol en entidad: " + user.getRol());
                
                // Generar access token y refresh token
                String accessToken = jwtUtil.generarToken(user, java.util.Arrays.asList(roles));
                String refreshToken = jwtUtil.generarRefreshToken(user);
                
                // Configurar cookies HttpOnly Secure
                configurarCookieToken(response, "accessToken", accessToken, (int) (jwtUtil.getAccessTokenExpiration() / 1000));
                configurarCookieToken(response, "refreshToken", refreshToken, (int) (jwtUtil.getRefreshTokenExpiration() / 1000));
                
                System.out.println("✅ [LOGIN] Cookies establecidas correctamente");
                System.out.println("✅ [LOGIN] Preparando respuesta con datos de usuario...");
                
                // Responder SOLO con información del usuario (NO incluir token en body)
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("mensaje", "Login exitoso");
                responseBody.put("usuario", Map.of(
                    "id", user.getUsuarioID(),
                    "nombre", user.getNombre(),
                    "nombres", user.getNombres(),
                    "apellidos", user.getApellidos(),
                    "email", user.getEmail(),
                    "roles", roles
                ));
                
                return ResponseEntity.ok(responseBody);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
                    
        } catch (RuntimeException e) {
            System.err.println("❌ [LOGIN] Error en autenticación: " + e.getMessage());
            e.printStackTrace();
            
            String mensaje = switch (e.getMessage()) {
                case "USUARIO_INCORRECTO" -> "Credenciales incorrectas";
                case "USUARIO_INACTIVO" -> "No se puede iniciar sesión. Contacte al administrador";
                case "USUARIO_PENDIENTE_APROBACION" -> "Cuenta pendiente de aprobación. Contacte al administrador";
                case "USUARIO_SIN_ROL" -> "No se puede iniciar sesión. Contacte al administrador";
                case "CONTRASENIA_INCORRECTA" -> "Credenciales incorrectas";
                default -> "Error de autenticación";
            };
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", mensaje));
        }
    }

    /**
     * Configura una cookie HttpOnly Secure para almacenar tokens JWT de forma segura.
     */
    private void configurarCookieToken(HttpServletResponse response, String nombre, String valor, int maxAgeSegundos) {
        // Usar ResponseCookie (Spring Framework 5+) para mejor control de SameSite
        String cookieValue = String.format(
            "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
            nombre, valor, maxAgeSegundos
        );
        
        System.out.println("🍪 [AuthController] Estableciendo cookie: " + nombre + " (maxAge: " + maxAgeSegundos + "s)");
        
        response.addHeader("Set-Cookie", cookieValue);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token", description = "Usa el refresh token para generar un nuevo access token")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        try {
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token no encontrado"));
            }

            if (!jwtUtil.esTokenValido(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token inválido o expirado"));
            }

            String tipo = jwtUtil.obtenerTipoToken(refreshToken);
            if (!"refresh".equals(tipo)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token no es de tipo refresh"));
            }

            Integer userId = jwtUtil.obtenerUserIdDelToken(refreshToken);
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario user = usuarioOpt.get();
            String[] roles = seguridadService.listarRolesPorUsuario(user);

            // Generar nuevo access token
            String nuevoAccessToken = jwtUtil.generarToken(user, java.util.Arrays.asList(roles));
            configurarCookieToken(response, "accessToken", nuevoAccessToken, (int) (jwtUtil.getAccessTokenExpiration() / 1000));

            return ResponseEntity.ok(Map.of("mensaje", "Access token renovado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Error al renovar token: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida las cookies de autenticación")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Borrar cookies estableciendo Max-Age=0
        Cookie accessCookie = new Cookie("accessToken", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(Map.of("mensaje", "Logout exitoso"));
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
    public ResponseEntity<?> actualizarPerfil(@RequestBody ActualizarPerfilRequestDTO solicitud, HttpServletResponse response) {
        try {
            UsuarioDTO perfilActualizado = usuarioService.actualizarPerfil(solicitud);
            
            // Buscar el usuario completo para regenerar el token
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(perfilActualizado.getUsuarioID());
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                String[] roles = seguridadService.listarRolesPorUsuario(usuario);
                
                // Generar nuevos tokens con la información actualizada
                String nuevoAccessToken = jwtUtil.generarToken(usuario, java.util.Arrays.asList(roles));
                String nuevoRefreshToken = jwtUtil.generarRefreshToken(usuario);
                
                // Actualizar cookies con los nuevos tokens
                configurarCookieToken(response, "accessToken", nuevoAccessToken, (int) (jwtUtil.getAccessTokenExpiration() / 1000));
                configurarCookieToken(response, "refreshToken", nuevoRefreshToken, (int) (jwtUtil.getRefreshTokenExpiration() / 1000));
                
                System.out.println("✅ [PERFIL] Tokens actualizados después de modificar perfil para usuario: " + usuario.getNombre());
            }
            
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