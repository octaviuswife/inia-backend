package utec.proyectofinal.Proyecto.Final.UTEC.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final String CLAVE = "@Z9@vQ3!pL8#wX7^tR2&nG6*yM4$eB1(dF0)sH5%kJ3&uY8*rE4#wQ1@zX6^nM9$";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(CLAVE.getBytes(StandardCharsets.UTF_8));
    private final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hora en milisegundos
    private final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 días en milisegundos

    public String generarToken(Usuario usuario, List<String> roles) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(usuario.getNombre())
                .claim("authorities", roles)
                .claim("userId", usuario.getUsuarioID())
                .claim("email", usuario.getEmail())
                .claim("nombres", usuario.getNombres())
                .claim("apellidos", usuario.getApellidos())
                .claim("type", "access")
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generarRefreshToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + REFRESH_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(usuario.getNombre())
                .claim("userId", usuario.getUsuarioID())
                .claim("type", "refresh")
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean esTokenValido(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String obtenerUsuarioDelToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Integer obtenerUserIdDelToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Integer.class);
    }

    public String obtenerTipoToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("type", String.class);
    }

    public long getAccessTokenExpiration() {
        return ACCESS_TOKEN_EXPIRATION;
    }

    public long getRefreshTokenExpiration() {
        return REFRESH_TOKEN_EXPIRATION;
    }
}