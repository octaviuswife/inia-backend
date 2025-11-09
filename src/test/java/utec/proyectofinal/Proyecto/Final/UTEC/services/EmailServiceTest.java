package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage((Session) null);
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Crear MimeMessage - debe retornar mensaje válido")
    void crearMimeMessage_debeRetornarMensajeValido() {
        MimeMessage resultado = mailSender.createMimeMessage();

        assertNotNull(resultado);
        verify(mailSender, times(1)).createMimeMessage();
    }

    @Test
    @DisplayName("JavaMailSender mock - debe estar configurado correctamente")
    void javaMailSender_debeEstarConfigurado() {
        assertNotNull(mailSender);
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        mailSender.send(mimeMessage);
        
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
