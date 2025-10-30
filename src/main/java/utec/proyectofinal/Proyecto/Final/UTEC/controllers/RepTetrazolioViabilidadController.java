package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepTetrazolioViabilidadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepTetrazolioViabilidadDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepTetrazolioViabilidadService;

// CORS configurado globalmente en WebSecurityConfig
@RestController
@RequestMapping("/api/tetrazolios/{tetrazolioId}/repeticiones")
@Tag(name = "Repeticiones Tetrazolio Viabilidad", description = "API para gestión de repeticiones de análisis de tetrazolio viabilidad")
@SecurityRequirement(name = "bearerAuth")
public class RepTetrazolioViabilidadController {

    @Autowired
    private RepTetrazolioViabilidadService repeticionService;

    // Crear nueva repetición para un tetrazolio específico
    @Operation(summary = "Crear repetición de tetrazolio viabilidad", 
              description = "Crea una nueva repetición para un análisis de tetrazolio viabilidad específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PostMapping
    public ResponseEntity<RepTetrazolioViabilidadDTO> crearRepeticion(
            @PathVariable Long tetrazolioId,
            @RequestBody RepTetrazolioViabilidadRequestDTO solicitud) {
        try {
            System.out.println("Creando repetición para tetrazolio ID: " + tetrazolioId + " con datos: " + solicitud);
            RepTetrazolioViabilidadDTO repeticionCreada = repeticionService.crearRepeticion(tetrazolioId, solicitud);
            return new ResponseEntity<>(repeticionCreada, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error al crear repetición: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error interno al crear repetición: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las repeticiones de un tetrazolio
    @Operation(summary = "Listar repeticiones de tetrazolio viabilidad", 
              description = "Obtiene todas las repeticiones asociadas a un análisis de tetrazolio viabilidad específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<List<RepTetrazolioViabilidadDTO>> obtenerRepeticionesPorTetrazolio(@PathVariable Long tetrazolioId) {
        try {
            List<RepTetrazolioViabilidadDTO> repeticiones = repeticionService.obtenerRepeticionesPorTetrazolio(tetrazolioId);
            return new ResponseEntity<>(repeticiones, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error al obtener repeticiones: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Contar repeticiones de un tetrazolio
    @Operation(summary = "Contar repeticiones de tetrazolio viabilidad", 
              description = "Cuenta el número de repeticiones asociadas a un análisis de tetrazolio viabilidad específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/count")
    public ResponseEntity<Long> contarRepeticionesPorTetrazolio(@PathVariable Long tetrazolioId) {
        try {
            Long count = repeticionService.contarRepeticionesPorTetrazolio(tetrazolioId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener repetición específica por ID (ruta alternativa fuera del contexto del tetrazolio)
    @Operation(summary = "Obtener repetición de tetrazolio viabilidad por ID", 
              description = "Obtiene una repetición específica de un análisis de tetrazolio viabilidad por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{repeticionId}")
    public ResponseEntity<RepTetrazolioViabilidadDTO> obtenerRepeticionPorId(
            @PathVariable Long tetrazolioId,
            @PathVariable Long repeticionId) {
        try {
            RepTetrazolioViabilidadDTO repeticion = repeticionService.obtenerRepeticionPorId(repeticionId);
            return new ResponseEntity<>(repeticion, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar repetición específica
    @Operation(summary = "Actualizar repetición de tetrazolio viabilidad", 
              description = "Actualiza una repetición específica de un análisis de tetrazolio viabilidad")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{repeticionId}")
    public ResponseEntity<RepTetrazolioViabilidadDTO> actualizarRepeticion(
            @PathVariable Long tetrazolioId,
            @PathVariable Long repeticionId,
            @RequestBody RepTetrazolioViabilidadRequestDTO solicitud) {
        try {
            RepTetrazolioViabilidadDTO repeticionActualizada = repeticionService.actualizarRepeticion(repeticionId, solicitud);
            return new ResponseEntity<>(repeticionActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar repetición específica (eliminación real)
    @Operation(summary = "Eliminar repetición de tetrazolio viabilidad", 
              description = "Elimina una repetición específica de un análisis de tetrazolio viabilidad (eliminación física)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{repeticionId}")
    public ResponseEntity<HttpStatus> eliminarRepeticion(
            @PathVariable Long tetrazolioId,
            @PathVariable Long repeticionId) {
        try {
            repeticionService.eliminarRepeticion(repeticionId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}