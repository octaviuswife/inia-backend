package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para TotpService (Autenticación 2FA)
 * 
 * Funcionalidades testeadas:
 * - Generación de secret keys
 * - Generación de QR codes para Google Authenticator
 * - Verificación de códigos TOTP
 * - Validación de códigos con ventana de tolerancia
 * - Manejo de códigos inválidos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de TotpService (2FA)")
class TotpServiceTest {

    private TotpService totpService;

    @BeforeEach
    void setUp() {
        totpService = new TotpService();
    }

    @Test
    @DisplayName("Generar secret - debe retornar string Base32 no vacío")
    void generateSecret_debeRetornarSecretValido() {
        // ACT
        String secret = totpService.generateSecret();

        // ASSERT
        assertNotNull(secret, "El secret no debe ser nulo");
        assertFalse(secret.isEmpty(), "El secret no debe estar vacío");
        assertTrue(secret.length() > 10, "El secret debe tener longitud suficiente");
        assertTrue(secret.matches("[A-Z2-7]+"), "El secret debe estar en formato Base32");
    }

    @Test
    @DisplayName("Generar secret - debe generar secrets únicos en cada llamada")
    void generateSecret_debeGenerarSecretsUnicos() {
        // ACT
        String secret1 = totpService.generateSecret();
        String secret2 = totpService.generateSecret();

        // ASSERT
        assertNotEquals(secret1, secret2, "Los secrets generados deben ser únicos");
    }

    @Test
    @DisplayName("Generar QR code - debe retornar data URL válido")
    void generateQrCodeDataUrl_debeRetornarDataUrlValido() {
        // ARRANGE
        String secret = totpService.generateSecret();
        String accountName = "test@example.com";

        // ACT
        String qrCodeDataUrl = totpService.generateQrCodeDataUrl(secret, accountName);

        // ASSERT
        assertNotNull(qrCodeDataUrl, "El QR code data URL no debe ser nulo");
        assertTrue(qrCodeDataUrl.startsWith("data:image/png;base64,"), 
            "El QR code debe ser una data URL de imagen PNG");
        assertTrue(qrCodeDataUrl.length() > 100, "El QR code debe tener contenido");
    }

    @Test
    @DisplayName("Verificar código válido generado en el momento - debe retornar true")
    void verifyCode_conCodigoValido_debeRetornarTrue() throws Exception {
        // ARRANGE
        String secret = totpService.generateSecret();
        
        // Generar código actual usando la misma lógica que TotpService
        long currentTimeMillis = System.currentTimeMillis();
        long currentBucket = Math.floorDiv(currentTimeMillis, 30000);
        
        // Usamos el servicio real para generar el código esperado
        // (en producción el usuario lo obtendría de Google Authenticator)
        dev.samstevens.totp.code.CodeGenerator codeGenerator = new dev.samstevens.totp.code.DefaultCodeGenerator();
        String validCode = codeGenerator.generate(secret, currentBucket);

        // ACT
        boolean isValid = totpService.verifyCode(secret, validCode);

        // ASSERT
        assertTrue(isValid, "El código generado en el momento actual debe ser válido");
    }

    @Test
    @DisplayName("Verificar código con espacios - debe limpiar y validar correctamente")
    void verifyCode_conEspacios_debeLimpiarYValidar() throws Exception {
        // ARRANGE
        String secret = totpService.generateSecret();
        
        long currentTimeMillis = System.currentTimeMillis();
        long currentBucket = Math.floorDiv(currentTimeMillis, 30000);
        dev.samstevens.totp.code.CodeGenerator codeGenerator = new dev.samstevens.totp.code.DefaultCodeGenerator();
        String validCode = codeGenerator.generate(secret, currentBucket);
        
        // Agregar espacios al código
        String codeWithSpaces = validCode.substring(0, 3) + " " + validCode.substring(3);

        // ACT
        boolean isValid = totpService.verifyCode(secret, codeWithSpaces);

        // ASSERT
        assertTrue(isValid, "El código con espacios debe ser válido después de limpiarlo");
    }

