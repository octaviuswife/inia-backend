package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.BackupCode;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.BackupCodeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de BackupCodeService")
class BackupCodeServiceTest {

    @Mock
    private BackupCodeRepository backupCodeRepository;

    @InjectMocks
    private BackupCodeService backupCodeService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private Integer usuarioId = 1;

    @Test
    @DisplayName("Generar códigos de respaldo - debe generar 10 códigos")
    void generateBackupCodes_debeGenerar10Codigos() {
        doNothing().when(backupCodeRepository).deleteAllByUsuarioId(anyInt());
        when(backupCodeRepository.save(any(BackupCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> codigos = backupCodeService.generateBackupCodes(usuarioId);

        assertNotNull(codigos);
        assertEquals(10, codigos.size());
        verify(backupCodeRepository, times(1)).deleteAllByUsuarioId(usuarioId);
        verify(backupCodeRepository, times(10)).save(any(BackupCode.class));
    }

    @Test
    @DisplayName("Generar códigos de respaldo - códigos deben tener formato correcto")
    void generateBackupCodes_debenTenerFormatoCorrecto() {
        doNothing().when(backupCodeRepository).deleteAllByUsuarioId(anyInt());
        when(backupCodeRepository.save(any(BackupCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> codigos = backupCodeService.generateBackupCodes(usuarioId);

        for (String codigo : codigos) {
            assertTrue(codigo.matches("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$"),
                "Código debe tener formato XXXX-XXXX-XXXX: " + codigo);
        }
    }

    @Test
    @DisplayName("Verificar código válido - debe retornar true y marcarlo como usado")
    void verifyAndUseBackupCode_codigoValido_debeRetornarTrue() {
        String codigoPlano = "ABCD-EFGH-JKLM";
        String codigoNormalizado = "ABCDEFGHJKLM";
        
        BackupCode backupCode = new BackupCode();
        backupCode.setId(1L);
        backupCode.setUsuarioId(usuarioId);
        backupCode.setCodeHash(passwordEncoder.encode(codigoNormalizado));
        backupCode.setUsed(false);

        List<BackupCode> codigosDisponibles = new ArrayList<>();
        codigosDisponibles.add(backupCode);

        when(backupCodeRepository.findByUsuarioIdAndUsedFalse(usuarioId)).thenReturn(codigosDisponibles);
        when(backupCodeRepository.save(any(BackupCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(backupCodeRepository.countByUsuarioIdAndUsedFalse(usuarioId)).thenReturn(9L);

        boolean resultado = backupCodeService.verifyAndUseBackupCode(usuarioId, codigoPlano);

        assertTrue(resultado);
        verify(backupCodeRepository, times(1)).save(backupCode);
        assertTrue(backupCode.getUsed());
    }

    @Test
    @DisplayName("Verificar código inválido - debe retornar false")
    void verifyAndUseBackupCode_codigoInvalido_debeRetornarFalse() {
        String codigoIncorrecto = "XXXX-XXXX-XXXX";
        
        BackupCode backupCode = new BackupCode();
        backupCode.setId(1L);
        backupCode.setUsuarioId(usuarioId);
        backupCode.setCodeHash(passwordEncoder.encode("ABCDEFGHJKLM"));
        backupCode.setUsed(false);

        List<BackupCode> codigosDisponibles = new ArrayList<>();
        codigosDisponibles.add(backupCode);

        when(backupCodeRepository.findByUsuarioIdAndUsedFalse(usuarioId)).thenReturn(codigosDisponibles);

        boolean resultado = backupCodeService.verifyAndUseBackupCode(usuarioId, codigoIncorrecto);

        assertFalse(resultado);
        verify(backupCodeRepository, never()).save(any(BackupCode.class));
    }

    @Test
    @DisplayName("Verificar código vacío - debe retornar false")
    void verifyAndUseBackupCode_codigoVacio_debeRetornarFalse() {
        boolean resultado = backupCodeService.verifyAndUseBackupCode(usuarioId, "");

        assertFalse(resultado);
        verify(backupCodeRepository, never()).findByUsuarioIdAndUsedFalse(anyInt());
    }

    @Test
    @DisplayName("Verificar código con longitud incorrecta - debe retornar false")
    void verifyAndUseBackupCode_longitudIncorrecta_debeRetornarFalse() {
        boolean resultado = backupCodeService.verifyAndUseBackupCode(usuarioId, "ABC");

        assertFalse(resultado);
        verify(backupCodeRepository, never()).save(any(BackupCode.class));
    }

    @Test
    @DisplayName("Obtener cantidad de códigos disponibles - debe retornar conteo correcto")
    void getAvailableCodesCount_debeRetornarConteoCorrecto() {
        when(backupCodeRepository.countByUsuarioIdAndUsedFalse(usuarioId)).thenReturn(5L);

        long resultado = backupCodeService.getAvailableCodesCount(usuarioId);

        assertEquals(5L, resultado);
        verify(backupCodeRepository, times(1)).countByUsuarioIdAndUsedFalse(usuarioId);
    }

    @Test
    @DisplayName("Verificar si tiene códigos disponibles - debe retornar true")
    void hasAvailableCodes_conCodigosDisponibles_debeRetornarTrue() {
        when(backupCodeRepository.countByUsuarioIdAndUsedFalse(usuarioId)).thenReturn(3L);

        boolean resultado = backupCodeService.hasAvailableCodes(usuarioId);

        assertTrue(resultado);
    }

    @Test
    @DisplayName("Verificar si tiene códigos disponibles - debe retornar false")
    void hasAvailableCodes_sinCodigosDisponibles_debeRetornarFalse() {
        when(backupCodeRepository.countByUsuarioIdAndUsedFalse(usuarioId)).thenReturn(0L);

        boolean resultado = backupCodeService.hasAvailableCodes(usuarioId);

        assertFalse(resultado);
    }

    @Test
    @DisplayName("Regenerar códigos - debe eliminar anteriores y generar nuevos")
    void regenerateBackupCodes_debeEliminarAnterioresYGenerarNuevos() {
        doNothing().when(backupCodeRepository).deleteAllByUsuarioId(anyInt());
        when(backupCodeRepository.save(any(BackupCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> codigos = backupCodeService.regenerateBackupCodes(usuarioId);

        assertNotNull(codigos);
        assertEquals(10, codigos.size());
        verify(backupCodeRepository, times(1)).deleteAllByUsuarioId(usuarioId);
        verify(backupCodeRepository, times(10)).save(any(BackupCode.class));
    }

    @Test
    @DisplayName("Eliminar todos los códigos de usuario - debe llamar al repositorio")
    void deleteAllUserCodes_debeEliminarTodos() {
        doNothing().when(backupCodeRepository).deleteAllByUsuarioId(usuarioId);

        backupCodeService.deleteAllUserCodes(usuarioId);

        verify(backupCodeRepository, times(1)).deleteAllByUsuarioId(usuarioId);
    }
}
