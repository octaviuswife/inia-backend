package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.services.LegadoService;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoSimpleDTO;

import java.util.List;

/**
 * Controlador para gestión de datos legados
 */
@RestController
@RequestMapping("/api/legados")
@Tag(name = "Legados", description = "API para gestión de datos históricos legados")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class LegadoController {

    private final LegadoService legadoService;

    /**
     * Obtener todos los registros legados (versión simple)
     */
    @GetMapping
    @Operation(summary = "Listar legados", description = "Obtiene todos los registros legados activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<List<LegadoSimpleDTO>> listarTodos() {
        try {
            List<LegadoSimpleDTO> legados = legadoService.obtenerTodosSimple();
            return ResponseEntity.ok(legados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener un legado por ID con información completa
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener legado", description = "Obtiene un registro legado por ID con información completa del lote")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<LegadoDTO> obtenerPorId(@PathVariable Long id) {
        try {
            LegadoDTO legado = legadoService.obtenerPorId(id);
            return ResponseEntity.ok(legado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener legados por archivo origen
     */
    @GetMapping("/archivo/{nombreArchivo}")
    @Operation(summary = "Buscar por archivo", description = "Obtiene registros legados de un archivo específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<List<LegadoSimpleDTO>> obtenerPorArchivo(@PathVariable String nombreArchivo) {
        try {
            List<LegadoSimpleDTO> legados = legadoService.obtenerPorArchivo(nombreArchivo);
            return ResponseEntity.ok(legados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener legados por ficha
     */
    @GetMapping("/ficha/{ficha}")
    @Operation(summary = "Buscar por ficha", description = "Obtiene registros legados de una ficha específica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    public ResponseEntity<List<LegadoSimpleDTO>> obtenerPorFicha(@PathVariable String ficha) {
        try {
            List<LegadoSimpleDTO> legados = legadoService.obtenerPorFicha(ficha);
            return ResponseEntity.ok(legados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Desactivar un registro legado
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar legado", description = "Desactiva un registro legado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        try {
            legadoService.desactivar(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
