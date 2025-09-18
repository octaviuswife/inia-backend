package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TablaGermService;

@RestController
@RequestMapping("/api/germinacion/{germinacionId}/tabla")
@CrossOrigin(origins = "*")
public class TablaGermController {

    @Autowired
    private TablaGermService tablaGermService;

    // Crear nueva tabla para una germinación
    @PostMapping
    public ResponseEntity<?> crearTablaGerm(
            @PathVariable Long germinacionId,
            @RequestBody TablaGermRequestDTO solicitud) {
        try {
            TablaGermDTO nuevaTablaGerm = tablaGermService.crearTablaGerm(germinacionId, solicitud);
            return new ResponseEntity<>(nuevaTablaGerm, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener tabla por ID (dentro de una germinación)
    @GetMapping("/{tablaId}")
    public ResponseEntity<?> obtenerTablaGermPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            TablaGermDTO tablaGerm = tablaGermService.obtenerTablaGermPorId(tablaId);
            return new ResponseEntity<>(tablaGerm, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Actualizar tabla existente
    @PutMapping("/{tablaId}")
    public ResponseEntity<?> actualizarTablaGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestBody TablaGermRequestDTO solicitud) {
        try {
            TablaGermDTO tablaGermActualizada = tablaGermService.actualizarTablaGerm(tablaId, solicitud);
            return new ResponseEntity<>(tablaGermActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar tabla
    @DeleteMapping("/{tablaId}")
    public ResponseEntity<?> eliminarTablaGerm(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            tablaGermService.eliminarTablaGerm(tablaId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener todas las tablas de una germinación
    @GetMapping
    public ResponseEntity<?> obtenerTablasPorGerminacion(@PathVariable Long germinacionId) {
        try {
            List<TablaGermDTO> tablas = tablaGermService.obtenerTablasPorGerminacion(germinacionId);
            return new ResponseEntity<>(tablas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Contar tablas de una germinación
    @GetMapping("/contar")
    public ResponseEntity<?> contarTablasPorGerminacion(@PathVariable Long germinacionId) {
        try {
            Long cantidad = tablaGermService.contarTablasPorGerminacion(germinacionId);
            return new ResponseEntity<>(cantidad, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}