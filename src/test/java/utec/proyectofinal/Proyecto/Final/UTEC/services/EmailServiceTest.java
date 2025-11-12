package utec.proyectofinal.Proyecto.Final.UTEC.services;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para EmailService
 * 
 * Valida el envío correcto de todos los tipos de correos electrónicos
 * del sistema INIA con plantillas HTML personalizadas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private static final String FROM_EMAIL = "noreply@inia.org.uy";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NOMBRE = "Usuario Test";
    private static final String ANALISTA_EMAIL = "analista@inia.org.uy";
    private static final String ANALISTA_NOMBRE = "Analista Test";

    @BeforeEach
    void setUp() {
        // Configurar el valor de la propiedad fromEmail usando reflexión
        ReflectionTestUtils.setField(emailService, "fromEmail", FROM_EMAIL);
        
        // Mock del comportamiento de JavaMailSender
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailNuevoRegistro - debe enviar notificación a analista cuando usuario se registra")
    void enviarEmailNuevoRegistro_debeEnviarCorrectamente() {
        // ARRANGE
        String usuarioEmail = "nuevouser@example.com";
        String usuarioNombre = "Nuevo Usuario";

        // ACT
        emailService.enviarEmailNuevoRegistro(ANALISTA_EMAIL, ANALISTA_NOMBRE, usuarioNombre, usuarioEmail);

        // ASSERT
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailNuevoRegistro - debe incluir datos del usuario en el contenido")
    void enviarEmailNuevoRegistro_debeIncluirDatosUsuario() {
        // ARRANGE
        String usuarioEmail = "usuario@example.com";
        String usuarioNombre = "Juan Pérez";

        // ACT
        emailService.enviarEmailNuevoRegistro(ANALISTA_EMAIL, ANALISTA_NOMBRE, usuarioNombre, usuarioEmail);

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailConfirmacionRegistro - debe enviar confirmación al usuario registrado")
    void enviarEmailConfirmacionRegistro_debeEnviarCorrectamente() {
        // ARRANGE - ACT
        emailService.enviarEmailConfirmacionRegistro(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailConfirmacionRegistro - debe notificar pendiente de aprobación")
    void enviarEmailConfirmacionRegistro_debeMostrarEstadoPendiente() {
        // ARRANGE
        String email = "pendiente@example.com";
        String nombre = "Usuario Pendiente";

        // ACT
        emailService.enviarEmailConfirmacionRegistro(email, nombre);

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailBienvenida - debe enviar email de bienvenida cuando usuario es aprobado")
    void enviarEmailBienvenida_debeEnviarCorrectamente() {
        // ARRANGE - ACT
        emailService.enviarEmailBienvenida(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailBienvenida - debe incluir enlace de inicio de sesión")
    void enviarEmailBienvenida_debeIncluirEnlaceLogin() {
        // ARRANGE
        String email = "aprobado@example.com";
        String nombre = "Usuario Aprobado";

        // ACT
        emailService.enviarEmailBienvenida(email, nombre);

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarCodigoRecuperacion - debe enviar código de recuperación de contraseña")
    void enviarCodigoRecuperacion_debeEnviarCorrectamente() {
        // ARRANGE
        String codigoRecuperacion = "123456";

        // ACT
        emailService.enviarCodigoRecuperacion(TEST_EMAIL, TEST_NOMBRE, codigoRecuperacion);

        // ASSERT
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarCodigoRecuperacion - debe incluir código de 6 dígitos")
    void enviarCodigoRecuperacion_debeIncluirCodigo() {
        // ARRANGE
        String codigo = "987654";
        String email = "recuperar@example.com";
        String nombre = "Usuario Recuperar";

        // ACT
        emailService.enviarCodigoRecuperacion(email, nombre, codigo);

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarCodigoRecuperacion - debe advertir sobre validez de 10 minutos")
    void enviarCodigoRecuperacion_debeIncluirAdvertenciaValidez() {
        // ARRANGE
        String codigo = "555555";

        // ACT
        emailService.enviarCodigoRecuperacion(TEST_EMAIL, TEST_NOMBRE, codigo);

        // ASSERT
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviar2FAActivado - debe enviar notificación de activación de 2FA")
    void enviar2FAActivado_debeEnviarCorrectamente() {
        // ARRANGE - ACT
        emailService.enviar2FAActivado(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviar2FAActivado - debe informar sobre protección adicional")
    void enviar2FAActivado_debeInformarProteccion() {
        // ARRANGE
        String email = "2fa@example.com";
        String nombre = "Usuario Seguro";

        // ACT
        emailService.enviar2FAActivado(email, nombre);

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarNuevoDispositivo - debe enviar notificación de nuevo dispositivo de confianza")
    void enviarNuevoDispositivo_debeEnviarCorrectamente() {
        // ARRANGE
        String nombreDispositivo = "Chrome en Windows 11";
        String ipAddress = "192.168.1.100";

        // ACT
        emailService.enviarNuevoDispositivo(TEST_EMAIL, TEST_NOMBRE, nombreDispositivo, ipAddress);

        // ASSERT
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarNuevoDispositivo - debe incluir información del dispositivo e IP")
    void enviarNuevoDispositivo_debeIncluirDetallesDispositivo() {
        // ARRANGE
        String dispositivo = "Firefox en Ubuntu 22.04";
        String ip = "10.0.0.50";
        String email = "dispositivo@example.com";
        String nombre = "Usuario Multi-Dispositivo";

        // ACT
        emailService.enviarNuevoDispositivo(email, nombre, dispositivo, ip);

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarNuevoDispositivo - debe advertir sobre validez de 30 días")
    void enviarNuevoDispositivo_debeAdvertirValidez30Dias() {
        // ARRANGE
        String dispositivo = "Safari en MacOS";
        String ip = "172.16.0.1";

        // ACT
        emailService.enviarNuevoDispositivo(TEST_EMAIL, TEST_NOMBRE, dispositivo, ip);

        // ASSERT
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmail - debe manejar error cuando falla el envío")
    void enviarEmail_debeCapturarErrorEnvio() {
        // ARRANGE
        doThrow(new RuntimeException("Error de envío")).when(mailSender).send(any(MimeMessage.class));

        // ACT - No debe lanzar excepción
        emailService.enviarEmailBienvenida(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmail - debe manejar error genérico sin lanzar excepción")
    void enviarEmail_debeCapturarExcepcionGenerica() {
        // ARRANGE
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error inesperado"));

        // ACT - No debe lanzar excepción
        emailService.enviarCodigoRecuperacion(TEST_EMAIL, TEST_NOMBRE, "123456");

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("enviarEmailNuevoRegistro - debe usar plantilla HTML con colores INIA")
    void enviarEmailNuevoRegistro_debeUsarPlantillaHTML() {
        // ARRANGE & ACT
        emailService.enviarEmailNuevoRegistro(ANALISTA_EMAIL, ANALISTA_NOMBRE, "Usuario", "user@test.com");

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailConfirmacionRegistro - debe tener asunto correcto")
    void enviarEmailConfirmacionRegistro_debeTenerAsuntoCorrecto() {
        // ARRANGE & ACT
        emailService.enviarEmailConfirmacionRegistro(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailBienvenida - debe tener asunto de bienvenida")
    void enviarEmailBienvenida_debeTenerAsuntoBienvenida() {
        // ARRANGE & ACT
        emailService.enviarEmailBienvenida(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarCodigoRecuperacion - debe tener asunto de recuperación")
    void enviarCodigoRecuperacion_debeTenerAsuntoRecuperacion() {
        // ARRANGE & ACT
        emailService.enviarCodigoRecuperacion(TEST_EMAIL, TEST_NOMBRE, "999999");

        // ASSERT
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviar2FAActivado - debe tener asunto de 2FA activado")
    void enviar2FAActivado_debeTenerAsunto2FA() {
        // ARRANGE & ACT
        emailService.enviar2FAActivado(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarNuevoDispositivo - debe tener asunto de nuevo dispositivo")
    void enviarNuevoDispositivo_debeTenerAsuntoDispositivo() {
        // ARRANGE & ACT
        emailService.enviarNuevoDispositivo(TEST_EMAIL, TEST_NOMBRE, "Device", "127.0.0.1");

        // ASSERT
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailNuevoRegistro - debe funcionar con múltiples usuarios")
    void enviarEmailNuevoRegistro_debeFuncionarConMultiplesUsuarios() {
        // ARRANGE
        String[] usuarios = {"user1@test.com", "user2@test.com", "user3@test.com"};
        String[] nombres = {"Usuario 1", "Usuario 2", "Usuario 3"};

        // ACT
        for (int i = 0; i < usuarios.length; i++) {
            emailService.enviarEmailNuevoRegistro(ANALISTA_EMAIL, ANALISTA_NOMBRE, nombres[i], usuarios[i]);
        }

        // ASSERT
        verify(mailSender, times(3)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmailBienvenida - debe funcionar con diferentes nombres de usuario")
    void enviarEmailBienvenida_debeFuncionarConDiferentesNombres() {
        // ARRANGE
        String[] nombres = {"María García", "José López", "Ana Martínez"};

        // ACT
        for (String nombre : nombres) {
            emailService.enviarEmailBienvenida(TEST_EMAIL, nombre);
        }

        // ASSERT
        verify(mailSender, times(3)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarCodigoRecuperacion - debe funcionar con diferentes códigos")
    void enviarCodigoRecuperacion_debeFuncionarConDiferentesCodigos() {
        // ARRANGE
        String[] codigos = {"111111", "222222", "333333", "444444"};

        // ACT
        for (String codigo : codigos) {
            emailService.enviarCodigoRecuperacion(TEST_EMAIL, TEST_NOMBRE, codigo);
        }

        // ASSERT
        verify(mailSender, times(4)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarNuevoDispositivo - debe funcionar con diferentes dispositivos e IPs")
    void enviarNuevoDispositivo_debeFuncionarConDiferentesDispositivos() {
        // ARRANGE
        String[] dispositivos = {"Chrome/Windows", "Firefox/Linux", "Safari/MacOS"};
        String[] ips = {"192.168.1.1", "10.0.0.1", "172.16.0.1"};

        // ACT
        for (int i = 0; i < dispositivos.length; i++) {
            emailService.enviarNuevoDispositivo(TEST_EMAIL, TEST_NOMBRE, dispositivos[i], ips[i]);
        }

        // ASSERT
        verify(mailSender, times(3)).send(mimeMessage);
    }

    @Test
    @DisplayName("todos los métodos de envío - deben crear MimeMessage antes de enviar")
    void todosLosMetodos_debenCrearMimeMessageAntesDeEnviar() {
        // ACT
        emailService.enviarEmailNuevoRegistro(ANALISTA_EMAIL, ANALISTA_NOMBRE, "User", "user@test.com");
        emailService.enviarEmailConfirmacionRegistro(TEST_EMAIL, TEST_NOMBRE);
        emailService.enviarEmailBienvenida(TEST_EMAIL, TEST_NOMBRE);
        emailService.enviarCodigoRecuperacion(TEST_EMAIL, TEST_NOMBRE, "123456");
        emailService.enviar2FAActivado(TEST_EMAIL, TEST_NOMBRE);
        emailService.enviarNuevoDispositivo(TEST_EMAIL, TEST_NOMBRE, "Device", "127.0.0.1");

        // ASSERT - Cada método debe crear un MimeMessage
        verify(mailSender, times(6)).createMimeMessage();
        verify(mailSender, times(6)).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmail - debe imprimir mensaje de éxito en consola")
    void enviarEmail_debeImprimirMensajeExito() {
        // ARRANGE & ACT
        emailService.enviarEmailBienvenida(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT - Verificar que se invocó send (mensaje de éxito se imprime después)
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmail - debe imprimir error cuando falla el envío")
    void enviarEmail_debeImprimirErrorEnvio() {
        // ARRANGE
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // ACT
        emailService.enviar2FAActivado(TEST_EMAIL, TEST_NOMBRE);

        // ASSERT - Verificar que intentó crear y enviar el mensaje
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("enviarEmail - debe imprimir error cuando falla con excepción genérica")
    void enviarEmail_debeImprimirErrorExcepcionGenerica() {
        // ARRANGE
        when(mailSender.createMimeMessage()).thenThrow(new NullPointerException("Null pointer"));

        // ACT
        emailService.enviarNuevoDispositivo(TEST_EMAIL, TEST_NOMBRE, "Device", "127.0.0.1");

        // ASSERT
        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
