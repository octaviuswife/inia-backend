package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para RecoveryCodeService
 * 
 * Funcionalidades testeadas:
 * - Generación de códigos de recuperación aleatorios
 * - Hashing de códigos para almacenamiento seguro
 * - Verificación de códigos
 * - Validación de formato
 * - Expiración de códigos (10 minutos)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RecoveryCodeService (Códigos de Recuperación)")
class RecoveryCodeServiceTest {

    private RecoveryCodeService recoveryCodeService;

    @BeforeEach
    void setUp() {
        recoveryCodeService = new RecoveryCodeService();
    }

    @Test
    @DisplayName("Generar código - debe retornar código de 9 caracteres con guion")
    void generateRecoveryCode_debeRetornarFormatoCorrecto() {
        // ACT
        String code = recoveryCodeService.generateRecoveryCode();

        // ASSERT
        assertNotNull(code, "El código no debe ser nulo");
        assertEquals(9, code.length(), "El código debe tener 9 caracteres (8 + guion)");
        assertTrue(code.contains("-"), "El código debe contener un guion");
        assertEquals(4, code.indexOf("-"), "El guion debe estar en la posición 4");
    }

    @Test
    @DisplayName("Generar código - debe usar solo caracteres válidos")
    void generateRecoveryCode_debeUsarCaracteresValidos() {
        // ARRANGE
        String validChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        // ACT
        String code = recoveryCodeService.generateRecoveryCode();
        String codeWithoutDash = code.replace("-", "");

        // ASSERT
        for (char c : codeWithoutDash.toCharArray()) {
            assertTrue(validChars.indexOf(c) >= 0, 
                "El carácter '" + c + "' debe estar en el conjunto válido");
        }
    }

    @Test
    @DisplayName("Generar código - no debe contener caracteres confusos (I, O, 0, 1)")
    void generateRecoveryCode_noDebeContenerCaracteresConfusos() {
        // ACT
        for (int i = 0; i < 100; i++) {
            String code = recoveryCodeService.generateRecoveryCode();
            String codeWithoutDash = code.replace("-", "");

            // ASSERT
            assertFalse(codeWithoutDash.contains("I"), "No debe contener 'I'");
            assertFalse(codeWithoutDash.contains("O"), "No debe contener 'O'");
            assertFalse(codeWithoutDash.contains("0"), "No debe contener '0'");
            assertFalse(codeWithoutDash.contains("1"), "No debe contener '1'");
        }
    }

    @Test
    @DisplayName("Generar múltiples códigos - deben ser únicos")
    void generateRecoveryCode_multiplesLlamadas_debenSerUnicos() {
        // ACT
        String code1 = recoveryCodeService.generateRecoveryCode();
        String code2 = recoveryCodeService.generateRecoveryCode();
        String code3 = recoveryCodeService.generateRecoveryCode();

        // ASSERT
        assertNotEquals(code1, code2, "Los códigos deben ser diferentes");
        assertNotEquals(code1, code3, "Los códigos deben ser diferentes");
        assertNotEquals(code2, code3, "Los códigos deben ser diferentes");
    }

    @Test
    @DisplayName("Hashear código - debe retornar hash BCrypt")
    void hashCode_debeRetornarHashBCrypt() {
        // ARRANGE
        String plainCode = "AB3K-7M9P";

        // ACT
        String hashedCode = recoveryCodeService.hashCode(plainCode);

        // ASSERT
        assertNotNull(hashedCode, "El hash no debe ser nulo");
        assertTrue(hashedCode.startsWith("$2a$") || hashedCode.startsWith("$2b$"), 
            "Debe ser un hash BCrypt válido");
        assertNotEquals(plainCode, hashedCode, "El hash debe ser diferente al código original");
    }

    @Test
    @DisplayName("Hashear mismo código dos veces - debe generar hashes diferentes")
    void hashCode_mismoCodigoDosVeces_debeGenerarHashesDiferentes() {
        // ARRANGE
        String plainCode = "XY4Z-8N2K";

        // ACT
        String hash1 = recoveryCodeService.hashCode(plainCode);
        String hash2 = recoveryCodeService.hashCode(plainCode);

        // ASSERT
        assertNotEquals(hash1, hash2, 
            "BCrypt debe generar hashes diferentes por el salt aleatorio");
    }

    @Test
    @DisplayName("Hashear código con guion - debe normalizar correctamente")
    void hashCode_conGuion_debeNormalizar() {
        // ARRANGE
        String codeWithDash = "AB3K-7M9P";
        String codeWithoutDash = "AB3K7M9P";

        // ACT
        String hash1 = recoveryCodeService.hashCode(codeWithDash);
        
        // Verificar que se puede validar con o sin guion
        boolean matches = recoveryCodeService.verifyCode(codeWithoutDash, hash1);

        // ASSERT
        assertTrue(matches, "Debe normalizar el código removiendo guiones");
    }

