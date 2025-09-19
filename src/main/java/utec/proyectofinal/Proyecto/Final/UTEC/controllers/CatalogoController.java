package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.CatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
@CrossOrigin(origins = "*")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    // Obtener todos los catálogos activos
    @GetMapping
    public ResponseEntity<List<CatalogoDTO>> obtenerTodos() {
        List<CatalogoDTO> catalogos = catalogoService.obtenerTodos();
        return ResponseEntity.ok(catalogos);
    }

    // Obtener catálogos por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<CatalogoDTO>> obtenerPorTipo(@PathVariable String tipo) {
        try {
            List<CatalogoDTO> catalogos = catalogoService.obtenerPorTipo(tipo);
            return ResponseEntity.ok(catalogos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener catálogo por ID
    @GetMapping("/{id}")
    public ResponseEntity<CatalogoDTO> obtenerPorId(@PathVariable Long id) {
        CatalogoDTO catalogo = catalogoService.obtenerPorId(id);
        if (catalogo != null) {
            return ResponseEntity.ok(catalogo);
        }
        return ResponseEntity.notFound().build();
    }

    // Crear nuevo catálogo
    @PostMapping
    public ResponseEntity<CatalogoDTO> crear(@RequestBody CatalogoRequestDTO solicitud) {
        try {
            CatalogoDTO creado = catalogoService.crear(solicitud);
            return ResponseEntity.ok(creado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Actualizar catálogo
    @PutMapping("/{id}")
    public ResponseEntity<CatalogoDTO> actualizar(@PathVariable Long id, @RequestBody CatalogoRequestDTO solicitud) {
        try {
            CatalogoDTO actualizado = catalogoService.actualizar(id, solicitud);
            if (actualizado != null) {
                return ResponseEntity.ok(actualizado);
            }
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Desactivar catálogo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        catalogoService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    // Eliminar físicamente (solo para casos especiales)
    @DeleteMapping("/{id}/fisico")
    public ResponseEntity<Void> eliminarFisicamente(@PathVariable Long id) {
        catalogoService.eliminarFisicamente(id);
        return ResponseEntity.ok().build();
    }

    // Endpoints específicos para obtener tipos de datos
    @GetMapping("/humedad")
    public ResponseEntity<List<CatalogoDTO>> obtenerTiposHumedad() {
        return obtenerPorTipo("HUMEDAD");
    }

    @GetMapping("/articulos")
    public ResponseEntity<List<CatalogoDTO>> obtenerNumerosArticulo() {
        return obtenerPorTipo("ARTICULO");
    }

    @GetMapping("/origenes")
    public ResponseEntity<List<CatalogoDTO>> obtenerOrigenes() {
        return obtenerPorTipo("ORIGEN");
    }

    @GetMapping("/estados")
    public ResponseEntity<List<CatalogoDTO>> obtenerEstados() {
        return obtenerPorTipo("ESTADO");
    }

    @GetMapping("/depositos")
    public ResponseEntity<List<CatalogoDTO>> obtenerDepositos() {
        return obtenerPorTipo("DEPOSITO");
    }
}