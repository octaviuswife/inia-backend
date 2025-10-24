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

// CORS configurado globalmente en WebSecurityConfig
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
        LoteDTO loteCreado = loteService.crearLote(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(loteCreado);
    }

    // Obtener todos los Lotes activos (listado simple)
    @GetMapping("/activos")
    @Operation(summary = "Listar lotes activos", description = "Obtiene todos los lotes activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerTodosLotesActivos() {
        ResponseListadoLoteSimple respuesta = loteService.obtenerTodosLotesActivos();
        return ResponseEntity.ok(respuesta);
    }

    // Obtener todos los Lotes inactivos (listado simple)
    @GetMapping("/inactivos")
    @Operation(summary = "Listar lotes inactivos", description = "Obtiene todos los lotes inactivos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerTodosLotesInactivos() {
        ResponseListadoLoteSimple respuesta = loteService.obtenerTodosLotesInactivos();
        return ResponseEntity.ok(respuesta);
    }

    // Obtener Lote por ID (completo)
    @GetMapping("/{id}")
    @Operation(summary = "Obtener lote por ID", description = "Obtiene un lote específico por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<LoteDTO> obtenerLotePorId(@PathVariable Long id) {
        LoteDTO lote = loteService.obtenerLotePorId(id);
        return ResponseEntity.ok(lote);
    }

    // Obtener Lotes con paginado para listado
    @GetMapping("/listado")
    @Operation(summary = "Obtener lotes paginadas con filtros", description = "Obtiene la lista paginada de lotes con soporte para búsqueda y filtros")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<org.springframework.data.domain.Page<LoteSimpleDTO>> obtenerLotesPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Boolean activo,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String cultivar) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<LoteSimpleDTO> response = loteService.obtenerLotesSimplePaginadasConFiltros(pageable, search, activo, cultivar);
        return ResponseEntity.ok(response);
    }
    
    // Obtener estadísticas de lotes
    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de lotes", description = "Obtiene el conteo total de lotes, activos e inactivos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<java.util.Map<String, Long>> obtenerEstadisticasLotes() {
        java.util.Map<String, Long> stats = loteService.obtenerEstadisticasLotes();
        return ResponseEntity.ok(stats);
    }

    // Actualizar Lote
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar lote", description = "Actualiza un lote existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<LoteDTO> actualizarLote(@PathVariable Long id, @RequestBody LoteRequestDTO solicitud) {
        LoteDTO loteActualizado = loteService.actualizarLote(id, solicitud);
        return ResponseEntity.ok(loteActualizado);
    }

    // Eliminar Lote (cambiar activo a false)
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar lote", description = "Desactiva un lote (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarLote(@PathVariable Long id) {
        loteService.eliminarLote(id);
        return ResponseEntity.ok().build();
    }

    // Reactivar Lote (cambiar activo a true)
    @PutMapping("/{id}/reactivar")
    @Operation(summary = "Reactivar lote", description = "Reactiva un lote desactivado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoteDTO> reactivarLote(@PathVariable Long id) {
        LoteDTO loteReactivado = loteService.reactivarLote(id);
        return ResponseEntity.ok(loteReactivado);
    }
    
    // Obtener lotes elegibles para un tipo de análisis específico
    @GetMapping("/elegibles/{tipoAnalisis}")
    @Operation(summary = "Obtener lotes elegibles para análisis", description = "Obtiene lotes que pueden tener análisis del tipo especificado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    public ResponseEntity<ResponseListadoLoteSimple> obtenerLotesElegiblesParaTipoAnalisis(@PathVariable String tipoAnalisis) {
        TipoAnalisis tipo = TipoAnalisis.valueOf(tipoAnalisis.toUpperCase());
        ResponseListadoLoteSimple lotes = loteService.obtenerLotesElegiblesParaTipoAnalisis(tipo);
        return ResponseEntity.ok(lotes);
    }
    
    // Verificar si se puede remover un tipo de análisis de un lote
    @GetMapping("/{loteID}/puede-remover-tipo/{tipoAnalisis}")
    @Operation(summary = "Verificar si se puede remover tipo de análisis", description = "Verifica si un tipo de análisis puede ser removido de un lote específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<Map<String, Object>> puedeRemoverTipoAnalisis(@PathVariable Long loteID, @PathVariable String tipoAnalisis) {
        TipoAnalisis tipo = TipoAnalisis.valueOf(tipoAnalisis.toUpperCase());
        boolean puedeRemover = loteService.puedeRemoverTipoAnalisis(loteID, tipo);
        
        Map<String, Object> response = new HashMap<>();
        response.put("puedeRemover", puedeRemover);
        
        if (!puedeRemover) {
            response.put("razon", "Este lote tiene análisis completados de tipo " + tipo + " que no están marcados para repetir");
        }
        
        return ResponseEntity.ok(response);
    }

}