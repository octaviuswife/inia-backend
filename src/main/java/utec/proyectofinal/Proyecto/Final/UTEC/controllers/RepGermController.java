package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.RepGermService;

@RestController
@RequestMapping("/api/germinacion/{germinacionId}/tabla/{tablaId}/repeticion")
@CrossOrigin(origins = "*")
public class RepGermController {

    @Autowired
    private RepGermService repGermService;

    // Crear nueva repetición para una tabla
    @PostMapping
    public ResponseEntity<?> crearRepGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestBody RepGermRequestDTO solicitud) {
        try {
            RepGermDTO nuevaRepeticion = repGermService.crearRepGerm(tablaId, solicitud);
            return new ResponseEntity<>(nuevaRepeticion, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Obtener repetición por ID
    @GetMapping("/{repeticionId}")
    public ResponseEntity<?> obtenerRepGermPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long repeticionId) {
        try {
            RepGermDTO repeticion = repGermService.obtenerRepGermPorId(repeticionId);
            return new ResponseEntity<>(repeticion, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Actualizar repetición existente
    @PutMapping("/{repeticionId}")
    public ResponseEntity<?> actualizarRepGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long repeticionId,
            @RequestBody RepGermRequestDTO solicitud) {
        try {
            RepGermDTO repeticionActualizada = repGermService.actualizarRepGerm(repeticionId, solicitud);
            return new ResponseEntity<>(repeticionActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar repetición
    @DeleteMapping("/{repeticionId}")
    public ResponseEntity<?> eliminarRepGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long repeticionId) {
        try {
            repGermService.eliminarRepGerm(repeticionId);
            return new ResponseEntity<>("Repetición eliminada exitosamente", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener todas las repeticiones de una tabla
    @GetMapping
    public ResponseEntity<?> obtenerRepeticionesPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            List<RepGermDTO> repeticiones = repGermService.obtenerRepeticionesPorTabla(tablaId);
            return new ResponseEntity<>(repeticiones, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Contar repeticiones de una tabla
    @GetMapping("/contar")
    public ResponseEntity<?> contarRepeticionesPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            Long cantidad = repGermService.contarRepeticionesPorTabla(tablaId);
            return new ResponseEntity<>(cantidad, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}