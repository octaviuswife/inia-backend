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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoGerminacion;
import utec.proyectofinal.Proyecto.Final.UTEC.services.GerminacionService;

@RestController
@RequestMapping("/api/germinaciones")
@CrossOrigin(origins = "*")
public class GerminacionController {

    @Autowired
    private GerminacionService germinacionService;

    // Crear nueva Germinación
    @PostMapping
    public ResponseEntity<GerminacionDTO> crearGerminacion(@RequestBody GerminacionRequestDTO solicitud) {
        try {
            System.out.println("Creando germinación con solicitud: " + solicitud);
            GerminacionDTO germinacionCreada = germinacionService.crearGerminacion(solicitud);
            return new ResponseEntity<>(germinacionCreada, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error al crear germinación: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error interno al crear germinación: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las Germinaciones activas
    @GetMapping
    public ResponseEntity<ResponseListadoGerminacion> obtenerTodasGerminaciones() {
        try {
            ResponseListadoGerminacion response = germinacionService.obtenerTodasGerminaciones();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Germinación por ID
    @GetMapping("/{id}")
    public ResponseEntity<GerminacionDTO> obtenerGerminacionPorId(@PathVariable Long id) {
        try {
            GerminacionDTO germinacion = germinacionService.obtenerGerminacionPorId(id);
            return new ResponseEntity<>(germinacion, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Germinación
    @PutMapping("/{id}")
    public ResponseEntity<GerminacionDTO> actualizarGerminacion(@PathVariable Long id, @RequestBody GerminacionRequestDTO solicitud) {
        try {
            GerminacionDTO germinacionActualizada = germinacionService.actualizarGerminacion(id, solicitud);
            return new ResponseEntity<>(germinacionActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Germinación (cambiar estado a INACTIVO)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarGerminacion(@PathVariable Long id) {
        try {
            germinacionService.eliminarGerminacion(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Germinaciones por Lote
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<GerminacionDTO>> obtenerGerminacionesPorIdLote(@PathVariable Long idLote) {
        try {
            List<GerminacionDTO> germinaciones = germinacionService.obtenerGerminacionesPorIdLote(idLote);
            return new ResponseEntity<>(germinaciones, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}