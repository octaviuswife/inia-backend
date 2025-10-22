package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionEditRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoGerminacion;
import utec.proyectofinal.Proyecto.Final.UTEC.services.GerminacionService;

// CORS configurado globalmente en WebSecurityConfig
@RestController
@RequestMapping("/api/germinaciones")
@Tag(name = "Germinación", description = "API para gestión del análisis de germinación")
@SecurityRequirement(name = "bearerAuth")
public class GerminacionController {

    @Autowired
    private GerminacionService germinacionService;

    // Crear nueva Germinación
    @Operation(summary = "Crear análisis de germinación", 
              description = "Crea un nuevo análisis de germinación con numeroRepeticiones y numeroConteos definidos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Germinación creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
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
    @Operation(summary = "Obtener todas las germinaciones", 
              description = "Obtiene la lista de todos los análisis de germinación activos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<ResponseListadoGerminacion> obtenerTodasGerminaciones() {
        try {
            ResponseListadoGerminacion response = germinacionService.obtenerTodasGerminaciones();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener germinaciones con paginado para listado
    @Operation(summary = "Obtener germinaciones paginadas", 
              description = "Obtiene la lista paginada de análisis de germinación para el listado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista paginada obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<Page<GerminacionListadoDTO>> obtenerGerminacionesPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "todos") String filtroActivo) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<GerminacionListadoDTO> response = germinacionService.obtenerGerminacionesPaginadasConFiltro(pageable, filtroActivo);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Germinación por ID
    @Operation(summary = "Obtener germinación por ID", 
              description = "Obtiene los detalles de un análisis de germinación específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Germinación encontrada"),
        @ApiResponse(responseCode = "404", description = "Germinación no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Actualizar germinación", 
              description = "Actualiza solo los campos editables de una germinación existente. " +
                          "Las fechas no se pueden modificar por seguridad.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Germinación actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Germinación no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GerminacionDTO> actualizarGerminacion(@PathVariable Long id, @RequestBody GerminacionEditRequestDTO dto) {
        try {
            GerminacionDTO germinacionActualizada = germinacionService.actualizarGerminacionSeguro(id, dto);
            return new ResponseEntity<>(germinacionActualizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Germinación (cambiar estado a INACTIVO)
    @Operation(summary = "Eliminar germinación", description = "Cambia el estado de la germinación a INACTIVO")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
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

    // Desactivar Germinacion (soft delete)
    @Operation(summary = "Desactivar análisis Germinación", 
              description = "Desactiva un análisis de Germinación (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<HttpStatus> desactivarGerminacion(@PathVariable Long id) {
        try {
            germinacionService.desactivarGerminacion(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Reactivar Germinacion
    @Operation(summary = "Reactivar análisis Germinación", 
              description = "Reactiva un análisis de Germinación desactivado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivarGerminacion(@PathVariable Long id) {
        try {
            GerminacionDTO germinacionReactivada = germinacionService.reactivarGerminacion(id);
            return ResponseEntity.ok(germinacionReactivada);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    // Obtener Germinaciones por Lote
    @Operation(summary = "Obtener germinaciones por lote", description = "Devuelve una lista de germinaciones asociadas a un lote específico")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<GerminacionDTO>> obtenerGerminacionesPorIdLote(@PathVariable Long idLote) {
        try {
            List<GerminacionDTO> germinaciones = germinacionService.obtenerGerminacionesPorIdLote(idLote);
            return new ResponseEntity<>(germinaciones, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Finalizar análisis de germinación
    @Operation(summary = "Finalizar análisis de germinación", description = "Marca el análisis como FINALIZADO, impidiendo más modificaciones")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<GerminacionDTO> finalizarAnalisis(@PathVariable Long id) {
        try {
            GerminacionDTO analisisFinalizado = germinacionService.finalizarAnalisis(id);
            return new ResponseEntity<>(analisisFinalizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Aprobar análisis (solo admin)
    @Operation(summary = "Aprobar análisis de germinación", description = "Solo administradores pueden aprobar análisis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Análisis aprobado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error en la aprobación"),
        @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<GerminacionDTO> aprobarAnalisis(@PathVariable Long id) {
        try {
            GerminacionDTO analisisAprobado = germinacionService.aprobarAnalisis(id);
            return new ResponseEntity<>(analisisAprobado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Marcar análisis para repetir (solo admin)
    @Operation(summary = "Marcar análisis de germinación para repetir", 
              description = "Marca un análisis de germinación para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<GerminacionDTO> marcarParaRepetir(@PathVariable Long id) {
        try {
            GerminacionDTO analisisRepetir = germinacionService.marcarParaRepetir(id);
            return new ResponseEntity<>(analisisRepetir, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}