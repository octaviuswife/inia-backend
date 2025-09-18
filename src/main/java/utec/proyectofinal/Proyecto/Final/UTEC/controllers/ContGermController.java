package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ContGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ContGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ContGermService;

@RestController
@RequestMapping("/api/germinacion/{germinacionId}/conteo")
@CrossOrigin(origins = "*")
public class ContGermController {

    @Autowired
    private ContGermService contGermService;

    // Crear nuevo conteo para una germinación
    @PostMapping
    public ResponseEntity<?> crearContGerm(
            @PathVariable Long germinacionId,
            @RequestBody ContGermRequestDTO solicitud) {
        try {
            ContGermDTO nuevoContGerm = contGermService.crearContGerm(germinacionId, solicitud);
            return new ResponseEntity<>(nuevoContGerm, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Obtener conteo por ID (dentro de una germinación)
    @GetMapping("/{conteoId}")
    public ResponseEntity<?> obtenerContGermPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId) {
        try {
            ContGermDTO contGerm = contGermService.obtenerContGermPorId(conteoId);
            return new ResponseEntity<>(contGerm, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Actualizar conteo existente
    @PutMapping("/{conteoId}")
    public ResponseEntity<?> actualizarContGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId,
            @RequestBody ContGermRequestDTO solicitud) {
        try {
            ContGermDTO contGermActualizado = contGermService.actualizarContGerm(conteoId, solicitud);
            return new ResponseEntity<>(contGermActualizado, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar conteo
    @DeleteMapping("/{conteoId}")
    public ResponseEntity<?> eliminarContGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId) {
        try {
            contGermService.eliminarContGerm(conteoId);
            return new ResponseEntity<>("Conteo eliminado exitosamente", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener todos los conteos de una germinación
    @GetMapping
    public ResponseEntity<?> obtenerConteosPorGerminacion(@PathVariable Long germinacionId) {
        try {
            List<ContGermDTO> conteos = contGermService.obtenerConteosPorGerminacion(germinacionId);
            return new ResponseEntity<>(conteos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Contar conteos de una germinación
    @GetMapping("/contar")
    public ResponseEntity<?> contarConteosPorGerminacion(@PathVariable Long germinacionId) {
        try {
            Long cantidad = contGermService.contarConteosPorGerminacion(germinacionId);
            return new ResponseEntity<>(cantidad, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}