    @Test
    @DisplayName("Hashear código vacío - debe lanzar excepción")
    void hashCode_codigoVacio_debeLanzarExcepcion() {
        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, 
            () -> recoveryCodeService.hashCode(""),
            "Debe lanzar excepción para código vacío");
    }

    @Test
    @DisplayName("Hashear código nulo - debe lanzar excepción")
    void hashCode_codigoNulo_debeLanzarExcepcion() {
        // ACT & ASSERT
        assertThrows(IllegalArgumentException.class, 
            () -> recoveryCodeService.hashCode(null),
            "Debe lanzar excepción para código nulo");
    }

    @Test
    @DisplayName("Verificar código correcto - debe retornar true")
    void verifyCode_codigoCorrecto_debeRetornarTrue() {
        // ARRANGE
        String plainCode = "MN5P-9Q2R";
        String hashedCode = recoveryCodeService.hashCode(plainCode);

        // ACT
        boolean isValid = recoveryCodeService.verifyCode(plainCode, hashedCode);

        // ASSERT
        assertTrue(isValid, "El código correcto debe ser verificado exitosamente");
    }

    @Test
    @DisplayName("Verificar código incorrecto - debe retornar false")
    void verifyCode_codigoIncorrecto_debeRetornarFalse() {
        // ARRANGE
        String correctCode = "AB3K-7M9P";
        String wrongCode = "XY4Z-8N2K";
        String hashedCode = recoveryCodeService.hashCode(correctCode);

        // ACT
        boolean isValid = recoveryCodeService.verifyCode(wrongCode, hashedCode);

        // ASSERT
        assertFalse(isValid, "Un código incorrecto debe fallar la verificación");
    }

    @Test
    @DisplayName("Verificar código sin guion - debe normalizar y validar")
    void verifyCode_sinGuion_debeNormalizarYValidar() {
        // ARRANGE
        String codeWithDash = "CD6E-4F2G";
        String codeWithoutDash = "CD6E4F2G";
        String hashedCode = recoveryCodeService.hashCode(codeWithDash);

        // ACT
        boolean isValid = recoveryCodeService.verifyCode(codeWithoutDash, hashedCode);

        // ASSERT
        assertTrue(isValid, "Debe validar código sin guion correctamente");
    }

    @Test
    @DisplayName("Verificar código en minúsculas - debe normalizar a mayúsculas")
    void verifyCode_enMinusculas_debeNormalizarAMayusculas() {
        // ARRANGE
        String upperCode = "AB3K-7M9P";
        String lowerCode = "ab3k-7m9p";
        String hashedCode = recoveryCodeService.hashCode(upperCode);

        // ACT
        boolean isValid = recoveryCodeService.verifyCode(lowerCode, hashedCode);

        // ASSERT
        assertTrue(isValid, "Debe normalizar a mayúsculas automáticamente");
    }

    @Test
    @DisplayName("Verificar código con espacios - debe limpiar espacios")
    void verifyCode_conEspacios_debeLimpiarEspacios() {
        // ARRANGE
        String code = "AB3K-7M9P";
        String codeWithSpaces = " AB3K-7M9P ";
        String hashedCode = recoveryCodeService.hashCode(code);

        // ACT
        boolean isValid = recoveryCodeService.verifyCode(codeWithSpaces, hashedCode);

        // ASSERT
        assertTrue(isValid, "Debe limpiar espacios al inicio/fin");
    }

    @Test
    @DisplayName("Verificar código nulo - debe retornar false")
    void verifyCode_codigoNulo_debeRetornarFalse() {
        // ARRANGE
        String hashedCode = recoveryCodeService.hashCode("AB3K-7M9P");

        // ACT
        boolean isValid = recoveryCodeService.verifyCode(null, hashedCode);

        // ASSERT
        assertFalse(isValid, "Un código nulo debe retornar false");
    }

    @Test
    @DisplayName("Verificar código con hash nulo - debe retornar false")
    void verifyCode_hashNulo_debeRetornarFalse() {
        // ACT
        boolean isValid = recoveryCodeService.verifyCode("AB3K-7M9P", null);

        // ASSERT
        assertFalse(isValid, "Un hash nulo debe retornar false");
    }

    @Test
    @DisplayName("Verificar código con longitud incorrecta - debe retornar false")
    void verifyCode_longitudIncorrecta_debeRetornarFalse() {
        // ARRANGE
        String hashedCode = recoveryCodeService.hashCode("AB3K-7M9P");

        // ACT
        boolean shortCode = recoveryCodeService.verifyCode("AB3-7M9P", hashedCode);
        boolean longCode = recoveryCodeService.verifyCode("AB3K-7M9PX", hashedCode);

        // ASSERT
        assertFalse(shortCode, "Un código corto debe retornar false");
        assertFalse(longCode, "Un código largo debe retornar false");
    }

    @Test
    @DisplayName("Validar formato de código válido - debe retornar true")
    void isValidFormat_codigoValido_debeRetornarTrue() {
        // ARRANGE
        String validCode = "AB3K-7M9P";

        // ACT
        boolean isValid = recoveryCodeService.isValidFormat(validCode);

        // ASSERT
        assertTrue(isValid, "Un código con formato válido debe retornar true");
    }

    @Test
    @DisplayName("Validar formato sin guion - debe retornar true")
    void isValidFormat_sinGuion_debeRetornarTrue() {
        // ARRANGE
        String codeWithoutDash = "AB3K7M9P";

        // ACT
        boolean isValid = recoveryCodeService.isValidFormat(codeWithoutDash);

        // ASSERT
        assertTrue(isValid, "Debe aceptar código sin guion");
    }

    @Test
    @DisplayName("Validar formato nulo - debe retornar false")
    void isValidFormat_nulo_debeRetornarFalse() {
        // ACT
        boolean isValid = recoveryCodeService.isValidFormat(null);

        // ASSERT
        assertFalse(isValid, "Un código nulo debe retornar false");
    }

    @Test
    @DisplayName("Validar formato con caracteres inválidos - debe retornar false")
    void isValidFormat_caracteresInvalidos_debeRetornarFalse() {
        // ACT
        boolean withI = recoveryCodeService.isValidFormat("ABIK-7M9P");
        boolean withO = recoveryCodeService.isValidFormat("ABOK-7M9P");
        boolean with0 = recoveryCodeService.isValidFormat("AB0K-7M9P");
        boolean with1 = recoveryCodeService.isValidFormat("AB1K-7M9P");

        // ASSERT
        assertFalse(withI, "No debe aceptar 'I'");
        assertFalse(withO, "No debe aceptar 'O'");
        assertFalse(with0, "No debe aceptar '0'");
        assertFalse(with1, "No debe aceptar '1'");
    }

    @Test
    @DisplayName("Validar formato con longitud incorrecta - debe retornar false")
    void isValidFormat_longitudIncorrecta_debeRetornarFalse() {
        // ACT
        boolean tooShort = recoveryCodeService.isValidFormat("AB3-7M9");
        boolean tooLong = recoveryCodeService.isValidFormat("AB3K-7M9PX");

        // ASSERT
        assertFalse(tooShort, "Un código corto debe retornar false");
        assertFalse(tooLong, "Un código largo debe retornar false");
    }

    @Test
    @DisplayName("Obtener tiempo de expiración - debe ser 10 minutos en el futuro")
    void getExpiryTime_debeSerDiezMinutosEnFuturo() {
        // ACT
        LocalDateTime expiryTime = recoveryCodeService.getExpiryTime();

        // ASSERT
        assertNotNull(expiryTime);
        assertTrue(expiryTime.isAfter(LocalDateTime.now()), 
            "El tiempo de expiración debe estar en el futuro");
        assertTrue(expiryTime.isBefore(LocalDateTime.now().plusMinutes(11)), 
            "El tiempo de expiración debe ser aproximadamente 10 minutos");
    }

    @Test
    @DisplayName("Verificar expiración - código no expirado debe retornar false")
    void isExpired_codigoNoExpirado_debeRetornarFalse() {
        // ARRANGE
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(5);

        // ACT
        boolean isExpired = recoveryCodeService.isExpired(futureTime);

        // ASSERT
        assertFalse(isExpired, "Un código con tiempo futuro no debe estar expirado");
    }

    @Test
    @DisplayName("Verificar expiración - código expirado debe retornar true")
    void isExpired_codigoExpirado_debeRetornarTrue() {
        // ARRANGE
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(5);

        // ACT
        boolean isExpired = recoveryCodeService.isExpired(pastTime);

        // ASSERT
        assertTrue(isExpired, "Un código con tiempo pasado debe estar expirado");
    }

    @Test
    @DisplayName("Verificar expiración - tiempo nulo debe retornar true")
    void isExpired_tiempoNulo_debeRetornarTrue() {
        // ACT
        boolean isExpired = recoveryCodeService.isExpired(null);

        // ASSERT
        assertTrue(isExpired, "Un tiempo nulo debe considerarse expirado");
    }

    @Test
    @DisplayName("Obtener minutos de expiración - debe retornar 10")
    void getExpiryMinutes_debeRetornarDiez() {
        // ACT
        int expiryMinutes = recoveryCodeService.getExpiryMinutes();

        // ASSERT
        assertEquals(10, expiryMinutes, "El tiempo de expiración debe ser 10 minutos");
    }

    @Test
    @DisplayName("Flujo completo - generar, hashear, verificar y expirar")
    void flujoCompleto_debeTrabajarCorrectamente() {
        // 1. Generar código
        String plainCode = recoveryCodeService.generateRecoveryCode();
        assertTrue(recoveryCodeService.isValidFormat(plainCode), "El código generado debe ser válido");

        // 2. Hashear código
        String hashedCode = recoveryCodeService.hashCode(plainCode);
        assertNotNull(hashedCode, "Debe generar hash correctamente");

        // 3. Verificar código correcto
        assertTrue(recoveryCodeService.verifyCode(plainCode, hashedCode), 
            "Debe verificar el código correcto");

        // 4. Verificar código incorrecto
        assertFalse(recoveryCodeService.verifyCode("WRONG-CODE", hashedCode), 
            "Debe rechazar código incorrecto");

        // 5. Verificar expiración
        LocalDateTime expiryTime = recoveryCodeService.getExpiryTime();
        assertFalse(recoveryCodeService.isExpired(expiryTime), 
            "El código recién creado no debe estar expirado");
    }
}
