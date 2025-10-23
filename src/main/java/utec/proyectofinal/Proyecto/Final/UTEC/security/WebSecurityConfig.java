package utec.proyectofinal.Proyecto.Final.UTEC.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @author Usuario
 */
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // IMPORTANTE: CORS debe ir primero
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Deshabilitar CSRF (necesario para APIs REST con JWT)
                .csrf(csrf -> csrf.disable())
                
                // Agregar filtro JWT
                .addFilterBefore(new FiltroJWTAutorizacion(), UsernamePasswordAuthenticationFilter.class)
                
                // Configurar autorización
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/configuration/**").permitAll()
                        
                        // Resto de endpoints requieren autenticación
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir localhost y cualquier subdominio de ngrok
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "https://*.ngrok-free.app",  // Cambiado de ngrok-free.dev
            "https://*.ngrok.io",
            "https://*.ngrok.app"          // Agregado dominio adicional
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Permitir todos los headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Headers expuestos al cliente
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Set-Cookie", 
            "Content-Type",
            "X-Total-Count"
        ));
        
        // CRÍTICO: permite cookies y credenciales
        configuration.setAllowCredentials(true);
        
        // Cache de preflight requests (1 hora)
        configuration.setMaxAge(3600L);
        
        // Registrar configuración CORS
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // IMPORTANTE: Aplicar a TODAS las rutas (no solo /api/**)
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}