package utec.proyectofinal.Proyecto.Final.UTEC.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.dto.CatalogoLoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.business.service.CatalogoLoteService;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos-lote")
@CrossOrigin(origins = "*")
public class CatalogoLoteController {

    @Autowired
    private CatalogoLoteService catalogoLoteService;

    @GetMapping
    public ResponseEntity<List<CatalogoLoteDTO>> obtenerTodos() {
        List<CatalogoLoteDTO> catalogos = catalogoLoteService.obtenerTodos();
        return ResponseEntity.ok(catalogos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CatalogoLoteDTO> obtenerPorId(@PathVariable Long id) {
        CatalogoLoteDTO catalogo = catalogoLoteService.obtenerPorId(id);
        return catalogo != null ? ResponseEntity.ok(catalogo) : ResponseEntity.notFound().build();
    }

    @GetMapping("/humedad-datos")
    public ResponseEntity<List<String>> obtenerDatosHumedad() {
        List<String> datos = catalogoLoteService.obtenerDatosHumedad();
        return ResponseEntity.ok(datos);
    }

    @GetMapping("/numeros-articulo")
    public ResponseEntity<List<String>> obtenerNumerosArticulo() {
        List<String> numeros = catalogoLoteService.obtenerNumerosArticulo();
        return ResponseEntity.ok(numeros);
    }

    @PostMapping
    public ResponseEntity<CatalogoLoteDTO> crear(@RequestBody CatalogoLoteDTO dto) {
        CatalogoLoteDTO creado = catalogoLoteService.guardar(dto);
        return ResponseEntity.ok(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CatalogoLoteDTO> actualizar(@PathVariable Long id, @RequestBody CatalogoLoteDTO dto) {
        CatalogoLoteDTO actualizado = catalogoLoteService.actualizar(id, dto);
        return actualizado != null ? ResponseEntity.ok(actualizado) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        catalogoLoteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}