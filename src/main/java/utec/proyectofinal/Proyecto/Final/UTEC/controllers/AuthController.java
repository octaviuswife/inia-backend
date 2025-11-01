package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ActualizarPerfilRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.AprobarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GestionarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoginRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación y Gestión de Usuarios", description = "Endpoints para autenticación, registro y gestión de usuarios")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y crea una sesión HTTP")
    public ResponseEntity<?> login(
            @RequestBody LoginRequestDTO loginData,
            HttpServletRequest httpRequest) {

        System.out.println("🔐 [LOGIN] Iniciando proceso de login...");
        System.out.println("🔐 [LOGIN] Usuario recibido: " + loginData.getUsuario());

        try {
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginData.getUsuario(),
                            loginData.getPassword()
                    )
            );

            System.out.println("✅ [LOGIN] Autenticación exitosa");
            System.out.println("👤 [LOGIN] Usuario autenticado: " + authentication.getName());
            System.out.println("🎫 [LOGIN] Authorities: " + authentication.getAuthorities());

            // Crear SecurityContext y establecerlo
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            // CRÍTICO: Crear la sesión SOLO en el login con getSession(true)
            // Con SessionCreationPolicy.NEVER, esta es la ÚNICA forma de crear una sesión
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

            System.out.println("🔑 [LOGIN] Sesión creada explícitamente: " + session.getId());

            // Obtener roles del usuario autenticado
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .collect(Collectors.toList());

            System.out.println("✅ [LOGIN] Roles finales: " + roles);

            // Preparar respuesta
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("mensaje", "Login exitoso");
            responseBody.put("usuario", Map.of(
                    "nombre", authentication.getName(),
                    "roles", roles
            ));

            return ResponseEntity.ok(responseBody);

        } catch (BadCredentialsException e) {
            System.out.println("❌ [LOGIN] Credenciales incorrectas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
        } catch (Exception e) {
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

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida la sesión del usuario")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Obtener la sesión sin crearla (false)
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("🚪 [LOGOUT] Invalidando sesión: " + session.getId());
            session.invalidate();
            System.out.println("✅ [LOGOUT] Sesión invalidada correctamente");
        } else {
            System.out.println("⚠️ [LOGOUT] No había sesión activa para invalidar");
        }
        
        // Limpiar el contexto de seguridad
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("mensaje", "Logout exitoso"));
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