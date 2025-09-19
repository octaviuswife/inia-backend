package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.MalezasYCultivosCatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasYCultivosCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.MalezasYCultivosCatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/malezas-cultivos")
@CrossOrigin(origins = "*")
public class MalezasYCultivosCatalogoController {

    @Autowired
    private MalezasYCultivosCatalogoService service;

    // Obtener todos activos
    @GetMapping
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerTodos() {
        List<MalezasYCultivosCatalogoDTO> catalogos = service.obtenerTodos();
        return ResponseEntity.ok(catalogos);
    }

    // Obtener inactivos
    @GetMapping("/inactivos")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerInactivos() {
        List<MalezasYCultivosCatalogoDTO> catalogos = service.obtenerInactivos();
        return ResponseEntity.ok(catalogos);
    }

    // Obtener malezas
    @GetMapping("/malezas")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerMalezas() {
        List<MalezasYCultivosCatalogoDTO> malezas = service.obtenerPorTipo(true);
        return ResponseEntity.ok(malezas);
    }

    // Obtener cultivos
    @GetMapping("/cultivos")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerCultivos() {
        List<MalezasYCultivosCatalogoDTO> cultivos = service.obtenerPorTipo(false);
        return ResponseEntity.ok(cultivos);
    }

    // Buscar por nombre común
    @GetMapping("/buscar/comun")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> buscarPorNombreComun(@RequestParam String nombre) {
        List<MalezasYCultivosCatalogoDTO> resultados = service.buscarPorNombreComun(nombre);
        return ResponseEntity.ok(resultados);
    }

    // Buscar por nombre científico
    @GetMapping("/buscar/cientifico")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> buscarPorNombreCientifico(@RequestParam String nombre) {
        List<MalezasYCultivosCatalogoDTO> resultados = service.buscarPorNombreCientifico(nombre);
        return ResponseEntity.ok(resultados);
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<MalezasYCultivosCatalogoDTO> obtenerPorId(@PathVariable Long id) {
        MalezasYCultivosCatalogoDTO catalogo = service.obtenerPorId(id);
        if (catalogo != null) {
            return ResponseEntity.ok(catalogo);
        }
        return ResponseEntity.notFound().build();
    }

    // Crear
    @PostMapping
    public ResponseEntity<MalezasYCultivosCatalogoDTO> crear(@RequestBody MalezasYCultivosCatalogoRequestDTO solicitud) {
        MalezasYCultivosCatalogoDTO creado = service.crear(solicitud);
        return ResponseEntity.ok(creado);
    }

    // Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<MalezasYCultivosCatalogoDTO> actualizar(@PathVariable Long id, @RequestBody MalezasYCultivosCatalogoRequestDTO solicitud) {
        MalezasYCultivosCatalogoDTO actualizado = service.actualizar(id, solicitud);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Eliminar (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }

    // Reactivar
    @PutMapping("/{id}/reactivar")
    public ResponseEntity<MalezasYCultivosCatalogoDTO> reactivar(@PathVariable Long id) {
        MalezasYCultivosCatalogoDTO reactivado = service.reactivar(id);
        if (reactivado != null) {
            return ResponseEntity.ok(reactivado);
        }
        return ResponseEntity.notFound().build();
    }
}