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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoTetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TetrazolioService;

@RestController
@RequestMapping("/api/tetrazolios")
@CrossOrigin(origins = "*")
public class TetrazolioController {

    @Autowired
    private TetrazolioService tetrazolioService;

    // Crear nuevo Tetrazolio
    @PostMapping
    public ResponseEntity<TetrazolioDTO> crearTetrazolio(@RequestBody TetrazolioRequestDTO solicitud) {
        try {
            System.out.println("Creando tetrazolio con solicitud: " + solicitud);
            TetrazolioDTO tetrazolioCreado = tetrazolioService.crearTetrazolio(solicitud);
            return new ResponseEntity<>(tetrazolioCreado, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error al crear tetrazolio: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error interno al crear tetrazolio: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los Tetrazolios activos
    @GetMapping
    public ResponseEntity<ResponseListadoTetrazolio> obtenerTodosTetrazolio() {
        try {
            ResponseListadoTetrazolio response = tetrazolioService.obtenerTodosTetrazolio();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Tetrazolio por ID
    @GetMapping("/{id}")
    public ResponseEntity<TetrazolioDTO> obtenerTetrazolioPorId(@PathVariable Long id) {
        try {
            TetrazolioDTO tetrazolio = tetrazolioService.obtenerTetrazolioPorId(id);
            return new ResponseEntity<>(tetrazolio, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Tetrazolio
    @PutMapping("/{id}")
    public ResponseEntity<TetrazolioDTO> actualizarTetrazolio(@PathVariable Long id, @RequestBody TetrazolioRequestDTO solicitud) {
        try {
            TetrazolioDTO tetrazolioActualizado = tetrazolioService.actualizarTetrazolio(id, solicitud);
            return new ResponseEntity<>(tetrazolioActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Tetrazolio (cambiar estado a INACTIVO)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarTetrazolio(@PathVariable Long id) {
        try {
            tetrazolioService.eliminarTetrazolio(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Tetrazolios por Lote
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<TetrazolioDTO>> obtenerTetrazoliosPorIdLote(@PathVariable Long idLote) {
        try {
            List<TetrazolioDTO> tetrazolios = tetrazolioService.obtenerTetrazoliosPorIdLote(idLote);
            return new ResponseEntity<>(tetrazolios, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}