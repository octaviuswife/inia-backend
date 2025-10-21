
package utec.proyectofinal.Proyecto.Final.UTEC.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de seguridad web y CORS
 * 
 * CORS configurado para soportar:
 * - Múltiples orígenes (local y ngrok)
 * - Credenciales (cookies, Authorization headers)
 * - Preflight requests (OPTIONS)
 * 
 * @author Usuario
 */
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    @Value("${cors.allowed.origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new FiltroJWTAutorizacion(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/configuration/**").permitAll()

                        /*
                        // LECTURA - Todos los roles autenticados pueden ver
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "ANALISTA", "OBSERVADOR")

                        // CREACIÓN Y EDICIÓN - Solo ADMIN y ANALISTA
                        .requestMatchers(HttpMethod.POST, "/api/germinacion/**").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(HttpMethod.POST, "/api/tetrazolio/**").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(HttpMethod.POST, "/api/pureza/**").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/germinacion/**").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/tetrazolio/**").hasAnyRole("ADMIN", "ANALISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/pureza/**").hasAnyRole("ADMIN", "ANALISTA")

                        // ELIMINACIÓN - Solo ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                        // GESTIÓN DE USUARIOS - Solo ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        */
                        // DESARROLLO: Cambiar a authenticated() para que funcione JWT
                        .anyRequest().authenticated());




        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración CORS global para todas las rutas /api/**
     * 
     * IMPORTANTE: Cuando se usa credentials: 'include' en el frontend,
     * NO se puede usar allowedOrigins("*"). Se deben especificar los
     * orígenes exactos en la propiedad cors.allowed.origins
     * 
     * Para ngrok, actualizar la propiedad con tu URL actual:
     * cors.allowed.origins=http://localhost:3000,https://tu-url.ngrok-free.app
     */
    @Bean
    public WebMvcConfigurer configurarCorsGlobal() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = allowedOrigins.split(",");
                
                registry.addMapping("/api/**")
                        .allowedOrigins(origins) // Orígenes específicos desde properties
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true) // Necesario para enviar cookies y Authorization
                        .maxAge(3600); // Cache preflight por 1 hora
            }
        };
    }

}
