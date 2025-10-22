package utec.proyectofinal.Proyecto.Final.UTEC.configurations;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// @Component - DESHABILITADO: Ahora usamos la configuración nativa de Spring Security en WebSecurityConfig
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // ESTE FILTRO ESTÁ DESHABILITADO
        // La configuración CORS ahora se maneja en WebSecurityConfig.corsConfigurationSource()
        
        filterChain.doFilter(request, response);
    }
}