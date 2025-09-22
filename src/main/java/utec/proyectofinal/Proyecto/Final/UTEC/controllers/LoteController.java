package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;
import utec.proyectofinal.Proyecto.Final.UTEC.services.LoteService;

@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = "*")
@Tag(name = "Lotes", description = "API para gestión de lotes de semillas")
@SecurityRequirement(name = "bearerAuth")
public class LoteController {

    @Autowired
    private LoteService loteService;

    // Crear nuevo Lote
    @PostMapping
    @Operation(summary = "Crear lote", description = "Crea un nuevo lote de semillas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<LoteDTO> crearLote(@RequestBody LoteRequestDTO solicitud) {
        try {
            LoteDTO loteCreado = loteService.crearLote(solicitud);
            return new ResponseEntity<>(loteCreado, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error al crear lote: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error interno al crear lote: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los Lotes activos (listado simple)
    @GetMapping("/activos")
    @Operation(summary = "Listar lotes activos", description = "Obtiene todos los lotes activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerTodosLotesActivos() {
        try {
            ResponseListadoLoteSimple respuesta = loteService.obtenerTodosLotesActivos();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los Lotes inactivos (listado simple)
    @GetMapping("/inactivos")
    @Operation(summary = "Listar lotes inactivos", description = "Obtiene todos los lotes inactivos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerTodosLotesInactivos() {
        try {
            ResponseListadoLoteSimple respuesta = loteService.obtenerTodosLotesInactivos();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Lote por ID (completo)
    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID", description = "Obtiene un lote específico por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<LoteDTO> obtenerLotePorId(@PathVariable Long id) {
        try {
            LoteDTO lote = loteService.obtenerLotePorId(id);
            return new ResponseEntity<>(lote, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Lote
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar lote", description = "Actualiza un lote existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<LoteDTO> actualizarLote(@PathVariable Long id, @RequestBody LoteRequestDTO solicitud) {
        try {
            LoteDTO loteActualizado = loteService.actualizarLote(id, solicitud);
            return new ResponseEntity<>(loteActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Lote (cambiar activo a false)
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lote", description = "Desactiva un lote (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> eliminarLote(@PathVariable Long id) {
        try {
            loteService.eliminarLote(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}