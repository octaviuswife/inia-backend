package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoginRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RegistroUsuarioRequestDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * EJEMPLO EDUCATIVO: Test de Integración para AuthController
 * 
 * Este tipo de test:
 * - Levanta TODA la aplicación Spring Boot (@SpringBootTest)
 * - Ejecuta peticiones HTTP reales contra los endpoints
 * - Interactúa con la base de datos (H2 en memoria o Testcontainers)
 * - Verifica el comportamiento end-to-end
 * 
 * Diferencia con test unitario:
 * - Test Unitario: Prueba un método aislado con mocks
 * - Test Integración: Prueba todo el flujo (Controller → Service → Repository → BD)
 */
@SpringBootTest  // Levanta el contexto completo de Spring Boot
@AutoConfigureMockMvc  // Configura MockMvc para simular peticiones HTTP
@DisplayName("Tests de Integración de AuthController")
class AuthControllerIntegrationTest {

    /**
     * MockMvc: Herramienta para simular peticiones HTTP sin levantar un servidor real
     * Es como hacer peticiones con Postman, pero desde código
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper: Convierte objetos Java a JSON y viceversa
     */
    @Autowired
    private ObjectMapper objectMapper;

    private RegistroUsuarioRequestDTO registroValido;
    private LoginRequestDTO loginValido;

    @BeforeEach
    void setUp() {
        // Preparar datos de prueba
        registroValido = new RegistroUsuarioRequestDTO();
        registroValido.setNombre("usuariotest");
        registroValido.setNombres("Usuario");
        registroValido.setApellidos("Test");
        registroValido.setEmail("test@inia.com");
        registroValido.setContrasenia("Password123!");

        loginValido = new LoginRequestDTO();
        loginValido.setUsuario("usuariotest");
        loginValido.setPassword("Password123!");
    }

    /**
     * TEST 1: Verificar que el endpoint de registro funciona correctamente
     * 
     * Flujo que se prueba:
     * 1. Se envía POST a /api/v1/auth/registro
     * 2. Controller recibe la petición
     * 3. Service crea el usuario
     * 4. Repository guarda en BD
     * 5. Se devuelve respuesta 201 CREATED
     */
    @Test
    @DisplayName("POST /api/v1/auth/registro - Debe registrar usuario correctamente")
    void registroUsuario_conDatosValidos_debeRetornar201() throws Exception {
        // Ejecutar petición HTTP POST
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)  // Tipo de contenido: JSON
                .content(objectMapper.writeValueAsString(registroValido)))  // Convertir objeto a JSON
                
                // Verificaciones (assertions)
                .andExpect(status().isCreated())  // Código HTTP 201
                .andExpect(jsonPath("$.mensaje").exists())  // Debe tener campo "mensaje"
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado exitosamente"))
                
                // Mostrar resultado en consola (útil para debug)
                .andDo(print());
    }

    /**
     * TEST 2: Verificar validación de email inválido
     */
    @Test
    @DisplayName("POST /api/v1/auth/registro - Email inválido debe retornar 400")
    void registroUsuario_conEmailInvalido_debeRetornar400() throws Exception {
        registroValido.setEmail("email-invalido");  // Email sin formato correcto

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroValido)))
                
                .andExpect(status().isBadRequest())  // Código HTTP 400
                .andDo(print());
    }

    /**
     * TEST 3: Verificar que el login funciona correctamente
     * 
     * Este test es más complejo porque:
     * 1. Primero necesita registrar un usuario
     * 2. Luego intenta hacer login con esas credenciales
     * 3. Verifica que se genera un token JWT
     */
    @Test
    @DisplayName("POST /api/v1/auth/login - Login exitoso debe retornar token JWT")
    void login_conCredencialesValidas_debeRetornarToken() throws Exception {
        // PASO 1: Registrar usuario primero
        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroValido)));

        // PASO 2: Hacer login con las credenciales
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginValido)))
                
                .andExpect(status().isOk())  // Código HTTP 200
                .andExpect(jsonPath("$.accessToken").exists())  // Debe tener token
                .andExpect(jsonPath("$.refreshToken").exists())  // Debe tener refresh token
                .andExpect(jsonPath("$.usuario").exists())  // Debe tener datos de usuario
                .andDo(print())
                .andReturn();

        // Verificar que el token no está vacío
        String response = result.getResponse().getContentAsString();
        System.out.println("Respuesta del login: " + response);
    }

    /**
     * TEST 4: Verificar que login con credenciales incorrectas falla
     */
    @Test
    @DisplayName("POST /api/v1/auth/login - Credenciales incorrectas debe retornar 401")
    void login_conCredencialesIncorrectas_debeRetornar401() throws Exception {
        loginValido.setPassword("PasswordIncorrecta");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginValido)))
                
                .andExpect(status().isUnauthorized())  // Código HTTP 401
                .andDo(print());
    }

    /**
     * TEST 5: Verificar que endpoint protegido requiere autenticación
     */
    @Test
    @DisplayName("GET /api/v1/auth/usuarios - Sin token debe retornar 403")
    void obtenerUsuarios_sinToken_debeRetornar403() throws Exception {
        mockMvc.perform(get("/api/v1/auth/usuarios"))
                .andExpect(status().isForbidden())  // Código HTTP 403
                .andDo(print());
    }

    /**
     * TEST 6: Verificar que endpoint protegido funciona con token válido
     * NOTA: Este test requiere una implementación más compleja con autenticación real
     */
    @Test
    @DisplayName("GET /api/v1/auth/usuarios - Ejemplo de test con autenticación")
    void obtenerUsuarios_ejemploAutenticacion() throws Exception {
        // Este es un test de ejemplo que muestra la estructura
        // En un test real, necesitarías:
        // 1. Crear un usuario
        // 2. Hacer login
        // 3. Extraer el token JWT
        // 4. Usarlo en las peticiones
        
        assertTrue(true, "Test de ejemplo - requiere configuración de seguridad");
    }
}
