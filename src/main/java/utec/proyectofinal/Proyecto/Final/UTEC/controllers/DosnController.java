package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DosnService;

@RestController
@RequestMapping("/api/dosn")
@Tag(name = "Determinacion de Otras Semillas en Número (DOSN)", description = "API para gestión de DOSN")
@SecurityRequirement(name = "bearerAuth")
public class DosnController {

    @Autowired
    private DosnService dosnService;

    // Crear nueva Dosn
    @Operation(summary = "Crear declaración de origen y sanidad (DOSN)", 
              description = "Crea una nueva declaración de origen y sanidad (DOSN)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PostMapping
    public ResponseEntity<DosnDTO> crearDosn(@RequestBody DosnRequestDTO solicitud) {
        try {
            DosnDTO dosnCreado = dosnService.crearDosn(solicitud);
            return new ResponseEntity<>(dosnCreado, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las Dosn activas
    @Operation(summary = "Listar todos los DOSN", 
              description = "Obtiene todos los DOSN activas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping
    public ResponseEntity<ResponseListadoDosn> obtenerTodasDosnActivas() {
        try {
            ResponseListadoDosn respuesta = dosnService.obtenerTodasDosnActivas();
            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Dosn por ID
    @Operation(summary = "Obtener declaración de origen y sanidad (DOSN) por ID", 
              description = "Obtiene una declaración de origen y sanidad (DOSN) específica por su ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<DosnDTO> obtenerDosnPorId(@PathVariable Long id) {
        try {
            DosnDTO dosn = dosnService.obtenerDosnPorId(id);
            return new ResponseEntity<>(dosn, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar Dosn
    @Operation(summary = "Actualizar declaración de origen y sanidad (DOSN)", 
              description = "Actualiza una declaración de origen y sanidad (DOSN) existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{id}")
    public ResponseEntity<DosnDTO> actualizarDosn(@PathVariable Long id, @RequestBody DosnRequestDTO solicitud) {
        try {
            DosnDTO dosnActualizado = dosnService.actualizarDosn(id, solicitud);
            return new ResponseEntity<>(dosnActualizado, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar Dosn (cambiar estado a INACTIVO)
    @Operation(summary = "Eliminar análisis de DOSN", 
              description = "Elimina una declaración de origen y sanidad (DOSN) cambiando su estado a INACTIVO")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarDosn(@PathVariable Long id) {
        try {
            dosnService.eliminarDosn(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener Dosn por Lote
    @Operation(summary = "Obtener DOSN por ID de lote", 
              description = "Obtiene todos los DOSN asociados a un lote específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/lote/{idLote}")
    public ResponseEntity<List<DosnDTO>> obtenerDosnPorIdLote(@PathVariable Integer idLote) {
        try {
            List<DosnDTO> dosn = dosnService.obtenerDosnPorIdLote(idLote);
            return new ResponseEntity<>(dosn, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Obtener DOSN con paginado para listado
    @Operation(summary = "Obtener DOSN paginadas", 
              description = "Obtiene la lista paginada de análisis de DOSN para el listado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @GetMapping("/listado")
    public ResponseEntity<org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnListadoDTO>> obtenerDosnPaginadas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size) {
        try {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
            org.springframework.data.domain.Page<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnListadoDTO> response = dosnService.obtenerDosnPaginadas(pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Finalizar análisis de DOSN
    @Operation(summary = "Finalizar análisis de DOSN", 
              description = "Finaliza un DOSN según el rol del usuario")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA')")
    @PutMapping("/{id}/finalizar")
    public ResponseEntity<DosnDTO> finalizarAnalisis(@PathVariable Long id) {
        try {
            DosnDTO dosnFinalizada = dosnService.finalizarAnalisis(id);
            return new ResponseEntity<>(dosnFinalizada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Aprobar análisis de DOSN (solo admin)
    @Operation(summary = "Aprobar análisis de DOSN", 
              description = "Aprueba un DOSN - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<DosnDTO> aprobarAnalisis(@PathVariable Long id) {
        try {
            DosnDTO dosnAprobada = dosnService.aprobarAnalisis(id);
            return new ResponseEntity<>(dosnAprobada, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Marcar análisis para repetir (solo admin)
    @Operation(summary = "Marcar análisis de DOSN para repetir", 
              description = "Marca un DOSN para repetir - solo administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/repetir")
    public ResponseEntity<DosnDTO> marcarParaRepetir(@PathVariable Long id) {
        try {
            DosnDTO dosnRepetir = dosnService.marcarParaRepetir(id);
            return new ResponseEntity<>(dosnRepetir, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
