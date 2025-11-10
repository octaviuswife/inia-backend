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
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private SeguridadService seguridadService;

    @MockBean
    private JwtUtil jwtUtil;

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
}
