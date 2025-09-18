package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ValoresGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ValoresGermService;

@RestController
@RequestMapping("/api/germinacion/{germinacionId}/conteo/{conteoId}/valores")
@CrossOrigin(origins = "*")
public class ValoresGermController {

    @Autowired
    private ValoresGermService valoresGermService;

    // Obtener valores por ID
    @GetMapping("/{valoresId}")
    public ResponseEntity<?> obtenerValoresPorId(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId,
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
            @PathVariable Long conteoId,
            @PathVariable Long valoresId,
            @RequestBody ValoresGermRequestDTO solicitud) {
        try {
            ValoresGermDTO valoresActualizados = valoresGermService.actualizarValores(valoresId, solicitud);
            return new ResponseEntity<>(valoresActualizados, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener todos los valores de un conteo
    @GetMapping
    public ResponseEntity<?> obtenerValoresPorConteo(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId) {
        try {
            List<ValoresGermDTO> valores = valoresGermService.obtenerValoresPorConteo(conteoId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Obtener valores de INIA para un conteo
    @GetMapping("/inia")
    public ResponseEntity<?> obtenerValoresIniaPorConteo(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresIniaPorConteo(conteoId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener valores de INASE para un conteo
    @GetMapping("/inase")
    public ResponseEntity<?> obtenerValoresInasePorConteo(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresInasePorConteo(conteoId);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Obtener valores por conteo e instituto (parámetro de consulta)
    @GetMapping("/instituto")
    public ResponseEntity<?> obtenerValoresPorConteoEInstituto(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId,
            @RequestParam Instituto instituto) {
        try {
            ValoresGermDTO valores = valoresGermService.obtenerValoresPorConteoEInstituto(conteoId, instituto);
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar valores
    @DeleteMapping("/{valoresId}")
    public ResponseEntity<?> eliminarValores(
            @PathVariable Long germinacionId,
            @PathVariable Long conteoId,
            @PathVariable Long valoresId) {
        try {
            valoresGermService.eliminarValores(valoresId);
            return new ResponseEntity<>("Valores de germinación eliminados exitosamente", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}