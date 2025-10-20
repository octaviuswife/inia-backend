package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CatalogoCrudRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;

@Service
public class CatalogoService {

    @Autowired
    private CatalogoCrudRepository catalogoRepository;

    // Obtener todos los catálogos activos
    public List<CatalogoDTO> obtenerTodos() {
        return catalogoRepository.findByActivoTrue().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener catálogos por tipo
    public List<CatalogoDTO> obtenerPorTipo(String tipo) {
        TipoCatalogo tipoCatalogo = TipoCatalogo.valueOf(tipo.toUpperCase());
        return catalogoRepository.findByTipoAndActivoTrue(tipoCatalogo).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener catálogos por tipo con filtro de estado opcional
    public List<CatalogoDTO> obtenerPorTipo(String tipo, Boolean activo) {
        TipoCatalogo tipoCatalogo = TipoCatalogo.valueOf(tipo.toUpperCase());
        
        if (activo == null) {
            // Si no se especifica, devolver todos (activos e inactivos)
            return catalogoRepository.findByTipo(tipoCatalogo).stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else if (activo) {
            // Solo activos
            return catalogoRepository.findByTipoAndActivoTrue(tipoCatalogo).stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else {
            // Solo inactivos
            return catalogoRepository.findByTipoAndActivoFalse(tipoCatalogo).stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        }
    }
    

    // Obtener por ID
    public CatalogoDTO obtenerPorId(Long id) {
        return catalogoRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    // Crear nuevo catálogo
    public CatalogoDTO crear(CatalogoRequestDTO solicitud) {
        // Verificar si ya existe el mismo valor para el mismo tipo
        TipoCatalogo tipo = TipoCatalogo.valueOf(solicitud.getTipo().toUpperCase());
        Optional<Catalogo> existente = catalogoRepository.findByTipoAndValor(tipo, solicitud.getValor());
        
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe un catálogo con el mismo tipo y valor");
        }

        Catalogo catalogo = new Catalogo();
        catalogo.setTipo(tipo);
        catalogo.setValor(solicitud.getValor());
        catalogo.setActivo(true); // Siempre activo al crear

        Catalogo guardado = catalogoRepository.save(catalogo);
        return mapearEntidadADTO(guardado);
    }

    // Actualizar catálogo
    public CatalogoDTO actualizar(Long id, CatalogoRequestDTO solicitud) {
        return catalogoRepository.findById(id)
                .map(catalogo -> {
                    // Verificar si el nuevo valor ya existe para otro registro del mismo tipo
                    TipoCatalogo tipo = TipoCatalogo.valueOf(solicitud.getTipo().toUpperCase());
                    Optional<Catalogo> existente = catalogoRepository.findByTipoAndValor(tipo, solicitud.getValor());
                    
                    if (existente.isPresent() && !existente.get().getId().equals(id)) {
                        throw new RuntimeException("Ya existe un catálogo con el mismo tipo y valor");
                    }

                    catalogo.setTipo(tipo);
                    catalogo.setValor(solicitud.getValor());

                    Catalogo actualizado = catalogoRepository.save(catalogo);
                    return mapearEntidadADTO(actualizado);
                })
                .orElse(null);
    }

    // Eliminar (desactivar)
    public void eliminar(Long id) {
        catalogoRepository.findById(id)
                .ifPresent(catalogo -> {
                    catalogo.setActivo(false);
                    catalogoRepository.save(catalogo);
                });
    }

    // Reactivar catálogo
    public CatalogoDTO reactivar(Long id) {
        return catalogoRepository.findById(id)
                .map(catalogo -> {
                    catalogo.setActivo(true);
                    Catalogo reactivado = catalogoRepository.save(catalogo);
                    return mapearEntidadADTO(reactivado);
                })
                .orElse(null);
    }

    // Eliminar físicamente
    public void eliminarFisicamente(Long id) {
        catalogoRepository.deleteById(id);
    }

    // Mapeo de entidad a DTO
    private CatalogoDTO mapearEntidadADTO(Catalogo catalogo) {
        CatalogoDTO dto = new CatalogoDTO();
        dto.setId(catalogo.getId());
        dto.setTipo(catalogo.getTipo().name());
        dto.setValor(catalogo.getValor());
        dto.setActivo(catalogo.getActivo());
        return dto;
    }

    // Obtener catálogo por ID para uso interno
    public Catalogo obtenerEntidadPorId(Long id) {
        return catalogoRepository.findById(id).orElse(null);
    }
}