package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ForgotPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.Login2FARequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ResetPasswordRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.Verify2FARequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.Setup2FAResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TrustedDeviceDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de Integración - Auth2FAController")
class Auth2FAControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeguridadService seguridadService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private TotpService totpService;

    @MockBean
    private RecoveryCodeService recoveryCodeService;

    @MockBean
    private TrustedDeviceService trustedDeviceService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private BackupCodeService backupCodeService;

    @MockBean
    private SetupTokenService setupTokenService;

    private Usuario usuarioTest;
    private Setup2FAResponseDTO setup2FAResponse;

    @BeforeEach
    void setUp() {
        usuarioTest = new Usuario();
        usuarioTest.setUsuarioID(1);
        usuarioTest.setNombre("testuser");
        usuarioTest.setNombres("Test");
        usuarioTest.setApellidos("User");
        usuarioTest.setEmail("test@example.com");
        usuarioTest.setTotpSecret("TESTSECRET123456");
        usuarioTest.setTotpEnabled(true);

        setup2FAResponse = new Setup2FAResponseDTO(
            "TESTSECRET123456",
            "data:image/png;base64,TEST",
            "INIA",
            "test@example.com"
        );
    }

    // ===== TESTS DE LOGIN CON 2FA =====

    @Test
    @DisplayName("POST /api/v1/auth/login-2fa - Login exitoso con 2FA y dispositivo de confianza")
    void loginWith2FA_dispositivoConfianza_debeRetornarToken() throws Exception {
        Login2FARequestDTO loginRequest = new Login2FARequestDTO();
        loginRequest.setUsuario("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setDeviceFingerprint("device123");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(usuarioTest));
        when(trustedDeviceService.isTrustedDevice(any(), any())).thenReturn(true);
        when(seguridadService.listarRolesPorUsuario(any())).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("access-token");
        when(jwtUtil.generarRefreshToken(any())).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(604800000L);

        mockMvc.perform(post("/api/v1/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                .andExpect(jsonPath("$.usuario.email").value("test@example.com"))
                .andExpect(jsonPath("$.usuario.has2FA").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login-2fa - Login requiere código 2FA")
    void loginWith2FA_sinDispositivo_debeRequerir2FA() throws Exception {
        Login2FARequestDTO loginRequest = new Login2FARequestDTO();
        loginRequest.setUsuario("test@example.com");
        loginRequest.setPassword("password123");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(usuarioTest));
        when(trustedDeviceService.isTrustedDevice(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.requires2FA").value(true))
                .andExpect(jsonPath("$.mensaje").value("Se requiere código de autenticación de dos factores"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login-2fa - Login con código TOTP válido")
    void loginWith2FA_codigoTOTPValido_debeRetornarToken() throws Exception {
        Login2FARequestDTO loginRequest = new Login2FARequestDTO();
        loginRequest.setUsuario("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setTotpCode("123456");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(usuarioTest));
        when(trustedDeviceService.isTrustedDevice(any(), any())).thenReturn(false);
        when(totpService.verifyCode(any(), any())).thenReturn(true);
        when(seguridadService.listarRolesPorUsuario(any())).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("access-token");
        when(jwtUtil.generarRefreshToken(any())).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(604800000L);

        mockMvc.perform(post("/api/v1/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login-2fa - Login con código de respaldo válido")
    void loginWith2FA_codigoRespaldoValido_debeRetornarToken() throws Exception {
        Login2FARequestDTO loginRequest = new Login2FARequestDTO();
        loginRequest.setUsuario("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setTotpCode("backup-code-1234");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(usuarioTest));
        when(trustedDeviceService.isTrustedDevice(any(), any())).thenReturn(false);
        when(totpService.verifyCode(any(), any())).thenReturn(false);
        when(backupCodeService.verifyAndUseBackupCode(any(), any())).thenReturn(true);
        when(backupCodeService.getAvailableCodesCount(any())).thenReturn(5L);
        when(seguridadService.listarRolesPorUsuario(any())).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("access-token");
        when(jwtUtil.generarRefreshToken(any())).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(86400000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(604800000L);

        mockMvc.perform(post("/api/v1/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login-2fa - Credenciales incorrectas")
    void loginWith2FA_credencialesIncorrectas_debeRetornar401() throws Exception {
        Login2FARequestDTO loginRequest = new Login2FARequestDTO();
        loginRequest.setUsuario("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login-2fa - Usuario sin 2FA debe activarlo")
    void loginWith2FA_usuarioSin2FA_debeRequirir2FASetup() throws Exception {
        usuarioTest.setTotpEnabled(false);

        Login2FARequestDTO loginRequest = new Login2FARequestDTO();
        loginRequest.setUsuario("test@example.com");
        loginRequest.setPassword("password123");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(usuarioTest));

        mockMvc.perform(post("/api/v1/auth/login-2fa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.requires2FASetup").value(true))
                .andExpect(jsonPath("$.mensaje").value("Debes activar la autenticación de dos factores para usar el sistema"));
    }

    // ===== TESTS DE SETUP INICIAL DE 2FA =====

    @Test
    @DisplayName("POST /api/v1/auth/2fa/setup-initial - Setup inicial exitoso")
    void setupInitial2FA_credencialesValidas_debeRetornarQR() throws Exception {
        usuarioTest.setTotpEnabled(false);

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "password123");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(usuarioTest));
        when(totpService.generateSecret()).thenReturn("NEWSECRET123");
        when(totpService.generateQrCodeDataUrl(any(), any())).thenReturn("data:image/png;base64,QR");

        mockMvc.perform(post("/api/v1/auth/2fa/setup-initial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Escanea el QR code con Google Authenticator"))
                .andExpect(jsonPath("$.data.secret").exists())
                .andExpect(jsonPath("$.data.qrCodeDataUrl").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/2fa/setup-initial - Usuario ya tiene 2FA")
    void setupInitial2FA_usuarioYaTiene2FA_debeRetornarError() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "password123");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(usuarioTest));

        mockMvc.perform(post("/api/v1/auth/2fa/setup-initial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya tienes 2FA habilitado. Usa el login normal."));
    }

    // ===== TESTS DE VERIFICACIÓN INICIAL DE 2FA =====

    @Test
    @DisplayName("POST /api/v1/auth/2fa/verify-initial - Verificación exitosa activa 2FA")
    void verifyInitial2FA_codigoValido_debeActivar2FA() throws Exception {
        usuarioTest.setTotpEnabled(false);

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("totpCode", "123456");

        List<String> backupCodes = Arrays.asList("code1", "code2", "code3");

        when(usuarioService.buscarPorEmail(any())).thenReturn(Optional.of(usuarioTest));
        when(totpService.verifyCode(any(), any())).thenReturn(true);
        when(backupCodeService.generateBackupCodes(any())).thenReturn(backupCodes);
        when(seguridadService.listarRolesPorUsuario(any())).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("access-token");
        when(jwtUtil.generarRefreshToken(any())).thenReturn("refresh-token");

        mockMvc.perform(post("/api/v1/auth/2fa/verify-initial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("2FA activado exitosamente. GUARDA estos códigos de respaldo."))
                .andExpect(jsonPath("$.totpEnabled").value(true))
                .andExpect(jsonPath("$.backupCodes").isArray())
                .andExpect(jsonPath("$.usuario").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/2fa/verify-initial - Código inválido")
    void verifyInitial2FA_codigoInvalido_debeRetornar401() throws Exception {
        usuarioTest.setTotpEnabled(false);

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("totpCode", "000000");

        when(usuarioService.buscarPorEmail(any())).thenReturn(Optional.of(usuarioTest));
        when(totpService.verifyCode(any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/2fa/verify-initial")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Código 2FA inválido o expirado"));
    }

    // ===== TESTS DE SETUP Y VERIFICACIÓN (AUTENTICADOS) =====

    @Test
    @DisplayName("POST /api/v1/auth/2fa/setup - Setup 2FA para usuario autenticado")
    @WithMockUser(roles = "ADMIN")
    void setup2FA_usuarioAutenticado_debeRetornarQR() throws Exception {
        usuarioTest.setTotpEnabled(false);

        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(usuarioService.buscarPorId(any())).thenReturn(Optional.of(usuarioTest));
        when(totpService.generateSecret()).thenReturn("NEWSECRET123");
        when(totpService.generateQrCodeDataUrl(any(), any())).thenReturn("data:image/png;base64,QR");

        mockMvc.perform(post("/api/v1/auth/2fa/setup")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.data.qrCodeDataUrl").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/2fa/verify - Verificar código y activar 2FA")
    @WithMockUser(roles = "ADMIN")
    void verify2FA_codigoValido_debeActivar2FA() throws Exception {
        usuarioTest.setTotpEnabled(false);

        Verify2FARequestDTO request = new Verify2FARequestDTO();
        request.setTotpCode("123456");

        List<String> backupCodes = Arrays.asList("code1", "code2", "code3");

        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(usuarioService.buscarPorId(any())).thenReturn(Optional.of(usuarioTest));
        when(totpService.verifyCode(any(), any())).thenReturn(true);
        when(totpService.getCurrentCode(any())).thenReturn("123456");
        when(totpService.getRemainingSeconds()).thenReturn(20);
        when(backupCodeService.generateBackupCodes(any())).thenReturn(backupCodes);

        mockMvc.perform(post("/api/v1/auth/2fa/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totpEnabled").value(true))
                .andExpect(jsonPath("$.backupCodes").isArray());
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/2fa/disable - Deshabilitar 2FA")
    @WithMockUser(roles = "ADMIN")
    void disable2FA_codigoValido_debeDeshabilitarExitosamente() throws Exception {
        Verify2FARequestDTO request = new Verify2FARequestDTO();
        request.setTotpCode("123456");

        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(usuarioService.buscarPorId(any())).thenReturn(Optional.of(usuarioTest));
        when(totpService.verifyCode(any(), any())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/auth/2fa/disable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("2FA deshabilitado exitosamente"))
                .andExpect(jsonPath("$.totpEnabled").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/auth/2fa/status - Obtener estado de 2FA")
    @WithMockUser(roles = "ADMIN")
    void get2FAStatus_usuarioCon2FA_debeRetornarEstado() throws Exception {
        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(usuarioService.buscarPorId(any())).thenReturn(Optional.of(usuarioTest));

        mockMvc.perform(get("/api/v1/auth/2fa/status")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totpEnabled").value(true))
                .andExpect(jsonPath("$.hasSecret").value(true));
    }

    // ===== TESTS DE DISPOSITIVOS DE CONFIANZA =====

    @Test
    @DisplayName("GET /api/v1/auth/trusted-devices - Listar dispositivos de confianza")
    @WithMockUser(roles = "ADMIN")
    void listTrustedDevices_debeRetornarDispositivos() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<TrustedDeviceDTO> devices = Arrays.asList(
            new TrustedDeviceDTO(1L, "Chrome en Windows", "Mozilla/5.0...", "192.168.1.1", now, now, now.plusDays(30), true),
            new TrustedDeviceDTO(2L, "Firefox en Linux", "Mozilla/5.0...", "192.168.1.2", now, now, now.plusDays(30), true)
        );

        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(trustedDeviceService.listUserDevices(any())).thenReturn(devices);

        mockMvc.perform(get("/api/v1/auth/trusted-devices")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devices").isArray())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/trusted-devices/{deviceId} - Revocar dispositivo específico")
    @WithMockUser(roles = "ADMIN")
    void revokeTrustedDevice_dispositivoValido_debeRevocar() throws Exception {
        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);

        mockMvc.perform(delete("/api/v1/auth/trusted-devices/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Dispositivo revocado exitosamente"));
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/trusted-devices - Revocar todos los dispositivos")
    @WithMockUser(roles = "ADMIN")
    void revokeAllTrustedDevices_debeRevocarTodos() throws Exception {
        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);

        mockMvc.perform(delete("/api/v1/auth/trusted-devices")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Todos los dispositivos revocados exitosamente"));
    }

    // ===== TESTS DE RECUPERACIÓN DE CONTRASEÑA =====

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password - Solicitar código de recuperación")
    void forgotPassword_emailValido_debeEnviarCodigo() throws Exception {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("test@example.com");

        when(usuarioService.buscarPorEmail(any())).thenReturn(Optional.of(usuarioTest));
        when(recoveryCodeService.generateRecoveryCode()).thenReturn("REC123456");
        when(recoveryCodeService.hashCode(any())).thenReturn("hashedCode");
        when(recoveryCodeService.getExpiryTime()).thenReturn(LocalDateTime.now().plusMinutes(10));
        when(recoveryCodeService.getExpiryMinutes()).thenReturn(10);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.expiresIn").value("10 minutos"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Resetear contraseña con código válido")
    void resetPassword_codigosValidos_debeResetearContrasena() throws Exception {
        usuarioTest.setRecoveryCodeHash("hashedCode");
        usuarioTest.setRecoveryCodeExpiry(LocalDateTime.now().plusMinutes(10));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail("test@example.com");
        request.setRecoveryCode("REC123456");
        request.setTotpCode("123456");
        request.setNewPassword("newPassword123");

        when(usuarioService.buscarPorEmail(any())).thenReturn(Optional.of(usuarioTest));
        when(recoveryCodeService.isExpired(any())).thenReturn(false);
        when(recoveryCodeService.verifyCode(any(), any())).thenReturn(true);
        when(totpService.verifyCode(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - Código de recuperación expirado")
    void resetPassword_codigoExpirado_debeRetornar401() throws Exception {
        usuarioTest.setRecoveryCodeHash("hashedCode");
        usuarioTest.setRecoveryCodeExpiry(LocalDateTime.now().minusMinutes(1));

        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setEmail("test@example.com");
        request.setRecoveryCode("REC123456");
        request.setTotpCode("123456");
        request.setNewPassword("newPassword123");

        when(usuarioService.buscarPorEmail(any())).thenReturn(Optional.of(usuarioTest));
        when(recoveryCodeService.isExpired(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Código de recuperación expirado. Solicita uno nuevo."));
    }

    // ===== TESTS DE CÓDIGOS DE RESPALDO =====

    @Test
    @DisplayName("POST /api/v1/auth/2fa/backup-codes/regenerate - Regenerar códigos de respaldo")
    @WithMockUser(roles = "ADMIN")
    void regenerateBackupCodes_codigoValido_debeGenerarNuevosCodigos() throws Exception {
        Verify2FARequestDTO request = new Verify2FARequestDTO();
        request.setTotpCode("123456");

        List<String> newBackupCodes = Arrays.asList("new1", "new2", "new3");

        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(usuarioService.buscarPorId(any())).thenReturn(Optional.of(usuarioTest));
        when(totpService.verifyCode(any(), any())).thenReturn(true);
        when(backupCodeService.regenerateBackupCodes(any())).thenReturn(newBackupCodes);

        mockMvc.perform(post("/api/v1/auth/2fa/backup-codes/regenerate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backupCodes").isArray())
                .andExpect(jsonPath("$.totalCodes").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/auth/2fa/backup-codes/count - Contar códigos disponibles")
    @WithMockUser(roles = "ADMIN")
    void getBackupCodesCount_debeRetornarConteo() throws Exception {
        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(usuarioService.buscarPorId(any())).thenReturn(Optional.of(usuarioTest));
        when(backupCodeService.getAvailableCodesCount(any())).thenReturn(8L);

        mockMvc.perform(get("/api/v1/auth/2fa/backup-codes/count")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCodes").value(8));
    }

    @Test
    @DisplayName("GET /api/v1/auth/2fa/backup-codes/count - Advertencia con pocos códigos")
    @WithMockUser(roles = "ADMIN")
    void getBackupCodesCount_pocosCodigos_debeRetornarAdvertencia() throws Exception {
        when(seguridadService.obtenerUsuarioAutenticado()).thenReturn(1);
        when(usuarioService.buscarPorId(any())).thenReturn(Optional.of(usuarioTest));
        when(backupCodeService.getAvailableCodesCount(any())).thenReturn(2L);

        mockMvc.perform(get("/api/v1/auth/2fa/backup-codes/count")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCodes").value(2))
                .andExpect(jsonPath("$.warning").value("Quedan pocos códigos de respaldo. Considera regenerarlos."));
    }

    // ===== TESTS DE ADMIN SETUP =====

    @Test
    @DisplayName("GET /api/v1/auth/admin/setup-data/{token} - Obtener datos de setup con token")
    void getSetupData_tokenValido_debeRetornarDatos() throws Exception {
        Map<String, Object> setupData = new HashMap<>();
        setupData.put("userId", 1);
        setupData.put("name", "Admin");
        setupData.put("qrCode", "data:image/png;base64,QR");

        when(setupTokenService.consumeSetupToken(any())).thenReturn(setupData);

        mockMvc.perform(get("/api/v1/auth/admin/setup-data/valid-token")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.name").value("Admin"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/admin/setup-data/{token} - Token inválido")
    void getSetupData_tokenInvalido_debeRetornar404() throws Exception {
        when(setupTokenService.consumeSetupToken(any())).thenReturn(null);

        mockMvc.perform(get("/api/v1/auth/admin/setup-data/invalid-token")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Token inválido o expirado"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/admin/complete-setup - Completar setup de admin")
    void completeAdminSetup_datosValidos_debeCompletarSetup() throws Exception {
        Usuario admin = new Usuario();
        admin.setUsuarioID(1);
        admin.setNombre("admin");
        admin.setNombres("Admin");
        admin.setApellidos("User");
        admin.setEmail("admin@temporal.local");
        admin.setTotpSecret("ADMINSECRET123");
        admin.setRequiereCambioCredenciales(true);
        admin.setRol(utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN);

        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "admin123");
        request.put("newEmail", "admin@inia.com");
        request.put("newPassword", "newSecurePass123");
        request.put("totpCode", "123456");

        List<String> backupCodes = Arrays.asList("backup1", "backup2");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.of(admin));
        when(totpService.verifyCode(any(), any())).thenReturn(true);
        when(backupCodeService.generateBackupCodes(any())).thenReturn(backupCodes);
        when(seguridadService.listarRolesPorUsuario(any())).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("access-token");
        when(jwtUtil.generarRefreshToken(any())).thenReturn("refresh-token");

        mockMvc.perform(post("/api/v1/auth/admin/complete-setup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Configuración completada exitosamente"))
                .andExpect(jsonPath("$.backupCodes").isArray())
                .andExpect(jsonPath("$.usuario").exists());
    }
}
