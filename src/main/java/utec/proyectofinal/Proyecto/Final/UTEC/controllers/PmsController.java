package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPms;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PmsService;

@RestController
@RequestMapping("/api/pms")
@CrossOrigin(origins = "*")
public class PmsController {

    @Autowired
    private PmsService pmsService;

    // Crear nuevo Pms
    @PostMapping
    public ResponseEntity<PmsDTO> crearPms(@RequestBody PmsRequestDTO solicitud) {
        try {
            PmsDTO pmsCreado = pmsService.crearPms(solicitud);
            return new ResponseEntity<>(pmsCreado, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los Pms activos
    @GetMapping
    public ResponseEntity<ResponseListadoPms> obtenerTodosPmsActivos() {
        try {
            ResponseListadoPms respuesta = pmsService.obtenerTodosPmsActivos();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Pms por ID
    @GetMapping("/{id}")
    public ResponseEntity<PmsDTO> obtenerPmsPorId(@PathVariable Long id) {
        try {
            PmsDTO pms = pmsService.obtenerPmsPorId(id);
            return new ResponseEntity<>(pms, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Pms
    @PutMapping("/{id}")
    public ResponseEntity<PmsDTO> actualizarPms(@PathVariable Long id, @RequestBody PmsRequestDTO solicitud) {
        try {
            PmsDTO pmsActualizado = pmsService.actualizarPms(id, solicitud);
            return new ResponseEntity<>(pmsActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Pms (cambiar estado a INACTIVO)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarPms(@PathVariable Long id) {
        try {
            pmsService.eliminarPms(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
