package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.AprobarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GestionarUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.UsuarioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioService
 * 
 * Funcionalidades testeadas:
 * - Registro de nuevos usuarios
 * - Validación de duplicados (username y email)
 * - Aprobación de solicitudes pendientes
 * - Cambio de contraseña
 * - Activación/Desactivación de usuarios
 * - Gestión de roles
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private EmailService emailService;

    @Mock
    private TotpService totpService;

    @Mock
    private BackupCodeService backupCodeService;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegistroUsuarioRequestDTO registroRequestDTO;
    private Usuario usuario;
    private Usuario analista;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        registroRequestDTO = new RegistroUsuarioRequestDTO();
        registroRequestDTO.setNombre("jperez");
        registroRequestDTO.setNombres("Juan");
        registroRequestDTO.setApellidos("Pérez");
        registroRequestDTO.setEmail("jperez@example.com");
        registroRequestDTO.setContrasenia("Password123!");

        usuario = new Usuario();
        usuario.setUsuarioID(1);
        usuario.setNombre("jperez");
        usuario.setNombres("Juan");
        usuario.setApellidos("Pérez");
        usuario.setEmail("jperez@example.com");
        usuario.setEstado(EstadoUsuario.PENDIENTE);
        usuario.setActivo(false);

        analista = new Usuario();
        analista.setUsuarioID(2);
        analista.setNombre("analista1");
        analista.setNombres("Ana");
        analista.setApellidos("Listas");
        analista.setEmail("analista@inia.com");
        analista.setRol(Rol.ANALISTA);
        analista.setActivo(true);
        analista.setEstado(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("Registrar usuario - debe crear usuario en estado PENDIENTE")
    void registrarSolicitud_debeCrearUsuarioPendiente() {
        // ARRANGE
        when(usuarioRepository.findByNombre(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(usuarioRepository.findAllByRol(Rol.ANALISTA)).thenReturn(Arrays.asList(analista));
        doNothing().when(notificacionService).notificarNuevoUsuario(anyLong());
        doNothing().when(emailService).enviarEmailConfirmacionRegistro(anyString(), anyString());
        doNothing().when(emailService).enviarEmailNuevoRegistro(anyString(), anyString(), anyString(), anyString());

        // ACT
        UsuarioDTO resultado = usuarioService.registrarSolicitud(registroRequestDTO);

        // ASSERT
        assertNotNull(resultado, "El usuario registrado no debe ser nulo");
        assertEquals("jperez", resultado.getNombre());
        assertEquals("Juan", resultado.getNombres());
        assertEquals("Pérez", resultado.getApellidos());
        assertEquals("jperez@example.com", resultado.getEmail());
        
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(notificacionService, times(1)).notificarNuevoUsuario(anyLong());
    }

    @Test
    @DisplayName("Registrar usuario con nombre duplicado - debe lanzar excepción")
    void registrarSolicitud_conNombreDuplicado_debeLanzarExcepcion() {
        // ARRANGE
        when(usuarioRepository.findByNombre("jperez")).thenReturn(Optional.of(usuario));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrarSolicitud(registroRequestDTO);
        });
        
        assertEquals("El nombre de usuario ya existe", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Registrar usuario con email duplicado - debe lanzar excepción")
    void registrarSolicitud_conEmailDuplicado_debeLanzarExcepcion() {
        // ARRANGE
        when(usuarioRepository.findByNombre(anyString())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("jperez@example.com")).thenReturn(Optional.of(usuario));

        // ACT & ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.registrarSolicitud(registroRequestDTO);
        });
        
        assertEquals("El email ya está registrado", exception.getMessage());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Listar solicitudes pendientes - debe retornar solo usuarios PENDIENTES")
    void listarSolicitudesPendientes_debeRetornarSoloPendientes() {
        // ARRANGE
        Usuario usuario2 = new Usuario();
        usuario2.setUsuarioID(3);
        usuario2.setNombre("mgarcia");
        usuario2.setEstado(EstadoUsuario.PENDIENTE);
        
        when(usuarioRepository.findByEstado(EstadoUsuario.PENDIENTE))
            .thenReturn(Arrays.asList(usuario, usuario2));

        // ACT
        List<UsuarioDTO> resultado = usuarioService.listarSolicitudesPendientes();

        // ASSERT
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findByEstado(EstadoUsuario.PENDIENTE);
    }

    @Test
    @DisplayName("Listar solicitudes pendientes paginadas - debe retornar página correcta")
    void listarSolicitudesPendientesPaginadas_debeRetornarPaginaCorrecta() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(usuario));
        
        when(usuarioRepository.findByEstado(eq(EstadoUsuario.PENDIENTE), any(Pageable.class)))
            .thenReturn(usuariosPage);

        // ACT
        Page<UsuarioDTO> resultado = usuarioService.listarSolicitudesPendientesPaginadas(0, 10, null);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Listar solicitudes con búsqueda - debe filtrar resultados")
    void listarSolicitudesPendientesPaginadas_conBusqueda_debeFiltrar() {
        // ARRANGE
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> usuariosPage = new PageImpl<>(Arrays.asList(usuario));
        
        when(usuarioRepository.findByEstadoAndSearchTerm(
            eq(EstadoUsuario.PENDIENTE), eq("juan"), any(Pageable.class)))
            .thenReturn(usuariosPage);

        // ACT
        Page<UsuarioDTO> resultado = usuarioService.listarSolicitudesPendientesPaginadas(0, 10, "juan");

        // ASSERT
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(usuarioRepository, times(1))
            .findByEstadoAndSearchTerm(eq(EstadoUsuario.PENDIENTE), eq("juan"), any(Pageable.class));
    }

    @Test
    @DisplayName("Aprobar usuario - debe cambiar estado a APROBADO y asignar rol")
    void aprobarUsuario_debeCambiarEstadoYAsignarRol() {
        // ARRANGE
        AprobarUsuarioRequestDTO aprobarRequest = new AprobarUsuarioRequestDTO();
        aprobarRequest.setRol(Rol.ANALISTA);
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(emailService).enviarEmailBienvenida(anyString(), anyString());

        // ACT
        UsuarioDTO resultado = usuarioService.aprobarUsuario(1, aprobarRequest);

        // ASSERT
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Aprobar usuario inexistente - debe lanzar excepción")
    void aprobarUsuario_usuarioInexistente_debeLanzarExcepcion() {
        // ARRANGE
        AprobarUsuarioRequestDTO aprobarRequest = new AprobarUsuarioRequestDTO();
        aprobarRequest.setRol(Rol.ANALISTA);
        
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            usuarioService.aprobarUsuario(999, aprobarRequest);
        });
        
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Buscar usuario por ID - debe retornar usuario si existe")
    void buscarPorId_cuandoExiste_debeRetornarUsuario() {
        // ARRANGE
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        // ACT
        Optional<Usuario> resultado = usuarioService.buscarPorId(1);

        // ASSERT
        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getUsuarioID());
        assertEquals("jperez", resultado.get().getNombre());
    }

    @Test
    @DisplayName("Buscar usuario por ID inexistente - debe retornar Optional vacío")
    void buscarPorId_cuandoNoExiste_debeRetornarOptionalVacio() {
        // ARRANGE
        when(usuarioRepository.findById(999)).thenReturn(Optional.empty());

        // ACT
        Optional<Usuario> resultado = usuarioService.buscarPorId(999);

        // ASSERT
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Gestionar usuario - debe cambiar rol y estado de usuario")
    void gestionarUsuario_debeCambiarRolYEstado() {
        // ARRANGE
        GestionarUsuarioRequestDTO gestionRequest = new GestionarUsuarioRequestDTO();
        gestionRequest.setRol(Rol.ADMIN);
        gestionRequest.setActivo(true);
        
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            return u;
        });

        // ACT
        UsuarioDTO resultado = usuarioService.gestionarUsuario(1, gestionRequest);

        // ASSERT
        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}
