package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PurezaRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasYCultivosCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoPureza;
import utec.proyectofinal.Proyecto.Final.UTEC.services.PurezaService;

// CORS configurado globalmente en WebSecurityConfig
@RestController
@RequestMapping("/api/purezas")
@Tag(name = "Pureza", description = "API para gestión del análisis de pureza")
@SecurityRequirement(name = "bearerAuth")
public class PurezaController {

    @Autowired
    private PurezaService purezaService;

    // Crear nueva Pureza
    @Operation(summary = "Crear análisis de pureza", 
              description = "Crea un nuevo análisis de pureza con numeroRepeticiones y numeroConteos definidos")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PurezaDTO> crearPureza(@RequestBody PurezaRequestDTO solicitud) {
        PurezaDTO purezaCreada = purezaService.crearPureza(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(purezaCreada);
    }

    // Obtener todas las Purezas activas
    @Operation(summary = "Listar todas las purezas", 
              description = "Obtiene todos los análisis de pureza activos")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<ResponseListadoPureza> obtenerTodasPurezasActivas() {
        ResponseListadoPureza respuesta = purezaService.obtenerTodasPurezasActivas();
        return ResponseEntity.ok(respuesta);
    }

    // Obtener Pureza por ID
    @Operation(summary = "Obtener pureza por ID", 
              description = "Obtiene un análisis de pureza específico por su ID")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<PurezaDTO> obtenerPurezaPorId(@PathVariable Long id) {
        PurezaDTO pureza = purezaService.obtenerPurezaPorId(id);
        return ResponseEntity.ok(pureza);
    }

    // Actualizar Pureza
    @Operation(summary = "Actualizar análisis de pureza", 
              description = "Actualiza un análisis de pureza existente")    
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PurezaDTO> actualizarPureza(@PathVariable Long id, @RequestBody PurezaRequestDTO solicitud) {
        PurezaDTO purezaActualizada = purezaService.actualizarPureza(id, solicitud);
        return ResponseEntity.ok(purezaActualizada);
    }

    // Eliminar Pureza (cambiar estado a INACTIVO)
    @Operation(summary = "Eliminar análisis de pureza", 
              description = "Elimina un análisis de pureza cambiando su estado a INACTIVO")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPureza(@PathVariable Long id) {
        purezaService.eliminarPureza(id);
        return ResponseEntity.noContent().build();
    }

    // Desactivar Pureza (soft delete)
    @Operation(summary = "Desactivar análisis de pureza", 
              description = "Desactiva un análisis de pureza (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarPureza(@PathVariable Long id) {
        purezaService.desactivarPureza(id);
        return ResponseEntity.ok().build();
    }

    // Reactivar Pureza
    @Operation(summary = "Reactivar análisis de pureza", 
              description = "Reactiva un análisis de pureza desactivado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<PurezaDTO> reactivarPureza(@PathVariable Long id) {
        PurezaDTO purezaReactivada = purezaService.reactivarPureza(id);
        return ResponseEntity.ok(purezaReactivada);
    }

    // Obtener Purezas por Lote
    @Operation(summary = "Obtener purezas por lote", 
              description = "Obtiene todos los análisis de pureza asociados a un lote específico")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<PurezaDTO>> obtenerPurezasPorIdLote(@PathVariable Long idLote) {
        List<PurezaDTO> purezas = purezaService.obtenerPurezasPorIdLote(idLote);
        return ResponseEntity.ok(purezas);
    }

    // Obtener Purezas con paginado para listado
    @Operation(summary = "Obtener purezas paginadas", 
              description = "Obtiene la lista paginada de análisis de pureza para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaListadoDTO>> obtenerPurezaPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "todos") String filtroActivo) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaListadoDTO> response = 
            purezaService.obtenerPurezaPaginadasConFiltro(pageable, filtroActivo);
        return ResponseEntity.ok(response);
    }

    // Obtener todos los catálogos para el select de otras semillas
    @Operation(summary = "Listar todos los catálogos", 
              description = "Obtiene todos los catálogos necesarios para el análisis de pureza")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/catalogos")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerTodosCatalogos() {
        List<MalezasYCultivosCatalogoDTO> catalogos = purezaService.obtenerTodosCatalogos();
        return ResponseEntity.ok(catalogos);
    }

    // Finalizar análisis de pureza
    @Operation(summary = "Finalizar análisis de pureza", 
              description = "Finaliza un análisis de pureza cambiando su estado a FINALIZADO")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<PurezaDTO> finalizarAnalisis(@PathVariable Long id) {
        PurezaDTO analisisFinalizado = purezaService.finalizarAnalisis(id);
        return ResponseEntity.ok(analisisFinalizado);
    }

    // Aprobar análisis (solo admin)
    @Operation(summary = "Aprobar análisis de pureza", 
              description = "Aprueba un análisis de pureza, cambiando su estado a APROBADO (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<PurezaDTO> aprobarAnalisis(@PathVariable Long id) {
        PurezaDTO analisisAprobado = purezaService.aprobarAnalisis(id);
        return ResponseEntity.ok(analisisAprobado);
    }

    // Marcar análisis para repetir (solo admin)
    @Operation(summary = "Marcar análisis de pureza para repetir", 
              description = "Marca un análisis de pureza para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<PurezaDTO> marcarParaRepetir(@PathVariable Long id) {
        PurezaDTO analisisRepetir = purezaService.marcarParaRepetir(id);
        return ResponseEntity.ok(analisisRepetir);
    }
    }

