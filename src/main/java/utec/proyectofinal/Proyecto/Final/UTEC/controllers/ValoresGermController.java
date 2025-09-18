package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ValoresGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ValoresGermService;

@RestController
@RequestMapping("/api/germinacion/{germinacionId}/tabla/{tablaId}/valores")
@CrossOrigin(origins = "*")
public class ValoresGermController {

    @Autowired
    private ValoresGermService valoresGermService;

    // Obtener valores por ID
    @GetMapping("/{valoresId}")
    public ResponseEntity<?> obtenerValoresPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long valoresId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresPorId(valoresId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Actualizar valores existentes
    @PutMapping("/{valoresId}")
    public ResponseEntity<?> actualizarValores(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long valoresId,
            @RequestBody ValoresGermRequestDTO solicitud) {
        try {
            ValoresGermDTO valoresActualizados = valoresGermService.actualizarValores(valoresId, solicitud);
            return new ResponseEntity<>(valoresActualizados, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener todos los valores de una tabla
    @GetMapping
    public ResponseEntity<?> obtenerValoresPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            List<ValoresGermDTO> valores = valoresGermService.obtenerValoresPorTabla(tablaId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Obtener valores de INIA para una tabla
    @GetMapping("/inia")
    public ResponseEntity<?> obtenerValoresIniaPorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresIniaPorTabla(tablaId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener valores de INASE para una tabla
    @GetMapping("/inase")
    public ResponseEntity<?> obtenerValoresInasePorTabla(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresInasePorTabla(tablaId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener valores por tabla e instituto (parámetro de consulta)
    @GetMapping("/instituto")
    public ResponseEntity<?> obtenerValoresPorTablaEInstituto(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @RequestParam Instituto instituto) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresPorTablaEInstituto(tablaId, instituto);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar valores
    @DeleteMapping("/{valoresId}")
    public ResponseEntity<?> eliminarValores(
            @PathVariable Long germinacionId,
            @PathVariable Long tablaId,
            @PathVariable Long valoresId) {
        try {
            valoresGermService.eliminarValores(valoresId);
            return new ResponseEntity<>("Valores de germinación eliminados exitosamente", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}