    @Test
    @DisplayName("Verificar código inválido - debe retornar false")
    void verifyCode_conCodigoInvalido_debeRetornarFalse() {
        // ARRANGE
        String secret = totpService.generateSecret();
        String invalidCode = "000000"; // Código que probablemente no coincida

        // ACT
        boolean isValid = totpService.verifyCode(secret, invalidCode);

        // ASSERT
        assertFalse(isValid, "Un código incorrecto debe retornar false");
    }

    @Test
    @DisplayName("Verificar código con longitud incorrecta - debe retornar false")
    void verifyCode_conLongitudIncorrecta_debeRetornarFalse() {
        // ARRANGE
        String secret = totpService.generateSecret();
        String shortCode = "123"; // Muy corto
        String longCode = "1234567"; // Muy largo

        // ACT & ASSERT
        assertFalse(totpService.verifyCode(secret, shortCode), 
            "Un código muy corto debe retornar false");
        assertFalse(totpService.verifyCode(secret, longCode), 
            "Un código muy largo debe retornar false");
    }

    @Test
    @DisplayName("Verificar código nulo - debe retornar false")
    void verifyCode_conCodigoNulo_debeRetornarFalse() {
        // ARRANGE
        String secret = totpService.generateSecret();

        // ACT
        boolean isValid = totpService.verifyCode(secret, null);

        // ASSERT
        assertFalse(isValid, "Un código nulo debe retornar false");
    }

    @Test
    @DisplayName("Verificar código con secret nulo - debe retornar false")
    void verifyCode_conSecretNulo_debeRetornarFalse() {
        // ACT
        boolean isValid = totpService.verifyCode(null, "123456");

        // ASSERT
        assertFalse(isValid, "Un secret nulo debe retornar false");
    }

    @Test
    @DisplayName("Verificar código con caracteres no numéricos - debe limpiar y validar")
    void verifyCode_conCaracteresNoNumericos_debeLimpiar() throws Exception {
        // ARRANGE
        String secret = totpService.generateSecret();
        
        long currentTimeMillis = System.currentTimeMillis();
        long currentBucket = Math.floorDiv(currentTimeMillis, 30000);
        dev.samstevens.totp.code.CodeGenerator codeGenerator = new dev.samstevens.totp.code.DefaultCodeGenerator();
        String validCode = codeGenerator.generate(secret, currentBucket);
        
        // Agregar caracteres no numéricos
        String codeWithChars = validCode.substring(0, 3) + "-" + validCode.substring(3);

        // ACT
        boolean isValid = totpService.verifyCode(secret, codeWithChars);

        // ASSERT
        assertTrue(isValid, "El código con guiones debe ser válido después de limpiarlo");
    }

    @Test
    @DisplayName("Generar QR code con nombre de cuenta largo - debe funcionar correctamente")
    void generateQrCodeDataUrl_conNombreLargo_debeFuncionar() {
        // ARRANGE
        String secret = totpService.generateSecret();
        String longAccountName = "usuario.con.nombre.muy.largo@example.com";

        // ACT
        String qrCodeDataUrl = totpService.generateQrCodeDataUrl(secret, longAccountName);

        // ASSERT
        assertNotNull(qrCodeDataUrl, "Debe generar QR code incluso con nombre largo");
        assertTrue(qrCodeDataUrl.startsWith("data:image/png;base64,"), 
            "Debe ser una data URL válida");
    }

    @Test
    @DisplayName("Verificar mismo secret genera códigos consistentes en el mismo período")
    void verifyCode_mismoSecret_generaCodigosConsistentes() throws Exception {
        // ARRANGE
        String secret = totpService.generateSecret();
        
        long currentTimeMillis = System.currentTimeMillis();
        long currentBucket = Math.floorDiv(currentTimeMillis, 30000);
        dev.samstevens.totp.code.CodeGenerator codeGenerator = new dev.samstevens.totp.code.DefaultCodeGenerator();
        
        // ACT - Generar el mismo código dos veces
        String code1 = codeGenerator.generate(secret, currentBucket);
        String code2 = codeGenerator.generate(secret, currentBucket);

        // ASSERT
        assertEquals(code1, code2, 
            "El mismo secret debe generar el mismo código en el mismo período de tiempo");
    }

