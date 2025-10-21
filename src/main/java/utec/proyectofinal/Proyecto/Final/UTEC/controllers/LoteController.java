package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;
import utec.proyectofinal.Proyecto.Final.UTEC.services.LoteService;

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "API para gestión de lotes de semillas")
@SecurityRequirement(name = "bearerAuth")
public class LoteController {

    @Autowired
    private LoteService loteService;

    // Crear nuevo Lote
    @PostMapping
    @Operation(summary = "Crear lote", description = "Crea un nuevo lote de semillas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
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
    @Operation(summary = "Listar lotes activos", description = "Obtiene todos los lotes activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Listar lotes inactivos", description = "Obtiene todos los lotes inactivos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Obtener lote por ID", description = "Obtiene un lote específico por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<LoteDTO> obtenerLotePorId(@PathVariable Long id) {
        try {
            LoteDTO lote = loteService.obtenerLotePorId(id);
            return new ResponseEntity<>(lote, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Lotes con paginado para listado
    @GetMapping("/listado")
    @Operation(summary = "Obtener lotes paginadas", description = "Obtiene la lista paginada de lotes activos para el listado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<org.springframework.data.domain.Page<LoteSimpleDTO>> obtenerLotesPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        try {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
            org.springframework.data.domain.Page<LoteSimpleDTO> response = loteService.obtenerLotesSimplePaginadas(pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Lote
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar lote", description = "Actualiza un lote existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<?> actualizarLote(@PathVariable Long id, @RequestBody LoteRequestDTO solicitud) {
        try {
            LoteDTO loteActualizado = loteService.actualizarLote(id, solicitud);
            return new ResponseEntity<>(loteActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            // Crear respuesta de error con mensaje
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("mensaje", e.getMessage());
            
            // Si el mensaje contiene "no encontrado", es un 404, sino es un 400
            if (e.getMessage().toLowerCase().contains("no encontrado")) {
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("mensaje", "Error interno del servidor");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Lote (cambiar activo a false)
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lote", description = "Desactiva un lote (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> eliminarLote(@PathVariable Long id) {
        try {
            loteService.eliminarLote(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Obtener lotes elegibles para un tipo de análisis específico
    @GetMapping("/elegibles/{tipoAnalisis}")
    @Operation(summary = "Obtener lotes elegibles para análisis", description = "Obtiene lotes que pueden tener análisis del tipo especificado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerLotesElegiblesParaTipoAnalisis(@PathVariable String tipoAnalisis) {
        try {
            TipoAnalisis tipo = TipoAnalisis.valueOf(tipoAnalisis.toUpperCase());
            ResponseListadoLoteSimple lotes = loteService.obtenerLotesElegiblesParaTipoAnalisis(tipo);
            return ResponseEntity.ok(lotes);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Verificar si se puede remover un tipo de análisis de un lote
    @GetMapping("/{loteID}/puede-remover-tipo/{tipoAnalisis}")
    @Operation(summary = "Verificar si se puede remover tipo de análisis", description = "Verifica si un tipo de análisis puede ser removido de un lote específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<Map<String, Object>> puedeRemoverTipoAnalisis(@PathVariable Long loteID, @PathVariable String tipoAnalisis) {
        try {
            TipoAnalisis tipo = TipoAnalisis.valueOf(tipoAnalisis.toUpperCase());
            boolean puedeRemover = loteService.puedeRemoverTipoAnalisis(loteID, tipo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("puedeRemover", puedeRemover);
            
            if (!puedeRemover) {
                response.put("razon", "Este lote tiene análisis completados de tipo " + tipo + " que no están marcados para repetir");
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("puedeRemover", false);
            response.put("razon", "Tipo de análisis no válido");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("puedeRemover", false);
            response.put("razon", "Error interno del servidor");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}