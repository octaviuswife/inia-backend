package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;    
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TablaGermService;

@RestController
@RequestMapping("/api/germinacion/{germinacionId}/tabla")
@CrossOrigin(origins = "*")
@Tag(name = "Tabla de Germinación", description = "API para gestión de tablas dentro del análisis de germinación")
public class TablaGermController {

    @Autowired
    private TablaGermService tablaGermService;

    // Crear nueva tabla para una germinación
    @Operation(summary = "Crear tabla de germinación", 
              description = "Crea una nueva tabla de germinación. Solo se puede crear si todas las tablas anteriores están finalizadas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tabla creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede crear - tabla anterior no finalizada o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Germinación no encontrada")
    })
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
// Contar tablas de una germinación
    @GetMapping("/contar")
    public ResponseEntity<?> contarTablasPorGerminacion(@PathVariable Long germinacionId) {
        try {
            Long count = tablaGermService.contarTablasPorGerminacion(germinacionId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Finalizar tabla (marcar como finalizada)
    @Operation(summary = "Finalizar tabla", 
              description = "Marca una tabla como finalizada. Requiere que todas las repeticiones estén completas y porcentajes ingresados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tabla finalizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se puede finalizar - faltan repeticiones o porcentajes"),
        @ApiResponse(responseCode = "404", description = "Tabla no encontrada")
    })
    @PutMapping("/{tablaId}/finalizar")
    public ResponseEntity<?> finalizarTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            TablaGermDTO tablaFinalizada = tablaGermService.finalizarTabla(tablaId);
            return new ResponseEntity<>(tablaFinalizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Verificar si se pueden ingresar porcentajes
    @GetMapping("/{tablaId}/puede-ingresar-porcentajes")
    public ResponseEntity<?> puedeIngresarPorcentajes(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            boolean puede = tablaGermService.puedeIngresarPorcentajes(tablaId);
            return new ResponseEntity<>(puede, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar porcentajes con redondeo
    @Operation(summary = "Actualizar porcentajes con redondeo", 
              description = "Actualiza los 5 porcentajes con redondeo de una tabla. Solo disponible cuando todas las repeticiones están completas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Porcentajes actualizados exitosamente"),
        @ApiResponse(responseCode = "400", description = "No se pueden ingresar porcentajes - repeticiones incompletas"),
        @ApiResponse(responseCode = "404", description = "Tabla no encontrada")
    })
    @PutMapping("/{tablaId}/porcentajes")
    public ResponseEntity<?> actualizarPorcentajes(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestBody PorcentajesRedondeoRequestDTO solicitud) {
        try {
            TablaGermDTO tablaActualizada = tablaGermService.actualizarPorcentajes(tablaId, solicitud);
            return new ResponseEntity<>(tablaActualizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
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
}