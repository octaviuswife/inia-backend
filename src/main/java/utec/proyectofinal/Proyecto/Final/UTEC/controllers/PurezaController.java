package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasYCultivosCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPureza;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PurezaService;

@RestController
@RequestMapping("/api/purezas")
@CrossOrigin(origins = "*")
public class PurezaController {

    @Autowired
    private PurezaService purezaService;

    // Crear nueva Pureza
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PurezaDTO> crearPureza(@RequestBody PurezaRequestDTO solicitud) {
        try {
            System.out.println("Creando pureza con solicitud: " + solicitud);
            PurezaDTO purezaCreada = purezaService.crearPureza(solicitud);
            return new ResponseEntity<>(purezaCreada, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.err.println("Error al crear pureza: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error interno al crear pureza: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las Purezas activas
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<ResponseListadoPureza> obtenerTodasPurezasActivas() {
        try {
            ResponseListadoPureza respuesta = purezaService.obtenerTodasPurezasActivas();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Pureza por ID
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<PurezaDTO> obtenerPurezaPorId(@PathVariable Long id) {
        try {
            PurezaDTO pureza = purezaService.obtenerPurezaPorId(id);
            return new ResponseEntity<>(pureza, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Pureza
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PurezaDTO> actualizarPureza(@PathVariable Long id, @RequestBody PurezaRequestDTO solicitud) {
        try {
            PurezaDTO purezaActualizada = purezaService.actualizarPureza(id, solicitud);
            return new ResponseEntity<>(purezaActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Pureza (cambiar estado a INACTIVO)
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarPureza(@PathVariable Long id) {
        try {
            purezaService.eliminarPureza(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Purezas por Lote
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<PurezaDTO>> obtenerPurezasPorIdLote(@PathVariable Long idLote) {
        try {
            List<PurezaDTO> purezas = purezaService.obtenerPurezasPorIdLote(idLote);
            return new ResponseEntity<>(purezas, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todos los catálogos para el select de otras semillas
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/catalogos")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerTodosCatalogos() {
        try {
            List<MalezasYCultivosCatalogoDTO> catalogos = purezaService.obtenerTodosCatalogos();
            return new ResponseEntity<>(catalogos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Finalizar análisis de pureza
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<PurezaDTO> finalizarAnalisis(@PathVariable Long id) {
        try {
            PurezaDTO analisisFinalizado = purezaService.finalizarAnalisis(id);
            return new ResponseEntity<>(analisisFinalizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Aprobar análisis (solo admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<PurezaDTO> aprobarAnalisis(@PathVariable Long id) {
        try {
            PurezaDTO analisisAprobado = purezaService.aprobarAnalisis(id);
            return new ResponseEntity<>(analisisAprobado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
