package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.security.JwtUtil;
import utec.proyectofinal.Proyecto.Final.UTEC.security.SeguridadService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private SeguridadService seguridadService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String usuario = loginData.get("usuario");
            String password = loginData.get("password");
            
            Optional<Usuario> usuarioOpt = seguridadService.autenticarUsuario(usuario, password);

            if (usuarioOpt.isPresent()) {
                Usuario user = usuarioOpt.get();
                String[] roles = seguridadService.listarRolesPorUsuario(user);
                
                String token = jwtUtil.generarToken(user, java.util.Arrays.asList(roles));
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("tipo", "Bearer");
                response.put("usuario", Map.of(
                    "id", user.getUsuarioID(),
                    "nombre", user.getNombre(),
                    "nombres", user.getNombres(),
                    "apellidos", user.getApellidos(),
                    "email", user.getEmail(),
                    "roles", roles
                ));
                
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales incorrectas"));
                    
        } catch (RuntimeException e) {
            String mensaje = switch (e.getMessage()) {
                case "USUARIO_INCORRECTO" -> "Usuario no encontrado";
                case "USUARIO_INACTIVO" -> "Usuario inactivo";
                case "CONTRASENIA_INCORRECTA" -> "Contraseña incorrecta";
                default -> "Error de autenticación";
            };
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", mensaje));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            
            if (jwtUtil.esTokenValido(token)) {
                String username = jwtUtil.obtenerUsuarioDelToken(token);
                return ResponseEntity.ok(Map.of(
                    "valido", true,
                    "usuario", username
                ));
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "error", "Token inválido"));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valido", false, "error", "Token malformado"));
        }
    }
}