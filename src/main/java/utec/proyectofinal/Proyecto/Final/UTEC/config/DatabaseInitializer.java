package utec.proyectofinal.Proyecto.Final.UTEC.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;

/**
 * Inicializador de datos base del sistema.
 * Crea el usuario administrador predeterminado si no existe.
 */
@Component
@Order(1) // Se ejecuta primero
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        try {
            // Verificar si ya existe al menos un admin en el sistema
            if (!usuarioRepository.existsByRol(Rol.ADMIN)) {
                usuarioService.crearAdminPredeterminado();
                System.out.println("✅ Administrador predeterminado creado:");
                System.out.println("   Usuario: admin");
                System.out.println("   Contraseña: admin123");
                System.out.println("   Email: admin@inia.gub.uy");
                System.out.println("   ⚠️  IMPORTANTE: Cambiar la contraseña después del primer login");
            } else {
                System.out.println("ℹ️  Ya existe al menos un administrador en el sistema");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar admin predeterminado: " + e.getMessage());
        }
    }
}