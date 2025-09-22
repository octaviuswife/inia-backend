package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ContactoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ContactoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ContactoService;

import java.util.List;

@RestController
@RequestMapping("/api/contactos")
@CrossOrigin(origins = "*")
@Tag(name = "Contactos", description = "API para gestión de contactos y empresas")
@SecurityRequirement(name = "bearerAuth")
public class ContactoController {

    @Autowired
    private ContactoService contactoService;

    // Obtener todos los contactos activos
    @Operation(summary = "Listar todos los contactos", 
              description = "Obtiene todos los contactos activos, incluyendo clientes y empresas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Listar todos los clientes", 
              description = "Obtiene todos los contactos que son clientes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Listar todas las empresas", 
              description = "Obtiene todos los contactos que son empresas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Obtener contacto por ID", 
              description = "Obtiene un contacto específico por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Crear nuevo contacto", 
              description = "Crea un nuevo contacto o empresa (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
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
    @Operation(summary = "Actualizar contacto", 
              description = "Actualiza un contacto o empresa existente (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
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
    @Operation(summary = "Eliminar contacto")
    @PreAuthorize("hasRole('ADMIN')")
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
    @Operation(summary = "Reactivar contacto")
    @PreAuthorize("hasRole('ADMIN')")
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
    @Operation(summary = "Buscar contactos por nombre")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Buscar clientes por nombre")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Buscar empresas por nombre")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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