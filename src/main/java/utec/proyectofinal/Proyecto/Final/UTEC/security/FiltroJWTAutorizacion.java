package utec.proyectofinal.Proyecto.Final.UTEC.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FiltroJWTAutorizacion extends OncePerRequestFilter {

    private final String CLAVE = "@Z9@vQ3!pL8#wX7^tR2&nG6*yM4$eB1(dF0)sH5%kJ3&uY8*rE4#wQ1@zX6^nM9$";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(CLAVE.getBytes(StandardCharsets.UTF_8));
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    @Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
        throws ServletException, IOException {
    
    // CRÍTICO: Ignorar peticiones OPTIONS (preflight CORS)
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        filterChain.doFilter(request, response);
        return;
    }
    
    try {
        String token = extraerToken(request);
        
        if (token != null && !token.isEmpty()) {
            Claims claims = validarToken(token);
            
            if (claims != null && claims.get("authorities") != null) {
                crearAutenticacion(claims);
            } else {
                SecurityContextHolder.clearContext();
            }
        } else {
            SecurityContextHolder.clearContext();
        }
        
    } catch (ExpiredJwtException ex) {
        System.err.println("Token JWT expirado: " + ex.getMessage());
        SecurityContextHolder.clearContext();
    } catch (UnsupportedJwtException | MalformedJwtException ex) {
        System.err.println("Token JWT inválido: " + ex.getMessage());
        SecurityContextHolder.clearContext();
    } catch (Exception ex) {
        System.err.println("Error inesperado en JWT: " + ex.getMessage());
        SecurityContextHolder.clearContext();
    }
    
    // IMPORTANTE: Siempre continuar la cadena de filtros
    filterChain.doFilter(request, response);
}

    private void crearAutenticacion(Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> autorizaciones = (List<String>) claims.get("authorities");
        
        // Debug: Ver qué roles vienen en el JWT
        System.out.println("🔑 [FiltroJWT] Usuario del token: " + claims.getSubject());
        System.out.println("🔑 [FiltroJWT] Roles en JWT: " + autorizaciones);
        
        // Agregar prefijo ROLE_ para que funcione con hasRole()
        List<SimpleGrantedAuthority> authorities = autorizaciones.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        
        System.out.println("🔑 [FiltroJWT] Authorities con prefijo: " + authorities);
        
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * Extrae el token JWT desde cookies (preferible) o desde el header Authorization (fallback).
     */
    private String extraerToken(HttpServletRequest request) {
        System.out.println("🔍 [FiltroJWT] Extrayendo token de request: " + request.getRequestURI());
        
        // 1) Intentar obtener desde cookie HttpOnly (método seguro)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("🍪 [FiltroJWT] Cookies encontradas: " + cookies.length);
            for (Cookie cookie : cookies) {
                System.out.println("   - Cookie: " + cookie.getName() + " = " + (cookie.getValue().length() > 20 ? cookie.getValue().substring(0, 20) + "..." : cookie.getValue()));
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    System.out.println("✅ [FiltroJWT] Token encontrado en cookie accessToken");
                    return cookie.getValue();
                }
            }
            System.out.println("⚠️ [FiltroJWT] No se encontró cookie accessToken");
        } else {
            System.out.println("⚠️ [FiltroJWT] No hay cookies en la petición");
        }
        
        // 2) Fallback: obtener desde header Authorization (compatibilidad temporal)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            System.out.println("✅ [FiltroJWT] Token encontrado en header Authorization (fallback)");
            return authHeader.replace("Bearer ", "");
        }
        
        System.out.println("❌ [FiltroJWT] No se encontró token en cookies ni en header");
        return null;
    }

    private Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
