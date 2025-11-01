package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Listar Catálogos con paginado (para listado)
    public Page<CatalogoDTO> obtenerCatalogosPaginados(Pageable pageable) {
        Page<Catalogo> catalogoPage = catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable);
        return catalogoPage.map(this::mapearEntidadADTO);
    }

    // Listar Catálogos con paginado y filtro por activo
    public Page<CatalogoDTO> obtenerCatalogosPaginadosConFiltro(Pageable pageable, String filtroActivo) {
        Page<Catalogo> catalogoPage;
        
        if ("activos".equalsIgnoreCase(filtroActivo)) {
            catalogoPage = catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable);
        } else if ("inactivos".equalsIgnoreCase(filtroActivo)) {
            catalogoPage = catalogoRepository.findByActivoFalseOrderByTipoAscValorAsc(pageable);
        } else {
            // "todos" o cualquier otro valor
            catalogoPage = catalogoRepository.findAllByOrderByTipoAscValorAsc(pageable);
        }
        
        return catalogoPage.map(this::mapearEntidadADTO);
    }

    /**
     * Listar Catálogos con paginado y filtros dinámicos
     * @param pageable Información de paginación
     * @param searchTerm Término de búsqueda (opcional)
     * @param activo Filtro por estado activo (opcional)
     * @param tipo Filtro por tipo de catálogo (opcional)
     * @return Página de CatalogoDTO filtrados
     */
    public Page<CatalogoDTO> obtenerCatalogosPaginadosConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            String tipo) {
        
        Page<Catalogo> catalogoPage;
        
        // Si hay tipo específico
        if (tipo != null && !tipo.trim().isEmpty()) {
            try {
                TipoCatalogo tipoCatalogo = TipoCatalogo.valueOf(tipo.toUpperCase());
                
                // Si hay búsqueda
                if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                    List<Catalogo> catalogos;
                    if (activo == null) {
                        catalogos = catalogoRepository.findByTipo(tipoCatalogo);
                    } else if (activo) {
                        catalogos = catalogoRepository.findByTipoAndActivoTrue(tipoCatalogo);
                    } else {
                        catalogos = catalogoRepository.findByTipoAndActivoFalse(tipoCatalogo);
                    }
                    
                    // Filtrar por término de búsqueda
                    catalogos = catalogos.stream()
                        .filter(c -> c.getValor() != null && c.getValor().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    
                    // Convertir lista a página
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), catalogos.size());
                    List<Catalogo> pageContent = catalogos.subList(start, end);
                    catalogoPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, catalogos.size());
                } else {
                    // Sin búsqueda, solo filtro de activo y tipo
                    if (activo == null) {
                        catalogoPage = catalogoRepository.findByTipoOrderByValorAsc(tipoCatalogo, pageable);
                    } else if (activo) {
                        catalogoPage = catalogoRepository.findByTipoAndActivoTrueOrderByValorAsc(tipoCatalogo, pageable);
                    } else {
                        catalogoPage = catalogoRepository.findByTipoAndActivoFalseOrderByValorAsc(tipoCatalogo, pageable);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Tipo inválido, devolver página vacía
                catalogoPage = Page.empty(pageable);
            }
        } else {
            // Sin tipo específico
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<Catalogo> catalogos;
                if (activo == null) {
                    catalogos = catalogoRepository.findAll();
                } else if (activo) {
                    catalogos = catalogoRepository.findByActivoTrue();
                } else {
                    catalogos = catalogoRepository.findAll().stream()
                        .filter(c -> !c.getActivo())
                        .collect(Collectors.toList());
                }
                
                // Filtrar por término de búsqueda
                catalogos = catalogos.stream()
                    .filter(c -> c.getValor() != null && c.getValor().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
                
                // Convertir lista a página
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), catalogos.size());
                List<Catalogo> pageContent = catalogos.subList(start, end);
                catalogoPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, catalogos.size());
            } else {
                // Sin búsqueda ni tipo, solo filtro de activo
                if (activo == null) {
                    catalogoPage = catalogoRepository.findAllByOrderByTipoAscValorAsc(pageable);
                } else if (activo) {
                    catalogoPage = catalogoRepository.findByActivoTrueOrderByTipoAscValorAsc(pageable);
                } else {
                    catalogoPage = catalogoRepository.findByActivoFalseOrderByTipoAscValorAsc(pageable);
                }
            }
        }
        
        return catalogoPage.map(this::mapearEntidadADTO);
    }
}