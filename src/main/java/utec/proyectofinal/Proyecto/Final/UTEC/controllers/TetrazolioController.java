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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeadosRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoTetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.services.TetrazolioService;

// CORS configurado globalmente en WebSecurityConfig
@RestController
@RequestMapping("/api/tetrazolios")
@Tag(name = "Tetrazolio", description = "API para gestión del análisis de tetrazolio")
@SecurityRequirement(name = "bearerAuth")
public class TetrazolioController {

    @Autowired
    private TetrazolioService tetrazolioService;

    // Crear nuevo Tetrazolio
    @Operation(summary = "Crear análisis de tetrazolio", 
              description = "Crea un nuevo análisis de tetrazolio con numeroRepeticiones y numeroConteos definidos")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
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
    @Operation(summary = "Listar todos los análisis de tetrazolio", 
              description = "Obtiene todos los análisis de tetrazolio activos en el sistema")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Obtener análisis de tetrazolio por ID", 
              description = "Obtiene un análisis de tetrazolio específico por su ID")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
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
    @Operation(summary = "Actualizar análisis de tetrazolio", 
              description = "Actualiza los detalles de un análisis de tetrazolio existente")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
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

    // Actualizar porcentajes redondeados (solo cuando todas las repeticiones estén completas)
    @Operation(summary = "Actualizar porcentajes redondeados")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/porcentajes")
    public ResponseEntity<TetrazolioDTO> actualizarPorcentajesRedondeados(@PathVariable Long id, @RequestBody PorcentajesRedondeadosRequestDTO solicitud) {
        try {
            TetrazolioDTO tetrazolioActualizado = tetrazolioService.actualizarPorcentajesRedondeados(id, solicitud);
            return new ResponseEntity<>(tetrazolioActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            System.err.println("Error al actualizar porcentajes: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error interno al actualizar porcentajes: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Tetrazolio (cambiar estado a INACTIVO)
    @Operation(summary = "Eliminar análisis de tetrazolio")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarTetrazolio(@PathVariable Long id) {
        try {
            tetrazolioService.desactivarTetrazolio(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Desactivar Tetrazolio (soft delete)
    @Operation(summary = "Desactivar análisis Tetrazolio", 
              description = "Desactiva un análisis Tetrazolio (cambiar activo a false)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<HttpStatus> desactivarTetrazolio(@PathVariable Long id) {
        try {
            tetrazolioService.desactivarTetrazolio(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Reactivar Tetrazolio
    @Operation(summary = "Reactivar análisis Tetrazolio", 
              description = "Reactiva un análisis Tetrazolio desactivado (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivarTetrazolio(@PathVariable Long id) {
        try {
            TetrazolioDTO tetrazolioReactivado = tetrazolioService.reactivarTetrazolio(id);
            return ResponseEntity.ok(tetrazolioReactivado);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }

    // Obtener Tetrazolios por Lote
    @Operation(summary = "Obtener tetrazolios por lote", 
              description = "Obtiene todos los análisis de tetrazolio asociados a un lote específico")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<TetrazolioDTO>> obtenerTetrazoliosPorIdLote(@PathVariable Long idLote) {
        try {
            List<TetrazolioDTO> tetrazolios = tetrazolioService.obtenerTetrazoliosPorIdLote(idLote);
            return new ResponseEntity<>(tetrazolios, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Tetrazolios con paginado para listado
    @Operation(summary = "Obtener tetrazolios paginadas", 
              description = "Obtiene la lista paginada de análisis de tetrazolio para el listado")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioListadoDTO>> obtenerTetrazoliosPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "todos") String filtroActivo) {
        try {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
            org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioListadoDTO> response = tetrazolioService.obtenerTetrazoliosPaginadasConFiltro(pageable, filtroActivo);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Finalizar análisis de tetrazolio
    @Operation(summary = "Finalizar análisis de tetrazolio", 
              description = "Finaliza un análisis de tetrazolio cambiando su estado según el rol del usuario (analista -> PENDIENTE_APROBACION, admin -> APROBADO)")
    @PreAuthorize("hasRole('ANALISTA') or hasRole('ADMIN')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<TetrazolioDTO> finalizarAnalisis(@PathVariable Long id) {
        try {
            TetrazolioDTO tetrazolioFinalizado = tetrazolioService.finalizarAnalisis(id);
            return new ResponseEntity<>(tetrazolioFinalizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Aprobar análisis (solo admin)
    @Operation(summary = "Aprobar análisis de tetrazolio", 
              description = "Aprueba un análisis de tetrazolio, cambiando su estado a APROBADO (solo administradores)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<?> aprobarAnalisis(@PathVariable Long id) {
        try {
            TetrazolioDTO tetrazolioAprobado = tetrazolioService.aprobarAnalisis(id);
            return new ResponseEntity<>(tetrazolioAprobado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }

    // Marcar análisis para repetir (solo admin)
    @Operation(summary = "Marcar análisis de tetrazolio para repetir", 
              description = "Marca un análisis de tetrazolio para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<?> marcarParaRepetir(@PathVariable Long id) {
        try {
            TetrazolioDTO tetrazolioRepetir = tetrazolioService.marcarParaRepetir(id);
            return new ResponseEntity<>(tetrazolioRepetir, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }
}