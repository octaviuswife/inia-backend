package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.EspecieRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.EspecieService;

import java.util.List;

@RestController
@RequestMapping("/api/especie")
@CrossOrigin(origins = "*")
public class EspecieController {

    @Autowired
    private EspecieService especieService;

    // Obtener todas activas
    @GetMapping
    public ResponseEntity<List<EspecieDTO>> obtenerTodas() {
        List<EspecieDTO> especies = especieService.obtenerTodas();
        return ResponseEntity.ok(especies);
    }

    // Obtener inactivas
    @GetMapping("/inactivas")
    public ResponseEntity<List<EspecieDTO>> obtenerInactivas() {
        List<EspecieDTO> especies = especieService.obtenerInactivas();
        return ResponseEntity.ok(especies);
    }

    // Buscar por nombre común
    @GetMapping("/buscar/comun")
    public ResponseEntity<List<EspecieDTO>> buscarPorNombreComun(@RequestParam String nombre) {
        List<EspecieDTO> especies = especieService.buscarPorNombreComun(nombre);
        return ResponseEntity.ok(especies);
    }

    // Buscar por nombre científico
    @GetMapping("/buscar/cientifico")
    public ResponseEntity<List<EspecieDTO>> buscarPorNombreCientifico(@RequestParam String nombre) {
        List<EspecieDTO> especies = especieService.buscarPorNombreCientifico(nombre);
        return ResponseEntity.ok(especies);
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<EspecieDTO> obtenerPorId(@PathVariable Long id) {
        EspecieDTO especie = especieService.obtenerPorId(id);
        if (especie != null) {
            return ResponseEntity.ok(especie);
        }
        return ResponseEntity.notFound().build();
    }

    // Crear
    @PostMapping
    public ResponseEntity<EspecieDTO> crear(@RequestBody EspecieRequestDTO solicitud) {
        EspecieDTO creada = especieService.crear(solicitud);
        return ResponseEntity.ok(creada);
    }

    // Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<EspecieDTO> actualizar(@PathVariable Long id, @RequestBody EspecieRequestDTO solicitud) {
        EspecieDTO actualizada = especieService.actualizar(id, solicitud);
        if (actualizada != null) {
            return ResponseEntity.ok(actualizada);
        }
        return ResponseEntity.notFound().build();
    }

    // Eliminar (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        especieService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    // Reactivar
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<EspecieDTO> reactivar(@PathVariable Long id) {
        EspecieDTO reactivada = especieService.reactivar(id);
        if (reactivada != null) {
            return ResponseEntity.ok(reactivada);
        }
        return ResponseEntity.notFound().build();
    }
}