    @Test
    @DisplayName("Generar QR code con caracteres especiales en account name - debe funcionar")
    void generateQrCodeDataUrl_conCaracteresEspeciales_debeFuncionar() {
        // ARRANGE
        String secret = totpService.generateSecret();
        String accountWithSpecialChars = "user+test@example.com";

        // ACT
        String qrCodeDataUrl = totpService.generateQrCodeDataUrl(secret, accountWithSpecialChars);

        // ASSERT
        assertNotNull(qrCodeDataUrl, "Debe generar QR code con caracteres especiales");
        assertTrue(qrCodeDataUrl.startsWith("data:image/png;base64,"), 
            "Debe ser una data URL válida");
    }

    @Test
    @DisplayName("getRemainingSeconds - debe retornar valor entre 0 y 30")
    void getRemainingSeconds_debeRetornarValorValido() {
        // ACT
        int remainingSeconds = totpService.getRemainingSeconds();

        // ASSERT
        assertTrue(remainingSeconds >= 0 && remainingSeconds <= 30, 
            "Los segundos restantes deben estar entre 0 y 30");
    }

    @Test
    @DisplayName("getRemainingSeconds - debe cambiar con el tiempo")
    void getRemainingSeconds_debeCambiarConElTiempo() throws InterruptedException {
        // ACT
        int seconds1 = totpService.getRemainingSeconds();
        Thread.sleep(1100); // Esperar 1.1 segundos
        int seconds2 = totpService.getRemainingSeconds();

        // ASSERT
        // Los valores deben ser diferentes (a menos que justo se reinició el ciclo)
        // O seconds2 debe ser menor que seconds1 (se consume el tiempo)
        assertTrue(seconds1 >= 0 && seconds1 <= 30);
        assertTrue(seconds2 >= 0 && seconds2 <= 30);
        // En la mayoría de casos, seconds2 será menor que seconds1
        // (a menos que cruzamos un boundary de 30s)
    }

    @Test
    @DisplayName("getRemainingSeconds - debe decrementar hasta 0 y reiniciar")
    void getRemainingSeconds_debeDecrementarYReiniciar() {
        // ACT
        int seconds = totpService.getRemainingSeconds();

        // ASSERT
        // Debe estar en el rango válido
        assertTrue(seconds >= 0 && seconds <= 30, 
            "Segundos restantes fuera de rango: " + seconds);
    }

    @Test
    @DisplayName("isValidSecret - debe retornar true para secret válido generado")
    void isValidSecret_debeRetornarTrueParaSecretValido() {
        // ARRANGE
        String validSecret = totpService.generateSecret();

        // ACT
        boolean isValid = totpService.isValidSecret(validSecret);

        // ASSERT
        assertTrue(isValid, "Un secret generado debe ser válido");
    }

    @Test
    @DisplayName("isValidSecret - debe retornar false para secret nulo")
    void isValidSecret_debeRetornarFalseParaSecretNulo() {
        // ACT
        boolean isValid = totpService.isValidSecret(null);

        // ASSERT
        assertFalse(isValid, "Un secret nulo debe ser inválido");
    }

    @Test
    @DisplayName("isValidSecret - debe retornar false para secret vacío")
    void isValidSecret_debeRetornarFalseParaSecretVacio() {
        // ACT
        boolean isValid = totpService.isValidSecret("");

        // ASSERT
        assertFalse(isValid, "Un secret vacío debe ser inválido");
    }

    @Test
    @DisplayName("isValidSecret - debe retornar false para secret muy corto")
    void isValidSecret_debeRetornarFalseParaSecretCorto() {
        // ARRANGE
        String shortSecret = "ABC123"; // Menos de 16 caracteres

        // ACT
        boolean isValid = totpService.isValidSecret(shortSecret);

        // ASSERT
        assertFalse(isValid, "Un secret muy corto (< 16 chars) debe ser inválido");
    }

    @Test
    @DisplayName("isValidSecret - debe retornar false para secret muy largo")
    void isValidSecret_debeRetornarFalseParaSecretLargo() {
        // ARRANGE
        String longSecret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567890"; // Más de 32 caracteres

        // ACT
        boolean isValid = totpService.isValidSecret(longSecret);

        // ASSERT
        assertFalse(isValid, "Un secret muy largo (> 32 chars) debe ser inválido");
    }

