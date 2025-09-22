package utec.proyectofinal.Proyecto.Final.UTEC.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "INIA - Sistema de Análisis de Semillas API",
        description = "API REST para el Sistema de Análisis de Semillas del Instituto Nacional de Investigación Agropecuaria (INIA)",
        version = "v1.0",
        contact = @Contact(
            name = "Equipo de Desarrollo INIA",
            email = "desarrollo@inia.gub.uy"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor de Desarrollo"),
        @Server(url = "https://api.inia.gub.uy", description = "Servidor de Producción")
    }
)
@SecurityScheme(
    name = "JWT",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Ingrese el token JWT obtenido del endpoint /api/v1/auth/login"
)
public class OpenApiConfig {
}