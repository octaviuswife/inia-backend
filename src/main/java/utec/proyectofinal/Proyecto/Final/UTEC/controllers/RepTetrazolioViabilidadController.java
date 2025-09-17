package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepTetrazolioViabilidadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepTetrazolioViabilidadDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepTetrazolioViabilidadService;

@RestController
@RequestMapping("/api/tetrazolios/{tetrazolioId}/repeticiones")
@CrossOrigin(origins = "*")
public class RepTetrazolioViabilidadController {

    @Autowired
    private RepTetrazolioViabilidadService repeticionService;

    // Crear nueva repetición para un tetrazolio específico
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