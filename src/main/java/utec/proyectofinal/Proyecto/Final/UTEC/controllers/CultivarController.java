package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CultivarRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CultivarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.CultivarService;

import java.util.List;

@RestController
@RequestMapping("/api/cultivar")
@CrossOrigin(origins = "*")
public class CultivarController {

    @Autowired
    private CultivarService cultivarService;

    // Obtener todos activos
    @GetMapping
    public ResponseEntity<List<CultivarDTO>> obtenerTodos() {
        List<CultivarDTO> cultivares = cultivarService.obtenerTodos();
        return ResponseEntity.ok(cultivares);
    }

    // Obtener inactivos
    @GetMapping("/inactivos")
    public ResponseEntity<List<CultivarDTO>> obtenerInactivos() {
        List<CultivarDTO> cultivares = cultivarService.obtenerInactivos();
        return ResponseEntity.ok(cultivares);
    }

    // Obtener por especie
    @GetMapping("/especie/{especieID}")
    public ResponseEntity<List<CultivarDTO>> obtenerPorEspecie(@PathVariable Long especieID) {
        List<CultivarDTO> cultivares = cultivarService.obtenerPorEspecie(especieID);
        return ResponseEntity.ok(cultivares);
    }

    // Buscar por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<CultivarDTO>> buscarPorNombre(@RequestParam String nombre) {
        List<CultivarDTO> cultivares = cultivarService.buscarPorNombre(nombre);
        return ResponseEntity.ok(cultivares);
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<CultivarDTO> obtenerPorId(@PathVariable Long id) {
        CultivarDTO cultivar = cultivarService.obtenerPorId(id);
        if (cultivar != null) {
            return ResponseEntity.ok(cultivar);
        }
        return ResponseEntity.notFound().build();
    }

    // Crear
    @PostMapping
    public ResponseEntity<CultivarDTO> crear(@RequestBody CultivarRequestDTO solicitud) {
        try {
            CultivarDTO creado = cultivarService.crear(solicitud);
            return ResponseEntity.ok(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<CultivarDTO> actualizar(@PathVariable Long id, @RequestBody CultivarRequestDTO solicitud) {
        try {
            CultivarDTO actualizado = cultivarService.actualizar(id, solicitud);
            if (actualizado != null) {
                return ResponseEntity.ok(actualizado);
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Eliminar (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        cultivarService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    // Reactivar
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<CultivarDTO> reactivar(@PathVariable Long id) {
        CultivarDTO reactivado = cultivarService.reactivar(id);
        if (reactivado != null) {
            return ResponseEntity.ok(reactivado);
        }
        return ResponseEntity.notFound().build();
    }
}