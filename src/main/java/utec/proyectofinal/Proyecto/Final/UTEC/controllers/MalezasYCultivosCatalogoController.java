package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.MalezasYCultivosCatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasYCultivosCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoMYCCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.services.MalezasYCultivosCatalogoService;

import java.util.List;

@RestController
@RequestMapping("/api/malezas-cultivos")
@CrossOrigin(origins = "*")
@Tag(name = "Malezas y Cultivos", description = "API para gestión del catálogo de malezas y cultivos")
@SecurityRequirement(name = "bearerAuth")
public class MalezasYCultivosCatalogoController {

    @Autowired
    private MalezasYCultivosCatalogoService service;

    // Obtener todos activos
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar malezas y cultivos activos", description = "Obtiene todos los registros activos del catálogo")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerTodos() {
        List<MalezasYCultivosCatalogoDTO> catalogos = service.obtenerTodos();
        return ResponseEntity.ok(catalogos);
    }

    // Obtener inactivos
    @GetMapping("/inactivos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar malezas y cultivos inactivos", description = "Obtiene todos los registros inactivos del catálogo")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerInactivos() {
        List<MalezasYCultivosCatalogoDTO> catalogos = service.obtenerInactivos();
        return ResponseEntity.ok(catalogos);
    }

    // Obtener malezas
    @GetMapping("/malezas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar malezas", description = "Obtiene todas las malezas del catálogo")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerMalezas() {
        List<MalezasYCultivosCatalogoDTO> malezas = service.obtenerPorTipo(TipoMYCCatalogo.MALEZA);
        return ResponseEntity.ok(malezas);
    }

    // Obtener cultivos
    @GetMapping("/cultivos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar cultivos", description = "Obtiene todos los cultivos del catálogo")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerCultivos() {
        List<MalezasYCultivosCatalogoDTO> cultivos = service.obtenerPorTipo(TipoMYCCatalogo.CULTIVO);
        return ResponseEntity.ok(cultivos);
    }

    // Obtener brassicas
    @GetMapping("/brassicas")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar brassicas", description = "Obtiene todas las brassicas del catálogo")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerBrassicas() {
        List<MalezasYCultivosCatalogoDTO> brassicas = service.obtenerPorTipo(TipoMYCCatalogo.BRASSICA);
        return ResponseEntity.ok(brassicas);
    }

    // Obtener por tipo específico
    @GetMapping("/tipo/{tipoEspecie}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Listar por tipo de especie", description = "Obtiene elementos del catálogo por tipo de especie")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> obtenerPorTipo(@PathVariable TipoMYCCatalogo tipoMYCCatalogo) {
        List<MalezasYCultivosCatalogoDTO> elementos = service.obtenerPorTipo(tipoMYCCatalogo);
        return ResponseEntity.ok(elementos);
    }

    // Buscar por nombre común
    @GetMapping("/buscar/comun")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Buscar por nombre común", description = "Busca elementos en el catálogo por nombre común")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> buscarPorNombreComun(@RequestParam String nombre) {
        List<MalezasYCultivosCatalogoDTO> resultados = service.buscarPorNombreComun(nombre);
        return ResponseEntity.ok(resultados);
    }

    // Buscar por nombre científico
    @GetMapping("/buscar/cientifico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Buscar por nombre científico", description = "Busca elementos en el catálogo por nombre científico")
    public ResponseEntity<List<MalezasYCultivosCatalogoDTO>> buscarPorNombreCientifico(@RequestParam String nombre) {
        List<MalezasYCultivosCatalogoDTO> resultados = service.buscarPorNombreCientifico(nombre);
        return ResponseEntity.ok(resultados);
    }

    // Obtener por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ANALISTA') or hasRole('OBSERVADOR')")
    @Operation(summary = "Obtener por ID", description = "Obtiene un elemento del catálogo por su ID")
    public ResponseEntity<MalezasYCultivosCatalogoDTO> obtenerPorId(@PathVariable Long id) {
        MalezasYCultivosCatalogoDTO catalogo = service.obtenerPorId(id);
        if (catalogo != null) {
            return ResponseEntity.ok(catalogo);
        }
        return ResponseEntity.notFound().build();
    }

    // Crear
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear elemento de catálogo", description = "Crea un nuevo elemento en el catálogo de malezas y cultivos")
    public ResponseEntity<MalezasYCultivosCatalogoDTO> crear(@RequestBody MalezasYCultivosCatalogoRequestDTO solicitud) {
        MalezasYCultivosCatalogoDTO creado = service.crear(solicitud);
        return ResponseEntity.ok(creado);
    }

    // Actualizar
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar elemento de catálogo", description = "Actualiza un elemento existente en el catálogo")
    public ResponseEntity<MalezasYCultivosCatalogoDTO> actualizar(@PathVariable Long id, @RequestBody MalezasYCultivosCatalogoRequestDTO solicitud) {
        MalezasYCultivosCatalogoDTO actualizado = service.actualizar(id, solicitud);
        if (actualizado != null) {
            return ResponseEntity.ok(actualizado);
        }
        return ResponseEntity.notFound().build();
    }

    // Eliminar (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar elemento de catálogo", description = "Desactiva un elemento del catálogo (eliminación lógica)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok().build();
    }

    // Reactivar
    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar elemento de catálogo", description = "Reactiva un elemento previamente desactivado")
    public ResponseEntity<MalezasYCultivosCatalogoDTO> reactivar(@PathVariable Long id) {
        MalezasYCultivosCatalogoDTO reactivado = service.reactivar(id);
        if (reactivado != null) {
            return ResponseEntity.ok(reactivado);
        }
        return ResponseEntity.notFound().build();
    }
}