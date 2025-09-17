package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DosnService;

@RestController
@RequestMapping("/api/dosn")
@CrossOrigin(origins = "*")
public class DosnController {

    @Autowired
    private DosnService dosnService;

    // Crear nueva Dosn
    @PostMapping
    public ResponseEntity<DosnDTO> crearDosn(@RequestBody DosnRequestDTO solicitud) {
        try {
            DosnDTO dosnCreado = dosnService.crearDosn(solicitud);
            return new ResponseEntity<>(dosnCreado, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las Dosn activas
    @GetMapping
    public ResponseEntity<ResponseListadoDosn> obtenerTodasDosnActivas() {
        try {
            ResponseListadoDosn respuesta = dosnService.obtenerTodasDosnActivas();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Dosn por ID
    @GetMapping("/{id}")
    public ResponseEntity<DosnDTO> obtenerDosnPorId(@PathVariable Long id) {
        try {
            DosnDTO dosn = dosnService.obtenerDosnPorId(id);
            return new ResponseEntity<>(dosn, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Dosn
    @PutMapping("/{id}")
    public ResponseEntity<DosnDTO> actualizarDosn(@PathVariable Long id, @RequestBody DosnRequestDTO solicitud) {
        try {
            DosnDTO dosnActualizado = dosnService.actualizarDosn(id, solicitud);
            return new ResponseEntity<>(dosnActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Dosn (cambiar estado a INACTIVO)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarDosn(@PathVariable Long id) {
        try {
            dosnService.eliminarDosn(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Dosn por Lote
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<DosnDTO>> obtenerDosnPorIdLote(@PathVariable Integer idLote) {
        try {
            List<DosnDTO> dosn = dosnService.obtenerDosnPorIdLote(idLote);
            return new ResponseEntity<>(dosn, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
