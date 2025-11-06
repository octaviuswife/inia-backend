package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ForgotPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.Login2FARequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ResetPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.Verify2FARequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.Setup2FAResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TrustedDeviceDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.EmailService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RecoveryCodeService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TotpService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TrustedDeviceService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.BackupCodeService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.SetupTokenService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para 2FA y Recuperación de Contraseña
 * 
 * Este controlador maneja:
 * - Configuración y gestión de Google Authenticator (2FA)
 * - Dispositivos de confianza
 * - Recuperación segura de contraseña
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Autenticación 2FA y Recuperación", description = "Endpoints para 2FA y recuperación de contraseña")
public class Auth2FAController {

    @Autowired
    private SeguridadService seguridadService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TotpService totpService;

    @Autowired
    private RecoveryCodeService recoveryCodeService;

    @Autowired
    private TrustedDeviceService trustedDeviceService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BackupCodeService backupCodeService;

    @Autowired
    private SetupTokenService setupTokenService;

    // ===== ENDPOINT DE LOGIN CON SOPORTE 2FA =====

    @PostMapping("/login-2fa")
    @Operation(summary = "Login con soporte 2FA", description = "Autentica usuario con soporte para 2FA y dispositivos de confianza")
    public ResponseEntity<?> loginWith2FA(@RequestBody Login2FARequestDTO loginData, 
                                          HttpServletRequest request, 
                                          HttpServletResponse response) {
        System.out.println("🔐 [LOGIN-2FA] Iniciando proceso de login con 2FA...");
        System.out.println("📧 [LOGIN-2FA] Usuario: " + loginData.getUsuario());
        
        try {
            // PASO 1: Autenticar credenciales básicas (usuario + contraseña)
            Optional<Usuario> usuarioOpt = seguridadService.autenticarUsuario(
                loginData.getUsuario(), 
                loginData.getPassword()
            );
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales incorrectas"));
            }
            
            Usuario user = usuarioOpt.get();
            System.out.println("✅ [LOGIN-2FA] Credenciales válidas para: " + user.getNombre());
            
            // PASO 2: Verificar si el usuario requiere cambio de credenciales (primer login del admin)
            if (user.getRequiereCambioCredenciales() != null && user.getRequiereCambioCredenciales()) {
                System.out.println("⚠️ [LOGIN-2FA] Usuario requiere cambio de credenciales (primer login)");
                System.out.println("🔑 [LOGIN-2FA] TOTP Secret del usuario en BD: " + user.getTotpSecret());
                
                // Generar QR code
                String qrCodeDataUrl = totpService.generateQrCodeDataUrl(user.getTotpSecret(), "admin@temporal.local");
                
                System.out.println("🎫 [LOGIN-2FA] Creando token con secret: " + user.getTotpSecret());
                
                // Crear token temporal seguro (expira en 5 minutos, un solo uso)
                String setupToken = setupTokenService.createSetupToken(
                    user.getUsuarioID(),
                    user.getNombre(),
                    qrCodeDataUrl,
                    user.getTotpSecret()
                );
                
                System.out.println("✅ [LOGIN-2FA] Token creado, secret enviado en JWT");
                
                // Devolver SOLO el token (no los datos sensibles)
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                            "requiresCredentialChange", true,
                            "mensaje", "Debes configurar tus credenciales y 2FA en el primer acceso",
                            "setupToken", setupToken  // Solo el token, no los datos
                        ));
            }
            
            // PASO 3: Verificar si el usuario tiene 2FA habilitado
            boolean has2FAEnabled = user.getTotpEnabled() != null && user.getTotpEnabled();
            
            if (!has2FAEnabled) {
                // 2FA OBLIGATORIO - Usuario DEBE activar 2FA antes de poder usar el sistema
                System.out.println("⚠️ [LOGIN-2FA] Usuario sin 2FA - Debe activar 2FA (OBLIGATORIO)");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                            "requires2FASetup", true,
                            "mensaje", "Debes activar la autenticación de dos factores para usar el sistema",
                            "userId", user.getUsuarioID(),
                            "email", user.getEmail(),
                            "nombre", user.getNombreCompleto()
                        ));
            }
            
            // PASO 3: Usuario TIENE 2FA habilitado - Verificar dispositivo de confianza
            String deviceFingerprint = loginData.getDeviceFingerprint();
            boolean isTrustedDevice = false;
            
            if (deviceFingerprint != null && !deviceFingerprint.isEmpty()) {
                isTrustedDevice = trustedDeviceService.isTrustedDevice(user.getUsuarioID(), deviceFingerprint);
                System.out.println("📱 [LOGIN-2FA] Dispositivo de confianza: " + isTrustedDevice);
            }
            
            if (isTrustedDevice) {
                // Dispositivo de confianza - NO requiere código 2FA
                System.out.println("✅ [LOGIN-2FA] Dispositivo de confianza validado, login directo");
                return completarLoginExitoso(user, response);
            }
            
            // PASO 4: Dispositivo NO es de confianza - Requiere código 2FA
            String totpCode = loginData.getTotpCode();
            
            if (totpCode == null || totpCode.isEmpty()) {
                // No proporcionó código 2FA - Solicitar código
                System.out.println("🔐 [LOGIN-2FA] Código 2FA requerido");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                            "requires2FA", true,
                            "mensaje", "Se requiere código de autenticación de dos factores",
                            "userId", user.getUsuarioID()
                        ));
            }
            
            // PASO 5: Verificar código 2FA (TOTP o código de respaldo)
            boolean isCodeValid = false;
            boolean usedBackupCode = false;
            
            // Primero intentar verificar como código TOTP
            if (totpCode.length() == 6 && totpCode.matches("\\d+")) {
                isCodeValid = totpService.verifyCode(user.getTotpSecret(), totpCode);
                System.out.println("🔑 [LOGIN-2FA] Verificación TOTP: " + isCodeValid);
            }
            
            // Si el TOTP falló, intentar con código de respaldo
            if (!isCodeValid && totpCode.length() >= 12) {
                isCodeValid = backupCodeService.verifyAndUseBackupCode(user.getUsuarioID(), totpCode);
                if (isCodeValid) {
                    usedBackupCode = true;
                    System.out.println("🎫 [LOGIN-2FA] Código de respaldo usado exitosamente");
                    
                    // Verificar códigos restantes y alertar si quedan pocos
                    long remaining = backupCodeService.getAvailableCodesCount(user.getUsuarioID());
                    if (remaining <= 2) {
                        System.out.println("⚠️ [LOGIN-2FA] ALERTA: Solo quedan " + remaining + " códigos de respaldo");
                    }
                }
            }
            
            if (!isCodeValid) {
                System.err.println("❌ [LOGIN-2FA] Código inválido (ni TOTP ni código de respaldo)");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código de autenticación inválido"));
            }
            
            System.out.println("✅ [LOGIN-2FA] Código válido" + (usedBackupCode ? " (código de respaldo)" : " (TOTP)"));
            
            // PASO 6: Si el usuario quiere confiar en este dispositivo, registrarlo
            if (loginData.getTrustDevice() != null && loginData.getTrustDevice() && deviceFingerprint != null) {
                try {
                    trustedDeviceService.trustDevice(user.getUsuarioID(), deviceFingerprint, request);
                    System.out.println("📱 [LOGIN-2FA] Dispositivo registrado como de confianza");
                    
                    // Notificar por email sobre nuevo dispositivo
                    String deviceName = extractDeviceName(request);
                    String ipAddress = extractIpAddress(request);
                    emailService.enviarNuevoDispositivo(
                        user.getEmail(),
                        user.getNombreCompleto(),
                        deviceName,
                        ipAddress
                    );
                } catch (Exception e) {
                    // No fallar el login si falla el registro del dispositivo
                    System.err.println("⚠️ [LOGIN-2FA] Error registrando dispositivo de confianza: " + e.getMessage());
                }
            }
            
            // PASO 7: Completar login exitoso
            return completarLoginExitoso(user, response);
            
        } catch (RuntimeException e) {
            System.err.println("❌ [LOGIN-2FA] Error: " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("❌ [LOGIN-2FA] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Completa el login exitoso generando tokens JWT y configurando cookies
     */
    private ResponseEntity<?> completarLoginExitoso(Usuario user, HttpServletResponse response) {
        String[] roles = seguridadService.listarRolesPorUsuario(user);
        
        // Generar access token y refresh token
        String accessToken = jwtUtil.generarToken(user, java.util.Arrays.asList(roles));
        String refreshToken = jwtUtil.generarRefreshToken(user);
        
        // Configurar cookies HttpOnly Secure
        configurarCookieToken(response, "accessToken", accessToken, (int) (jwtUtil.getAccessTokenExpiration() / 1000));
        configurarCookieToken(response, "refreshToken", refreshToken, (int) (jwtUtil.getRefreshTokenExpiration() / 1000));
        
        System.out.println("✅ [LOGIN-2FA] Login exitoso para: " + user.getNombre());
        
        // Respuesta
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("mensaje", "Login exitoso");
        responseBody.put("usuario", Map.of(
            "id", user.getUsuarioID(),
            "nombre", user.getNombre(),
            "nombres", user.getNombres(),
            "apellidos", user.getApellidos(),
            "email", user.getEmail(),
            "roles", roles,
            "has2FA", user.getTotpEnabled() != null && user.getTotpEnabled()
        ));
        
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Configura una cookie HttpOnly Secure
     */
    private void configurarCookieToken(HttpServletResponse response, String nombre, String valor, int maxAgeSegundos) {
        String cookieValue = String.format(
            "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Lax",
            nombre, valor, maxAgeSegundos
        );
        response.addHeader("Set-Cookie", cookieValue);
    }

    /**
     * Extrae nombre del dispositivo del User-Agent
     */
    private String extractDeviceName(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) return "Dispositivo Desconocido";
        
        String browser = "Navegador";
        String os = "SO";
        
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) browser = "Chrome";
        else if (userAgent.contains("Firefox")) browser = "Firefox";
        else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) browser = "Safari";
        else if (userAgent.contains("Edg")) browser = "Edge";
        
        if (userAgent.contains("Windows")) os = "Windows";
        else if (userAgent.contains("Mac")) os = "macOS";
        else if (userAgent.contains("Linux")) os = "Linux";
        else if (userAgent.contains("Android")) os = "Android";
        else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";
        
        return browser + " en " + os;
    }

    /**
     * Extrae IP considerando proxies
     */
    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    // ===== ENDPOINTS DE AUTENTICACIÓN DE DOS FACTORES (2FA) =====

    @PostMapping("/2fa/setup-initial")
    @Operation(summary = "Setup inicial de 2FA (sin autenticación)", 
               description = "Genera QR code para usuarios que DEBEN activar 2FA por primera vez")
    public ResponseEntity<?> setupInitial2FA(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            if (email == null || password == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email y contraseña requeridos"));
            }
            
            // Autenticar al usuario
            Optional<Usuario> userOpt = seguridadService.autenticarUsuario(email, password);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales incorrectas"));
            }
            
            Usuario user = userOpt.get();
            System.out.println("🔐 [SETUP-INITIAL] Iniciando setup 2FA para: " + user.getNombre());
            
            // Si ya tiene 2FA habilitado, redirigir a login normal
            if (user.getTotpEnabled() != null && user.getTotpEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Ya tienes 2FA habilitado. Usa el login normal."));
            }
            
            // Generar nuevo secret
            String secret = totpService.generateSecret();
            
            // Generar QR code
            String accountName = user.getEmail();
            String qrCodeDataUrl = totpService.generateQrCodeDataUrl(secret, accountName);
            
            // Guardar el secret (pero NO habilitar 2FA hasta que se verifique)
            user.setTotpSecret(secret);
            user.setTotpEnabled(false);
            usuarioService.guardar(user);
            
            Setup2FAResponseDTO response = new Setup2FAResponseDTO(
                secret,
                qrCodeDataUrl,
                "INIA",
                accountName
            );
            
            System.out.println("✅ [SETUP-INITIAL] QR generado para: " + user.getNombre());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Escanea el QR code con Google Authenticator",
                "data", response,
                "userId", user.getUsuarioID(),
                "email", user.getEmail()
            ));
            
        } catch (RuntimeException e) {
            System.err.println("❌ [SETUP-INITIAL] Error: " + e.getMessage());
            
            String mensaje = switch (e.getMessage()) {
                case "USUARIO_INCORRECTO" -> "Credenciales incorrectas";
                case "USUARIO_INACTIVO" -> "Usuario inactivo";
                case "USUARIO_PENDIENTE_APROBACION" -> "Cuenta pendiente de aprobación";
                case "CONTRASENIA_INCORRECTA" -> "Credenciales incorrectas";
                default -> "Error de autenticación";
            };
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", mensaje));
        } catch (Exception e) {
            System.err.println("❌ [SETUP-INITIAL] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @PostMapping("/2fa/verify-initial")
    @Operation(summary = "Verificar código 2FA inicial (sin autenticación)", 
               description = "Verifica el código TOTP y activa 2FA por primera vez, luego hace login")
    public ResponseEntity<?> verifyInitial2FA(
            @RequestBody Map<String, String> request,
            HttpServletResponse response) {
        try {
            String email = request.get("email");
            String totpCode = request.get("totpCode");
            
            if (email == null || totpCode == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email y código TOTP requeridos"));
            }
            
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            System.out.println("🔐 [VERIFY-INITIAL] Verificando código para: " + usuario.getNombre());
            
            // Verificar que tenga un secret configurado
            if (usuario.getTotpSecret() == null || usuario.getTotpSecret().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No hay configuración 2FA pendiente. Inicia el setup primero."));
            }
            
            // Verificar el código TOTP
            boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), totpCode);
            
            if (!isValid) {
                System.err.println("❌ [VERIFY-INITIAL] Código inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código 2FA inválido o expirado"));
            }
            
            // ✅ Código correcto - Activar 2FA
            usuario.setTotpEnabled(true);
            usuarioService.guardar(usuario);
            
            // Generar códigos de respaldo
            List<String> backupCodes = backupCodeService.generateBackupCodes(usuario.getUsuarioID());
            System.out.println("🔐 [VERIFY-INITIAL] Generados " + backupCodes.size() + " códigos de respaldo");
            
            // Enviar email de notificación
            emailService.enviar2FAActivado(usuario.getEmail(), usuario.getNombreCompleto());
            
            System.out.println("✅ [VERIFY-INITIAL] 2FA activado para: " + usuario.getNombre());
            
            // Ahora hacer login automático
            String[] roles = seguridadService.listarRolesPorUsuario(usuario);
            String accessToken = jwtUtil.generarToken(usuario, java.util.Arrays.asList(roles));
            String refreshToken = jwtUtil.generarRefreshToken(usuario);
            
            // Configurar cookies
            configurarCookieToken(response, "accessToken", accessToken, 24 * 60 * 60);
            configurarCookieToken(response, "refreshToken", refreshToken, 7 * 24 * 60 * 60);
            
            // Respuesta con códigos de respaldo y datos de usuario
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("mensaje", "2FA activado exitosamente. GUARDA estos códigos de respaldo.");
            responseData.put("totpEnabled", true);
            responseData.put("backupCodes", backupCodes);
            responseData.put("totalCodes", backupCodes.size());
            responseData.put("usuario", Map.of(
                "id", usuario.getUsuarioID(),
                "nombre", usuario.getNombre(),
                "nombres", usuario.getNombres(),
                "apellidos", usuario.getApellidos(),
                "email", usuario.getEmail(),
                "roles", roles,
                "has2FA", true
            ));
            
            System.out.println("✅ [VERIFY-INITIAL] Login completado para: " + usuario.getNombre());
            
            return ResponseEntity.ok(responseData);
            
        } catch (Exception e) {
            System.err.println("❌ [VERIFY-INITIAL] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al verificar 2FA"));
        }
    }

    @GetMapping("/admin/setup-data/{token}")
    @Operation(summary = "Obtener datos de configuración con token temporal", 
               description = "Recupera datos de configuración usando un token de un solo uso")
    public ResponseEntity<?> getSetupData(@PathVariable String token) {
        try {
            System.out.println("🎫 [SETUP-DATA] Solicitando datos para token");
            
            // Consumir token (un solo uso)
            Map<String, Object> setupData = setupTokenService.consumeSetupToken(token);
            
            if (setupData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Token inválido o expirado"));
            }
            
            System.out.println("✅ [SETUP-DATA] Datos enviados, token consumido");
            return ResponseEntity.ok(setupData);
            
        } catch (Exception e) {
            System.err.println("❌ [SETUP-DATA] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener datos de configuración"));
        }
    }

    @PostMapping("/admin/complete-setup")
    @Operation(summary = "Completar configuración inicial del admin", 
               description = "Permite al admin configurar email, contraseña y 2FA en el primer acceso")
    public ResponseEntity<?> completeAdminSetup(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        try {
            String currentPassword = request.get("currentPassword");
            String newEmail = request.get("newEmail");
            String newPassword = request.get("newPassword");
            String totpCode = request.get("totpCode");
            
            if (currentPassword == null || newEmail == null || newPassword == null || totpCode == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Todos los campos son requeridos"));
            }
            
            System.out.println("🔐 [ADMIN-SETUP] Iniciando configuración del admin...");
            
            // Autenticar con contraseña actual (admin123)
            Optional<Usuario> userOpt = seguridadService.autenticarUsuario("admin", currentPassword);
            
            if (userOpt.isEmpty()) {
                System.err.println("❌ [ADMIN-SETUP] Contraseña actual incorrecta");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Contraseña actual incorrecta"));
            }
            
            Usuario admin = userOpt.get();
            
            // Verificar que sea admin y requiera cambio de credenciales
            if (!admin.esAdmin() || !admin.getRequiereCambioCredenciales()) {
                System.err.println("❌ [ADMIN-SETUP] Usuario no es admin o no requiere cambio");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado"));
            }
            
            System.out.println("🔐 [ADMIN-SETUP] Validando código TOTP...");
            System.out.println("🔑 [ADMIN-SETUP] TOTP Secret: " + admin.getTotpSecret());
            System.out.println("🔢 [ADMIN-SETUP] Código recibido: " + totpCode);
            
            // Verificar código TOTP (el usuario ya escaneó el QR)
            boolean isValid = totpService.verifyCode(admin.getTotpSecret(), totpCode);
            
            System.out.println("🔍 [ADMIN-SETUP] Código válido: " + isValid);
            
            if (!isValid) {
                System.err.println("❌ [ADMIN-SETUP] Código 2FA inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código 2FA inválido. Verifica que hayas escaneado correctamente el código QR."));
            }
            
            System.out.println("✅ [ADMIN-SETUP] Código 2FA válido, actualizando credenciales...");
            
            // Actualizar email primero
            admin.setEmail(newEmail);
            admin.setTotpEnabled(true);  // AHORA SÍ activamos el 2FA
            admin.setRequiereCambioCredenciales(false);  // Ya no requiere cambio
            usuarioService.guardar(admin);
            
            // Cambiar contraseña (el servicio la encripta automáticamente)
            usuarioService.cambiarContrasenia(admin.getUsuarioID(), newPassword);
            
            System.out.println("✅ [ADMIN-SETUP] Credenciales actualizadas");
            
            // Generar códigos de respaldo
            List<String> backupCodes = backupCodeService.generateBackupCodes(admin.getUsuarioID());
            
            System.out.println("✅ [ADMIN-SETUP] Códigos de respaldo generados: " + backupCodes.size());
            
            // Enviar email de notificación
            try {
                emailService.enviar2FAActivado(newEmail, admin.getNombreCompleto());
                System.out.println("📧 [ADMIN-SETUP] Email de notificación enviado");
            } catch (Exception e) {
                System.err.println("⚠️ [ADMIN-SETUP] No se pudo enviar email: " + e.getMessage());
            }
            
            System.out.println("✅ [ADMIN-SETUP] Configuración completada, creando sesión...");
            
            // Login automático: Obtener roles y crear authorities
            String[] roles = seguridadService.listarRolesPorUsuario(admin);
            
            Collection<org.springframework.security.core.GrantedAuthority> authorities = 
                java.util.Arrays.stream(roles)
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        role.startsWith("ROLE_") ? role : "ROLE_" + role))
                    .collect(java.util.stream.Collectors.toList());
            
            // Crear authentication token
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(admin.getNombre(), null, authorities);
            
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authToken);
            SecurityContextHolder.setContext(securityContext);
            
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            
            System.out.println("✅ [ADMIN-SETUP] Sesión HTTP creada");
            
            // También generar tokens JWT para compatibilidad
            String accessToken = jwtUtil.generarToken(admin, java.util.Arrays.asList(roles));
            String refreshToken = jwtUtil.generarRefreshToken(admin);
            
            configurarCookieToken(response, "accessToken", accessToken, 24 * 60 * 60);
            configurarCookieToken(response, "refreshToken", refreshToken, 7 * 24 * 60 * 60);
            
            System.out.println("✅ [ADMIN-SETUP] Tokens JWT configurados");
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("mensaje", "Configuración completada exitosamente");
            responseData.put("backupCodes", backupCodes);
            responseData.put("totalCodes", backupCodes.size());
            responseData.put("usuario", Map.of(
                "id", admin.getUsuarioID(),
                "nombre", admin.getNombre(),
                "nombres", admin.getNombres(),
                "apellidos", admin.getApellidos(),
                "email", admin.getEmail(),
                "roles", roles,
                "has2FA", true
            ));
            
            System.out.println("🎉 [ADMIN-SETUP] Setup completado exitosamente");
            
            return ResponseEntity.ok(responseData);
            
        } catch (Exception e) {
            System.err.println("❌ [ADMIN-SETUP] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al completar configuración: " + e.getMessage()));
        }
    }

    @PostMapping("/2fa/setup")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Configurar 2FA", description = "Genera QR code para configurar Google Authenticator")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> setup2FA() {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Si ya tiene 2FA habilitado, no permitir reconfigurar sin deshabilitarlo primero
            if (usuario.getTotpEnabled() != null && usuario.getTotpEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "2FA ya está habilitado. Deshabilítalo primero si deseas reconfigurarlo."));
            }
            
            // Generar nuevo secret
            String secret = totpService.generateSecret();
            
            // Generar QR code
            String accountName = usuario.getEmail();
            String qrCodeDataUrl = totpService.generateQrCodeDataUrl(secret, accountName);
            
            // Guardar el secret (pero NO habilitar 2FA hasta que se verifique)
            usuario.setTotpSecret(secret);
            usuario.setTotpEnabled(false); // Todavía no está habilitado
            usuarioService.guardar(usuario);
            
            Setup2FAResponseDTO response = new Setup2FAResponseDTO(
                secret,
                qrCodeDataUrl,
                "INIA",
                accountName
            );
            
            System.out.println("🔐 2FA setup iniciado para usuario: " + usuario.getNombre());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Escanea el QR code con Google Authenticator y verifica el código",
                "data", response
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error configurando 2FA: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al configurar 2FA"));
        }
    }

    @PostMapping("/2fa/verify")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Verificar y activar 2FA", description = "Verifica el código de Google Authenticator y activa 2FA")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> verify2FA(@RequestBody Verify2FARequestDTO request) {
        try {
            System.out.println("🔐 [VERIFY-2FA] Verificando código TOTP...");
            System.out.println("📧 [VERIFY-2FA] Código recibido: " + request.getTotpCode());
            
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            
            if (usuarioOpt.isEmpty()) {
                System.err.println("❌ [VERIFY-2FA] Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            System.out.println("👤 [VERIFY-2FA] Usuario: " + usuario.getNombre());
            
            // Verificar que tenga un secret configurado
            if (usuario.getTotpSecret() == null || usuario.getTotpSecret().isEmpty()) {
                System.err.println("❌ [VERIFY-2FA] Usuario sin secret configurado");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Debes configurar 2FA primero (llama a /2fa/setup)"));
            }
            
            System.out.println("🔑 [VERIFY-2FA] Secret del usuario: " + usuario.getTotpSecret().substring(0, 8) + "...");
            
            // DEBUG: Generar código actual para comparar
            String currentCode = totpService.getCurrentCode(usuario.getTotpSecret());
            System.out.println("🎯 [VERIFY-2FA] Código actual esperado: " + currentCode);
            System.out.println("⏰ [VERIFY-2FA] Tiempo restante del código: " + totpService.getRemainingSeconds() + "s");
            
            // Verificar el código TOTP
            boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());
            
            System.out.println("✅ [VERIFY-2FA] Código válido: " + isValid);
            
            if (!isValid) {
                System.err.println("❌ [VERIFY-2FA] Código inválido o expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código 2FA inválido o expirado"));
            }
            
            // ¡Código correcto! Activar 2FA
            usuario.setTotpEnabled(true);
            usuarioService.guardar(usuario);
            
            // Generar códigos de respaldo (solo se muestran una vez)
            List<String> backupCodes = backupCodeService.generateBackupCodes(userId);
            System.out.println("🔐 [VERIFY-2FA] Generados " + backupCodes.size() + " códigos de respaldo");
            
            // Enviar email de notificación
            emailService.enviar2FAActivado(usuario.getEmail(), usuario.getNombreCompleto());
            
            System.out.println("✅ [VERIFY-2FA] 2FA activado exitosamente para usuario: " + usuario.getNombre());
            
            // Respuesta incluye códigos de respaldo (SOLO SE MUESTRAN UNA VEZ)
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "2FA activado exitosamente. GUARDA estos códigos de respaldo en un lugar seguro.");
            response.put("totpEnabled", true);
            response.put("backupCodes", backupCodes);
            response.put("totalCodes", backupCodes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ [VERIFY-2FA] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al verificar 2FA"));
        }
    }

    @DeleteMapping("/2fa/disable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Deshabilitar 2FA", description = "Deshabilita 2FA después de verificar el código actual")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> disable2FA(@RequestBody Verify2FARequestDTO request) {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar que tenga 2FA habilitado
            if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "2FA no está habilitado"));
            }
            
            // Verificar el código TOTP antes de deshabilitar (seguridad)
            boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());
            
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código 2FA inválido. Necesitas el código correcto para deshabilitar 2FA"));
            }
            
            // Deshabilitar 2FA
            usuario.setTotpEnabled(false);
            usuario.setTotpSecret(null);
            usuarioService.guardar(usuario);
            
            // Eliminar todos los códigos de respaldo
            backupCodeService.deleteAllUserCodes(userId);
            System.out.println("🗑️ Códigos de respaldo eliminados para usuario: " + usuario.getNombre());
            
            // Revocar todos los dispositivos de confianza
            trustedDeviceService.revokeAllUserDevices(userId);
            
            System.out.println("🔓 2FA deshabilitado para usuario: " + usuario.getNombre());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "2FA deshabilitado exitosamente",
                "totpEnabled", false
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error deshabilitando 2FA: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al deshabilitar 2FA"));
        }
    }

    @GetMapping("/2fa/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Estado de 2FA", description = "Verifica si el usuario tiene 2FA habilitado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> get2FAStatus() {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            return ResponseEntity.ok(Map.of(
                "totpEnabled", usuario.getTotpEnabled() != null && usuario.getTotpEnabled(),
                "hasSecret", usuario.getTotpSecret() != null && !usuario.getTotpSecret().isEmpty()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener estado de 2FA"));
        }
    }

    // ===== ENDPOINTS DE DISPOSITIVOS DE CONFIANZA =====

    @GetMapping("/trusted-devices")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar dispositivos de confianza", description = "Lista todos los dispositivos de confianza del usuario")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> listTrustedDevices() {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            System.out.println("📱 [TRUSTED-DEVICES] Listando dispositivos para usuario ID: " + userId);
            
            List<TrustedDeviceDTO> devices = trustedDeviceService.listUserDevices(userId);
            
            System.out.println("📱 [TRUSTED-DEVICES] Dispositivos encontrados: " + devices.size());
            devices.forEach(d -> System.out.println("  - " + d.getDeviceName() + " (ID: " + d.getId() + ")"));
            
            return ResponseEntity.ok(Map.of(
                "devices", devices,
                "count", devices.size()
            ));
            
        } catch (Exception e) {
            System.err.println("❌ [TRUSTED-DEVICES] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar dispositivos"));
        }
    }

    @DeleteMapping("/trusted-devices/{deviceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Revocar dispositivo de confianza", description = "Revoca un dispositivo de confianza específico")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> revokeTrustedDevice(@PathVariable Long deviceId) {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            trustedDeviceService.revokeDevice(deviceId, userId);
            
            return ResponseEntity.ok(Map.of("mensaje", "Dispositivo revocado exitosamente"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al revocar dispositivo"));
        }
    }

    @DeleteMapping("/trusted-devices")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Revocar todos los dispositivos", description = "Revoca todos los dispositivos de confianza del usuario")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> revokeAllTrustedDevices() {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            trustedDeviceService.revokeAllUserDevices(userId);
            
            return ResponseEntity.ok(Map.of("mensaje", "Todos los dispositivos revocados exitosamente"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al revocar dispositivos"));
        }
    }

    // ===== ENDPOINTS DE RECUPERACIÓN DE CONTRASEÑA =====

    @PostMapping("/forgot-password")
    @Operation(summary = "Olvidé mi contraseña", description = "Envía un código de recuperación al email del usuario")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        try {
            // Buscar usuario por email
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());
            
            if (usuarioOpt.isEmpty()) {
                // Por seguridad, no revelar si el email existe o no
                return ResponseEntity.ok(Map.of(
                    "mensaje", "Si el email existe, se enviará un código de recuperación"
                ));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar que tenga 2FA habilitado
            if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Este usuario no tiene 2FA habilitado. Contacte al administrador."));
            }
            
            // Generar código de recuperación
            String recoveryCode = recoveryCodeService.generateRecoveryCode();
            String hashedCode = recoveryCodeService.hashCode(recoveryCode);
            
            // Guardar el código hasheado y su expiración
            usuario.setRecoveryCodeHash(hashedCode);
            usuario.setRecoveryCodeExpiry(recoveryCodeService.getExpiryTime());
            usuarioService.guardar(usuario);
            
            // Enviar email con el código
            emailService.enviarCodigoRecuperacion(
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                recoveryCode
            );
            
            System.out.println("📧 Código de recuperación enviado a: " + usuario.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Código de recuperación enviado a tu email. Válido por 10 minutos.",
                "expiresIn", recoveryCodeService.getExpiryMinutes() + " minutos"
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error en forgot-password: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar solicitud"));
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Resetear contraseña", description = "Resetea la contraseña usando código de recuperación + 2FA")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        try {
            // Buscar usuario por email
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(request.getEmail());
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar que tenga un código de recuperación activo
            if (usuario.getRecoveryCodeHash() == null || usuario.getRecoveryCodeExpiry() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No hay código de recuperación activo. Solicita uno nuevo."));
            }
            
            // Verificar que el código no haya expirado
            if (recoveryCodeService.isExpired(usuario.getRecoveryCodeExpiry())) {
                // Limpiar código expirado
                usuario.setRecoveryCodeHash(null);
                usuario.setRecoveryCodeExpiry(null);
                usuarioService.guardar(usuario);
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código de recuperación expirado. Solicita uno nuevo."));
            }
            
            // Verificar el código de recuperación
            boolean isRecoveryCodeValid = recoveryCodeService.verifyCode(
                request.getRecoveryCode(),
                usuario.getRecoveryCodeHash()
            );
            
            if (!isRecoveryCodeValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código de recuperación inválido"));
            }
            
            // Verificar el código 2FA (DOBLE SEGURIDAD) - acepta TOTP o código de respaldo
            boolean is2FAValid = false;
            boolean usedBackupCode = false;
            
            // Primero intentar verificar como código TOTP
            if (request.getTotpCode().length() == 6 && request.getTotpCode().matches("\\d+")) {
                is2FAValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());
                System.out.println("🔑 [RESET-PASSWORD] Verificación TOTP: " + is2FAValid);
            }
            
            // Si el TOTP falló, intentar con código de respaldo
            if (!is2FAValid && request.getTotpCode().length() >= 12) {
                is2FAValid = backupCodeService.verifyAndUseBackupCode(usuario.getUsuarioID(), request.getTotpCode());
                if (is2FAValid) {
                    usedBackupCode = true;
                    System.out.println("🎫 [RESET-PASSWORD] Código de respaldo usado exitosamente");
                }
            }
            
            if (!is2FAValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código de autenticación inválido"));
            }
            
            System.out.println("✅ [RESET-PASSWORD] Código 2FA válido" + (usedBackupCode ? " (código de respaldo)" : " (TOTP)"));
            
            // ✅ TODO VÁLIDO - Cambiar contraseña
            usuarioService.cambiarContrasenia(usuario.getUsuarioID(), request.getNewPassword());
            
            // Limpiar código de recuperación (solo se usa una vez)
            usuario.setRecoveryCodeHash(null);
            usuario.setRecoveryCodeExpiry(null);
            usuarioService.guardar(usuario);
            
            // Revocar todos los dispositivos de confianza por seguridad
            trustedDeviceService.revokeAllUserDevices(usuario.getUsuarioID());
            
            System.out.println("🔑 Contraseña reseteada exitosamente para: " + usuario.getEmail());
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Contraseña cambiada exitosamente. Por seguridad, todos tus dispositivos de confianza fueron revocados."
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error en reset-password: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al resetear contraseña"));
        }
    }

    // ============================================
    // ENDPOINTS DE CÓDIGOS DE RESPALDO
    // ============================================

    @PostMapping("/2fa/backup-codes/regenerate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Regenerar códigos de respaldo", 
               description = "Genera un nuevo conjunto de códigos de respaldo, invalidando los anteriores. Requiere código TOTP para seguridad.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> regenerateBackupCodes(@RequestBody Verify2FARequestDTO request) {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar que tenga 2FA habilitado
            if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "2FA no está habilitado"));
            }
            
            // Verificar código TOTP antes de regenerar (seguridad)
            boolean isValid = totpService.verifyCode(usuario.getTotpSecret(), request.getTotpCode());
            
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Código 2FA inválido"));
            }
            
            // Regenerar códigos de respaldo
            List<String> backupCodes = backupCodeService.regenerateBackupCodes(userId);
            
            System.out.println("🔄 Códigos de respaldo regenerados para usuario: " + usuario.getNombre() + " (Total: " + backupCodes.size() + ")");
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Códigos de respaldo regenerados exitosamente. GUARDA estos nuevos códigos, los anteriores fueron invalidados.");
            response.put("backupCodes", backupCodes);
            response.put("totalCodes", backupCodes.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error regenerando códigos de respaldo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al regenerar códigos de respaldo"));
        }
    }

    @GetMapping("/2fa/backup-codes/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Contar códigos de respaldo disponibles", 
               description = "Obtiene la cantidad de códigos de respaldo disponibles (no usados)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getBackupCodesCount() {
        try {
            Integer userId = seguridadService.obtenerUsuarioAutenticado();
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(userId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado"));
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar que tenga 2FA habilitado
            if (usuario.getTotpEnabled() == null || !usuario.getTotpEnabled()) {
                return ResponseEntity.ok(Map.of(
                    "availableCodes", 0,
                    "mensaje", "2FA no está habilitado"
                ));
            }
            
            long availableCodes = backupCodeService.getAvailableCodesCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("availableCodes", availableCodes);
            
            if (availableCodes <= 2) {
                response.put("warning", "Quedan pocos códigos de respaldo. Considera regenerarlos.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo conteo de códigos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener códigos de respaldo"));
        }
    }
}
