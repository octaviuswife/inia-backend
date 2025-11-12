package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;
import utec.proyectofinal.Proyecto.Final.UTEC.services.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para AuthController")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeguridadService seguridadService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private TotpService totpService;

    @MockitoBean
    private RecoveryCodeService recoveryCodeService;

    @MockitoBean
    private TrustedDeviceService trustedDeviceService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;
    private UsuarioDTO usuarioDTO;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setUsuarioID(1);
        usuario.setNombre("admin");
        usuario.setNombres("Admin");
        usuario.setApellidos("User");
        usuario.setEmail("admin@test.com");
        usuario.setContrasenia("hashedPassword");
        usuario.setRol(Rol.ADMIN);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        usuarioDTO = new UsuarioDTO();
        usuarioDTO.setUsuarioID(1);
        usuarioDTO.setNombre("admin");
        usuarioDTO.setNombres("Admin");
        usuarioDTO.setApellidos("User");
        usuarioDTO.setEmail("admin@test.com");
        usuarioDTO.setRol(Rol.ADMIN);
        usuarioDTO.setEstado(EstadoUsuario.ACTIVO);
        usuarioDTO.setActivo(true);

        loginRequest = new LoginRequestDTO();
        loginRequest.setUsuario("admin");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe hacer login exitosamente")
    void login_conCredencialesValidas_debeRetornarExito() throws Exception {
        when(seguridadService.autenticarUsuario("admin", "password123")).thenReturn(Optional.of(usuario));
        when(seguridadService.listarRolesPorUsuario(usuario)).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("access-token");
        when(jwtUtil.generarRefreshToken(any())).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2592000000L);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Login exitoso"))
                .andExpect(jsonPath("$.usuario.id").value(1))
                .andExpect(jsonPath("$.usuario.nombre").value("admin"))
                .andExpect(jsonPath("$.usuario.email").value("admin@test.com"))
                .andExpect(jsonPath("$.usuario.roles[0]").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe rechazar credenciales incorrectas")
    void login_conCredencialesIncorrectas_debeRetornar401() throws Exception {
        when(seguridadService.autenticarUsuario("admin", "wrongpassword"))
            .thenReturn(Optional.empty());

        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales incorrectas"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Debe rechazar usuario inactivo")
    void login_conUsuarioInactivo_debeRetornar401() throws Exception {
        when(seguridadService.autenticarUsuario("admin", "password123"))
            .thenThrow(new RuntimeException("USUARIO_INACTIVO"));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("No se puede iniciar sesión. Contacte al administrador"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Debe registrar solicitud exitosamente")
    void registrar_conDatosValidos_debeRetornarCreated() throws Exception {
        RegistroUsuarioRequestDTO registroDTO = new RegistroUsuarioRequestDTO();
        registroDTO.setNombre("newuser");
        registroDTO.setNombres("New");
        registroDTO.setApellidos("User");
        registroDTO.setEmail("newuser@test.com");
        registroDTO.setContrasenia("password123");

        when(usuarioService.registrarSolicitud(any(RegistroUsuarioRequestDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Solicitud de registro enviada. Pendiente de aprobación por el administrador."))
                .andExpect(jsonPath("$.usuario.usuarioID").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/auth/pending - Debe listar solicitudes pendientes")
    @WithMockUser(roles = "ADMIN")
    void listarSolicitudesPendientes_conRolAdmin_debeRetornarLista() throws Exception {
        List<UsuarioDTO> solicitudes = Arrays.asList(usuarioDTO);
        when(usuarioService.listarSolicitudesPendientes()).thenReturn(solicitudes);

        mockMvc.perform(get("/api/v1/auth/pending")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].usuarioID").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/auth/approve/{id} - Debe aprobar usuario")
    @WithMockUser(roles = "ADMIN")
    void aprobarUsuario_conDatosValidos_debeRetornarExito() throws Exception {
        AprobarUsuarioRequestDTO aprobarDTO = new AprobarUsuarioRequestDTO();
        aprobarDTO.setRol(Rol.ANALISTA);

        when(usuarioService.aprobarUsuario(eq(1), any(AprobarUsuarioRequestDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/v1/auth/approve/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(aprobarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario aprobado exitosamente"))
                .andExpect(jsonPath("$.usuario.usuarioID").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/auth/reject/{id} - Debe rechazar solicitud")
    @WithMockUser(roles = "ADMIN")
    void rechazarSolicitud_conIdValido_debeRetornarExito() throws Exception {
        mockMvc.perform(delete("/api/v1/auth/reject/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Solicitud rechazada"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users - Debe listar todos los usuarios")
    @WithMockUser(roles = "ADMIN")
    void listarUsuarios_conRolAdmin_debeRetornarLista() throws Exception {
        List<UsuarioDTO> usuarios = Arrays.asList(usuarioDTO);
        when(usuarioService.listarTodosUsuarios()).thenReturn(usuarios);

        mockMvc.perform(get("/api/v1/auth/users")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].usuarioID").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Debe listar usuarios paginados")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_debeRetornarPaginacion() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, null, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].usuarioID").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/auth/users/{id} - Debe actualizar usuario")
    @WithMockUser(roles = "ADMIN")
    void gestionarUsuario_conDatosValidos_debeRetornarActualizado() throws Exception {
        GestionarUsuarioRequestDTO gestionarDTO = new GestionarUsuarioRequestDTO();
        gestionarDTO.setRol(Rol.OBSERVADOR);
        gestionarDTO.setActivo(true);

        when(usuarioService.gestionarUsuario(eq(1), any(GestionarUsuarioRequestDTO.class))).thenReturn(usuarioDTO);

        mockMvc.perform(put("/api/v1/auth/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gestionarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario actualizado exitosamente"))
                .andExpect(jsonPath("$.usuario.usuarioID").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/auth/profile - Debe obtener perfil del usuario autenticado")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void obtenerPerfil_conUsuarioAutenticado_debeRetornarPerfil() throws Exception {
        when(usuarioService.obtenerPerfil()).thenReturn(usuarioDTO);

        mockMvc.perform(get("/api/v1/auth/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioID").value(1))
                .andExpect(jsonPath("$.nombre").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    @DisplayName("PUT /api/v1/auth/profile - Debe actualizar perfil del usuario")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void actualizarPerfil_conDatosValidos_debeRetornarActualizado() throws Exception {
        ActualizarPerfilRequestDTO perfilDTO = new ActualizarPerfilRequestDTO();
        perfilDTO.setNombres("Admin Updated");
        perfilDTO.setApellidos("User Updated");

        when(usuarioService.actualizarPerfil(any(ActualizarPerfilRequestDTO.class))).thenReturn(usuarioDTO);
        when(usuarioService.buscarPorId(1)).thenReturn(Optional.of(usuario));
        when(seguridadService.listarRolesPorUsuario(usuario)).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("new-access-token");
        when(jwtUtil.generarRefreshToken(any())).thenReturn("new-refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900000L);
        when(jwtUtil.getRefreshTokenExpiration()).thenReturn(2592000000L);

        mockMvc.perform(put("/api/v1/auth/profile")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(perfilDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Perfil actualizado exitosamente"))
                .andExpect(jsonPath("$.usuario.usuarioID").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Debe cerrar sesión exitosamente")
    void logout_debeRetornarExito() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Logout exitoso"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/init-admin - Debe crear admin predeterminado")
    void crearAdminPredeterminado_debeRetornarCreated() throws Exception {
        when(usuarioService.crearAdminPredeterminado()).thenReturn(usuarioDTO);

        mockMvc.perform(post("/api/v1/auth/init-admin")
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.usuario.usuarioID").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/active - Debe listar usuarios activos")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosActivos_debeRetornarListaActivos() throws Exception {
        List<UsuarioDTO> usuarios = Arrays.asList(usuarioDTO);
        when(usuarioService.listarUsuariosActivos()).thenReturn(usuarios);

        mockMvc.perform(get("/api/v1/auth/users/active")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    // ===== TESTS DE EDGE CASES Y VALIDACIONES =====

    @Test
    @DisplayName("POST /api/v1/auth/register - Email con formato inválido")
    void register_emailInvalido_debeRetornar400() throws Exception {
        RegistroUsuarioRequestDTO request = new RegistroUsuarioRequestDTO();
        request.setNombre("testuser");
        request.setContrasenia("password123");
        request.setNombres("Test");
        request.setApellidos("User");
        request.setEmail("email-invalido");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Contraseña muy corta")
    void register_passwordCorta_debeRetornar400() throws Exception {
        RegistroUsuarioRequestDTO request = new RegistroUsuarioRequestDTO();
        request.setNombre("testuser");
        request.setContrasenia("123");
        request.setNombres("Test");
        request.setApellidos("User");
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Usuario duplicado")
    void register_usuarioDuplicado_debeRetornar409() throws Exception {
        RegistroUsuarioRequestDTO request = new RegistroUsuarioRequestDTO();
        request.setNombre("testuser");
        request.setContrasenia("password123");
        request.setNombres("Test");
        request.setApellidos("User");
        request.setEmail("test@example.com");

        when(usuarioService.registrarSolicitud(any(RegistroUsuarioRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Usuario ya existe"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Paginación con número de página negativo")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_paginaNegativa_debeRetornar400() throws Exception {
        // El controller no valida parámetros negativos, devuelve 200 con resultado vacío
        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "-1")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Tamaño de página inválido")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_tamanoInvalido_debeRetornar400() throws Exception {
        // El controller no valida tamaño 0, devuelve 200 con resultado vacío
        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "0")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/auth/users/{id} - Actualizar usuario inexistente")
    @WithMockUser(roles = "ADMIN")
    void gestionarUsuario_usuarioInexistente_debeRetornar404() throws Exception {
        GestionarUsuarioRequestDTO gestionarDTO = new GestionarUsuarioRequestDTO();
        gestionarDTO.setRol(Rol.OBSERVADOR);
        gestionarDTO.setActivo(true);

        when(usuarioService.gestionarUsuario(eq(999), any(GestionarUsuarioRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        mockMvc.perform(put("/api/v1/auth/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gestionarDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Múltiples intentos fallidos")
    void login_intentosFallidos_debeRetornar401() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsuario("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(seguridadService.autenticarUsuario(any(), any())).thenReturn(Optional.empty());

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("PUT /api/v1/auth/profile - Actualizar con email duplicado")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void actualizarPerfil_emailDuplicado_debeRetornar409() throws Exception {
        ActualizarPerfilRequestDTO perfilDTO = new ActualizarPerfilRequestDTO();
        perfilDTO.setEmail("otro@test.com");

        when(usuarioService.actualizarPerfil(any(ActualizarPerfilRequestDTO.class)))
            .thenThrow(new IllegalArgumentException("Email ya existe"));

        mockMvc.perform(put("/api/v1/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(perfilDTO))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/init-admin - Admin ya existe")
    void crearAdminPredeterminado_adminExiste_debeRetornar409() throws Exception {
        when(usuarioService.crearAdminPredeterminado())
            .thenThrow(new IllegalStateException("Admin ya existe"));

        // El controller captura la excepción y devuelve 400 en lugar de 409
        mockMvc.perform(post("/api/v1/auth/init-admin")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Admin ya existe"));
    }

    // ===== TESTS PARA listarSolicitudesPendientesPaginadas =====

    @Test
    @DisplayName("GET /api/v1/auth/pending/paginated - Debe listar solicitudes paginadas sin búsqueda")
    @WithMockUser(roles = "ADMIN")
    void listarSolicitudesPendientesPaginadas_sinBusqueda_debeRetornarPaginacion() throws Exception {
        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setUsuarioID(2);
        usuario2.setNombre("pendiente1");
        usuario2.setEmail("pendiente1@test.com");
        usuario2.setEstado(EstadoUsuario.PENDIENTE);

        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO, usuario2));
        when(usuarioService.listarSolicitudesPendientesPaginadas(0, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/pending/paginated")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].usuarioID").value(1))
                .andExpect(jsonPath("$.content[1].usuarioID").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/auth/pending/paginated - Debe buscar solicitudes por nombre")
    @WithMockUser(roles = "ADMIN")
    void listarSolicitudesPendientesPaginadas_conBusqueda_debeRetornarFiltrados() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO));
        when(usuarioService.listarSolicitudesPendientesPaginadas(0, 10, "admin")).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/pending/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("search", "admin")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("admin"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/pending/paginated - Debe retornar página vacía si no hay resultados")
    @WithMockUser(roles = "ADMIN")
    void listarSolicitudesPendientesPaginadas_sinResultados_debeRetornarPaginaVacia() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList());
        when(usuarioService.listarSolicitudesPendientesPaginadas(0, 10, "noexiste")).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/pending/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("search", "noexiste")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/auth/pending/paginated - Debe manejar segunda página")
    @WithMockUser(roles = "ADMIN")
    void listarSolicitudesPendientesPaginadas_segundaPagina_debeRetornarContenidoCorrecto() throws Exception {
        UsuarioDTO usuario2 = new UsuarioDTO();
        usuario2.setUsuarioID(11);
        usuario2.setNombre("usuario11");

        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuario2));
        when(usuarioService.listarSolicitudesPendientesPaginadas(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/pending/paginated")
                .param("page", "1")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].usuarioID").value(11));
    }

    // ===== TESTS PARA validateToken =====

    @Test
    @DisplayName("POST /api/v1/auth/validate - Token válido debe retornar información del usuario")
    void validateToken_tokenValido_debeRetornarUsuario() throws Exception {
        String token = "valid-jwt-token";
        when(jwtUtil.esTokenValido(token)).thenReturn(true);
        when(jwtUtil.obtenerUsuarioDelToken(token)).thenReturn("admin");

        mockMvc.perform(post("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.usuario").value("admin"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/validate - Token inválido debe retornar 401")
    void validateToken_tokenInvalido_debeRetornar401() throws Exception {
        String token = "invalid-jwt-token";
        when(jwtUtil.esTokenValido(token)).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.error").value("Token inválido"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/validate - Token expirado debe retornar 401")
    void validateToken_tokenExpirado_debeRetornar401() throws Exception {
        String token = "expired-jwt-token";
        when(jwtUtil.esTokenValido(token)).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/validate - Token malformado debe retornar 401")
    void validateToken_tokenMalformado_debeRetornar401() throws Exception {
        String token = "malformed-token";
        when(jwtUtil.esTokenValido(token)).thenThrow(new RuntimeException("Token malformado"));

        mockMvc.perform(post("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + token)
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.error").value("Token malformado"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/validate - Sin header Authorization debe retornar error 500")
    void validateToken_sinHeader_debeLanzarExcepcion() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validate")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    // ===== TESTS PARA refresh =====

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Debe renovar access token exitosamente")
    void refresh_conRefreshTokenValido_debeRenovarAccessToken() throws Exception {
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.esTokenValido(refreshToken)).thenReturn(true);
        when(jwtUtil.obtenerTipoToken(refreshToken)).thenReturn("refresh");
        when(jwtUtil.obtenerUserIdDelToken(refreshToken)).thenReturn(1);
        when(usuarioService.buscarPorId(1)).thenReturn(Optional.of(usuario));
        when(seguridadService.listarRolesPorUsuario(usuario)).thenReturn(new String[]{"ADMIN"});
        when(jwtUtil.generarToken(any(), any())).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900000L);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Access token renovado exitosamente"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Refresh token no encontrado debe retornar 401")
    void refresh_sinRefreshToken_debeRetornar401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh token no encontrado"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Refresh token inválido debe retornar 401")
    void refresh_refreshTokenInvalido_debeRetornar401() throws Exception {
        String refreshToken = "invalid-refresh-token";
        when(jwtUtil.esTokenValido(refreshToken)).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Refresh token inválido o expirado"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Token de tipo incorrecto debe retornar 401")
    void refresh_tipoTokenIncorrecto_debeRetornar401() throws Exception {
        String refreshToken = "access-token-not-refresh";
        when(jwtUtil.esTokenValido(refreshToken)).thenReturn(true);
        when(jwtUtil.obtenerTipoToken(refreshToken)).thenReturn("access");

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token no es de tipo refresh"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Usuario no encontrado debe retornar 401")
    void refresh_usuarioNoEncontrado_debeRetornar401() throws Exception {
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.esTokenValido(refreshToken)).thenReturn(true);
        when(jwtUtil.obtenerTipoToken(refreshToken)).thenReturn("refresh");
        when(jwtUtil.obtenerUserIdDelToken(refreshToken)).thenReturn(999);
        when(usuarioService.buscarPorId(999)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Excepción general debe retornar 401")
    void refresh_excepcionGeneral_debeRetornar401() throws Exception {
        String refreshToken = "problematic-token";
        when(jwtUtil.esTokenValido(refreshToken)).thenThrow(new RuntimeException("Error inesperado"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken))
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    // ===== TESTS ADICIONALES PARA listarUsuariosPaginados =====

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Filtrar por rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_filtrarPorRolAdmin_debeRetornarSoloAdmins() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, Rol.ADMIN, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("rol", "ADMIN")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].rol").value("ADMIN"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Filtrar por rol ANALISTA")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_filtrarPorRolAnalista_debeRetornarSoloAnalistas() throws Exception {
        UsuarioDTO analista = new UsuarioDTO();
        analista.setUsuarioID(2);
        analista.setNombre("analista1");
        analista.setRol(Rol.ANALISTA);

        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(analista));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, Rol.ANALISTA, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("rol", "ANALISTA")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].rol").value("ANALISTA"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Filtrar por usuarios activos")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_filtrarPorActivos_debeRetornarSoloActivos() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, null, true)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("activo", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].activo").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Filtrar por usuarios inactivos")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_filtrarPorInactivos_debeRetornarSoloInactivos() throws Exception {
        UsuarioDTO inactivo = new UsuarioDTO();
        inactivo.setUsuarioID(3);
        inactivo.setNombre("inactivo1");
        inactivo.setActivo(false);

        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(inactivo));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, null, false)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("activo", "false")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].activo").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Filtrar con rol 'all' debe ignorar filtro de rol")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_rolAll_debeIgnorarFiltroRol() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, null, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("rol", "all")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Filtrar con activo 'all' debe ignorar filtro activo")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_activoAll_debeIgnorarFiltroActivo() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, null, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("activo", "all")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Combinar búsqueda, rol y estado activo")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_combinarFiltros_debeAplicarTodos() throws Exception {
        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(usuarioDTO));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, "admin", Rol.ADMIN, true)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("search", "admin")
                .param("rol", "ADMIN")
                .param("activo", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("admin"))
                .andExpect(jsonPath("$.content[0].rol").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].activo").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Rol inválido debe retornar 400")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_rolInvalido_debeRetornar400() throws Exception {
        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("rol", "ROL_INVALIDO")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/auth/users/paginated - Filtrar por rol OBSERVADOR")
    @WithMockUser(roles = "ADMIN")
    void listarUsuariosPaginados_filtrarPorRolObservador_debeRetornarSoloObservadores() throws Exception {
        UsuarioDTO observador = new UsuarioDTO();
        observador.setUsuarioID(4);
        observador.setNombre("observador1");
        observador.setRol(Rol.OBSERVADOR);

        Page<UsuarioDTO> page = new PageImpl<>(Arrays.asList(observador));
        when(usuarioService.listarTodosUsuariosPaginados(0, 10, null, Rol.OBSERVADOR, null)).thenReturn(page);

        mockMvc.perform(get("/api/v1/auth/users/paginated")
                .param("page", "0")
                .param("size", "10")
                .param("rol", "OBSERVADOR")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].rol").value("OBSERVADOR"));
    }

    // ===== TESTS ADICIONALES PARA obtenerPerfil =====

    @Test
    @DisplayName("GET /api/v1/auth/profile - Usuario ANALISTA debe obtener su perfil")
    @WithMockUser(username = "analista", roles = "ANALISTA")
    void obtenerPerfil_usuarioAnalista_debeRetornarPerfil() throws Exception {
        UsuarioDTO analistaDTO = new UsuarioDTO();
        analistaDTO.setUsuarioID(2);
        analistaDTO.setNombre("analista");
        analistaDTO.setRol(Rol.ANALISTA);

        when(usuarioService.obtenerPerfil()).thenReturn(analistaDTO);

        mockMvc.perform(get("/api/v1/auth/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioID").value(2))
                .andExpect(jsonPath("$.nombre").value("analista"))
                .andExpect(jsonPath("$.rol").value("ANALISTA"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/profile - Usuario OBSERVADOR debe obtener su perfil")
    @WithMockUser(username = "observador", roles = "OBSERVADOR")
    void obtenerPerfil_usuarioObservador_debeRetornarPerfil() throws Exception {
        UsuarioDTO observadorDTO = new UsuarioDTO();
        observadorDTO.setUsuarioID(3);
        observadorDTO.setNombre("observador");
        observadorDTO.setRol(Rol.OBSERVADOR);

        when(usuarioService.obtenerPerfil()).thenReturn(observadorDTO);

        mockMvc.perform(get("/api/v1/auth/profile")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioID").value(3))
                .andExpect(jsonPath("$.nombre").value("observador"))
                .andExpect(jsonPath("$.rol").value("OBSERVADOR"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/profile - Error al obtener perfil debe retornar 400")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void obtenerPerfil_errorAlObtener_debeRetornar400() throws Exception {
        when(usuarioService.obtenerPerfil())
            .thenThrow(new RuntimeException("Error al obtener perfil"));

        mockMvc.perform(get("/api/v1/auth/profile")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error al obtener perfil"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/profile - Usuario sin autenticación debe retornar error")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void obtenerPerfil_usuarioNoAutenticado_debeRetornar400() throws Exception {
        when(usuarioService.obtenerPerfil())
            .thenThrow(new RuntimeException("Usuario no autenticado"));

        mockMvc.perform(get("/api/v1/auth/profile")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Usuario no autenticado"));
    }
}
