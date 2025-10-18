
package utec.proyectofinal.Proyecto.Final.UTEC.security;

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
 * @author Usuario
 */
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

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

    @Bean
    public WebMvcConfigurer configurarCorsGlobal() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*") // Para desarrollo, despues cambiar a dominios específicos
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
                        // .allowCredentials(true); // Comentado para desarrollo
            }
        };
    }

}