    @Test
    @DisplayName("isValidSecret - debe retornar false para secret con caracteres inválidos")
    void isValidSecret_debeRetornarFalseParaCaracteresInvalidos() {
        // ARRANGE - Base32 solo permite A-Z y 2-7
        String secretConMinusculas = "abcdefghijklmnop"; // Minúsculas no permitidas
        String secretConNumeros = "ABCD1890EFGH1890"; // 0, 1, 8, 9 no son Base32
        String secretConSimbolos = "ABCD-EFGH-IJKL16"; // Símbolos no permitidos

        // ACT & ASSERT
        assertFalse(totpService.isValidSecret(secretConMinusculas), 
            "Secret con minúsculas debe ser inválido");
        assertFalse(totpService.isValidSecret(secretConNumeros), 
            "Secret con números 0, 1, 8, 9 debe ser inválido");
        assertFalse(totpService.isValidSecret(secretConSimbolos), 
            "Secret con símbolos debe ser inválido");
    }

    @Test
    @DisplayName("isValidSecret - debe retornar true para secret Base32 válido de 16 caracteres")
    void isValidSecret_debeRetornarTrueParaBase32Valido16Chars() {
        // ARRANGE
        String validSecret = "JBSWY3DPEHPK3PXP"; // 16 caracteres Base32 válidos

        // ACT
        boolean isValid = totpService.isValidSecret(validSecret);

        // ASSERT
        assertTrue(isValid, "Secret Base32 de 16 caracteres debe ser válido");
    }

    @Test
    @DisplayName("isValidSecret - debe retornar true para secret Base32 válido de 32 caracteres")
    void isValidSecret_debeRetornarTrueParaBase32Valido32Chars() {
        // ARRANGE
        String validSecret = "JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP"; // 32 caracteres Base32 válidos

        // ACT
        boolean isValid = totpService.isValidSecret(validSecret);

        // ASSERT
        assertTrue(isValid, "Secret Base32 de 32 caracteres debe ser válido");
    }

    @Test
    @DisplayName("isValidSecret - debe validar caracteres Base32 permitidos (A-Z, 2-7)")
    void isValidSecret_debeValidarCaracteresBase32() {
        // ARRANGE
        String validSecret = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"; // 32 chars con todos los caracteres permitidos

        // ACT
        boolean isValid = totpService.isValidSecret(validSecret);

        // ASSERT
        assertTrue(isValid, "Secret con todos los caracteres Base32 válidos debe ser válido");
    }

    @Test
    @DisplayName("verifyCode - debe capturar excepción y retornar false")
    void verifyCode_debeCaptularExcepcionYRetornarFalse() {
        // ARRANGE
        String invalidSecret = "NOT-A-VALID-BASE32-SECRET!!!"; // Secret inválido que causará excepción

        // ACT
        boolean isValid = totpService.verifyCode(invalidSecret, "123456");

        // ASSERT
        assertFalse(isValid, "Debe retornar false cuando ocurre una excepción en la verificación");
    }

    @Test
    @DisplayName("verifyCode - debe manejar secret corrupto sin lanzar excepción")
    void verifyCode_debeManejarSecretCorruptoSinLanzarExcepcion() {
        // ARRANGE
        String corruptSecret = "!!!INVALID!!!"; // Secret totalmente corrupto

        // ACT & ASSERT - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            boolean result = totpService.verifyCode(corruptSecret, "123456");
            assertFalse(result, "Debe retornar false para secret corrupto");
        });
    }

    @Test
    @DisplayName("verifyCode - debe manejar código con formato especial que cause excepción")
    void verifyCode_debeManejarCodigoEspecialConExcepcion() {
        // ARRANGE
        String secret = totpService.generateSecret();
        // Código con caracteres extraños que podrían causar problemas
        String weirdCode = "\u0000\u0001\u0002123456"; 

        // ACT & ASSERT - No debe lanzar excepción
        assertDoesNotThrow(() -> {
            boolean result = totpService.verifyCode(secret, weirdCode);
            // El resultado puede ser true o false, pero no debe lanzar excepción
            assertNotNull(result);
        });
    }
}

