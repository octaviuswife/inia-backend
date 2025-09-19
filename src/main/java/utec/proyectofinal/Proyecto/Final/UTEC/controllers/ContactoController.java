package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ContactoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ContactoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ContactoService;

import java.util.List;

@RestController
@RequestMapping("/api/contactos")
@CrossOrigin(origins = "*")
public class ContactoController {

    @Autowired
    private ContactoService contactoService;

    // Obtener todos los contactos activos
    @GetMapping
    public ResponseEntity<List<ContactoDTO>> obtenerTodosLosContactos() {
        try {
            List<ContactoDTO> contactos = contactoService.obtenerTodosLosContactos();
            return ResponseEntity.ok(contactos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener solo clientes
    @GetMapping("/clientes")
    public ResponseEntity<List<ContactoDTO>> obtenerClientes() {
        try {
            List<ContactoDTO> clientes = contactoService.obtenerClientes();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener solo empresas
    @GetMapping("/empresas")
    public ResponseEntity<List<ContactoDTO>> obtenerEmpresas() {
        try {
            List<ContactoDTO> empresas = contactoService.obtenerEmpresas();
            return ResponseEntity.ok(empresas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Obtener contacto por ID
    @GetMapping("/{contactoID}")
    public ResponseEntity<ContactoDTO> obtenerContactoPorId(@PathVariable Long contactoID) {
        try {
            ContactoDTO contacto = contactoService.obtenerContactoPorId(contactoID);
            return ResponseEntity.ok(contacto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Crear nuevo contacto
    @PostMapping
    public ResponseEntity<?> crearContacto(@RequestBody ContactoRequestDTO contactoRequestDTO) {
        try {
            ContactoDTO contactoCreado = contactoService.crearContacto(contactoRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(contactoCreado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    // Actualizar contacto existente
    @PutMapping("/{contactoID}")
    public ResponseEntity<?> actualizarContacto(@PathVariable Long contactoID, 
                                              @RequestBody ContactoRequestDTO contactoRequestDTO) {
        try {
            ContactoDTO contactoActualizado = contactoService.actualizarContacto(contactoID, contactoRequestDTO);
            return ResponseEntity.ok(contactoActualizado);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    // Eliminar contacto (soft delete)
    @DeleteMapping("/{contactoID}")
    public ResponseEntity<?> eliminarContacto(@PathVariable Long contactoID) {
        try {
            contactoService.eliminarContacto(contactoID);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    // Reactivar contacto
    @PatchMapping("/{contactoID}/reactivar")
    public ResponseEntity<?> reactivarContacto(@PathVariable Long contactoID) {
        try {
            ContactoDTO contactoReactivado = contactoService.reactivarContacto(contactoID);
            return ResponseEntity.ok(contactoReactivado);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    // Buscar contactos por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<ContactoDTO>> buscarContactosPorNombre(@RequestParam String nombre) {
        try {
            List<ContactoDTO> contactos = contactoService.buscarContactosPorNombre(nombre);
            return ResponseEntity.ok(contactos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Buscar clientes por nombre
    @GetMapping("/clientes/buscar")
    public ResponseEntity<List<ContactoDTO>> buscarClientes(@RequestParam String nombre) {
        try {
            List<ContactoDTO> clientes = contactoService.buscarClientes(nombre);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Buscar empresas por nombre
    @GetMapping("/empresas/buscar")
    public ResponseEntity<List<ContactoDTO>> buscarEmpresas(@RequestParam String nombre) {
        try {
            List<ContactoDTO> empresas = contactoService.buscarEmpresas(nombre);
            return ResponseEntity.ok(empresas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}