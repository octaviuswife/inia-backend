package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;
import utec.proyectofinal.Proyecto.Final.UTEC.services.LoteService;

@RestController
@RequestMapping("/api/lotes")
@CrossOrigin(origins = "*")
public class LoteController {

    @Autowired
    private LoteService loteService;

    // Crear nuevo Lote
    @PostMapping
    public ResponseEntity<LoteDTO> crearLote(@RequestBody LoteRequestDTO solicitud) {
        try {
            LoteDTO loteCreado = loteService.crearLote(solicitud);
            return new ResponseEntity<>(loteCreado, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error al crear lote: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error interno al crear lote: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los Lotes activos (listado simple)
    @GetMapping("/activos")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerTodosLotesActivos() {
        try {
            ResponseListadoLoteSimple respuesta = loteService.obtenerTodosLotesActivos();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los Lotes inactivos (listado simple)
    @GetMapping("/inactivos")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerTodosLotesInactivos() {
        try {
            ResponseListadoLoteSimple respuesta = loteService.obtenerTodosLotesInactivos();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Lote por ID (completo)
    @GetMapping("/{id}")
    public ResponseEntity<LoteDTO> obtenerLotePorId(@PathVariable Integer id) {
        try {
            LoteDTO lote = loteService.obtenerLotePorId(id);
            return new ResponseEntity<>(lote, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Lote
    @PutMapping("/{id}")
    public ResponseEntity<LoteDTO> actualizarLote(@PathVariable Integer id, @RequestBody LoteRequestDTO solicitud) {
        try {
            LoteDTO loteActualizado = loteService.actualizarLote(id, solicitud);
            return new ResponseEntity<>(loteActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Lote (cambiar activo a false)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarLote(@PathVariable Integer id) {
        try {
            loteService.eliminarLote(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}