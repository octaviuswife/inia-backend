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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepPmsService;

@RestController
@RequestMapping("/api/pms/{pmsId}/repeticiones")
@CrossOrigin(origins = "*")
public class RepPmsController {

    @Autowired
    private RepPmsService repeticionService;

    // Crear nueva repetición para un Pms específico
    @PostMapping
    public ResponseEntity<RepPmsDTO> crearRepeticion(
            @PathVariable Long pmsId,
            @RequestBody RepPmsRequestDTO solicitud) {
        try {
            RepPmsDTO repeticionCreada = repeticionService.crearRepeticion(pmsId, solicitud);
            return new ResponseEntity<>(repeticionCreada, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las repeticiones de un Pms
    @GetMapping
    public ResponseEntity<List<RepPmsDTO>> obtenerRepeticionesPorPms(@PathVariable Long pmsId) {
        try {
            List<RepPmsDTO> repeticiones = repeticionService.obtenerPorPms(pmsId);
            return new ResponseEntity<>(repeticiones, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Contar repeticiones de un Pms
    @GetMapping("/count")
    public ResponseEntity<Long> contarRepeticionesPorPms(@PathVariable Long pmsId) {
        try {
            Long count = repeticionService.contarPorPms(pmsId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener repetición específica por ID
    @GetMapping("/{repeticionId}")
    public ResponseEntity<RepPmsDTO> obtenerRepeticionPorId(
            @PathVariable Long pmsId,
            @PathVariable Long repeticionId) {
        try {
            RepPmsDTO repeticion = repeticionService.obtenerPorId(repeticionId);
            return new ResponseEntity<>(repeticion, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar repetición específica
    @PutMapping("/{repeticionId}")
    public ResponseEntity<RepPmsDTO> actualizarRepeticion(
            @PathVariable Long pmsId,
            @PathVariable Long repeticionId,
            @RequestBody RepPmsRequestDTO solicitud) {
        try {
            RepPmsDTO repeticionActualizada = repeticionService.actualizarRepeticion(repeticionId, solicitud);
            return new ResponseEntity<>(repeticionActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar repetición específica
    @DeleteMapping("/{repeticionId}")
    public ResponseEntity<HttpStatus> eliminarRepeticion(
            @PathVariable Long pmsId,
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
