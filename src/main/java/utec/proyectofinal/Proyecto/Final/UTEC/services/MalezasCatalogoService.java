package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.MalezasCatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MalezasCatalogoService {

    @Autowired
    private MalezasCatalogoRepository repository;

    // Obtener todos activos
    public List<MalezasCatalogoDTO> obtenerTodos() {
        return repository.findByActivoTrue().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener inactivos
    public List<MalezasCatalogoDTO> obtenerInactivos() {
        return repository.findByActivoFalse().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar por nombre común
    public List<MalezasCatalogoDTO> buscarPorNombreComun(String nombre) {
        return repository.findByNombreComunContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar por nombre científico
    public List<MalezasCatalogoDTO> buscarPorNombreCientifico(String nombre) {
        return repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    public MalezasCatalogoDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    // Crear
    public MalezasCatalogoDTO crear(MalezasCatalogoRequestDTO solicitud) {
        MalezasCatalogo catalogo = new MalezasCatalogo();
        catalogo.setNombreComun(solicitud.getNombreComun());
        catalogo.setNombreCientifico(solicitud.getNombreCientifico());
        catalogo.setActivo(true);

        MalezasCatalogo guardado = repository.save(catalogo);
        return mapearEntidadADTO(guardado);
    }

    // Actualizar
    public MalezasCatalogoDTO actualizar(Long id, MalezasCatalogoRequestDTO solicitud) {
        return repository.findById(id)
                .map(catalogo -> {
                    catalogo.setNombreComun(solicitud.getNombreComun());
                    catalogo.setNombreCientifico(solicitud.getNombreCientifico());

                    MalezasCatalogo actualizado = repository.save(catalogo);
                    return mapearEntidadADTO(actualizado);
                })
                .orElse(null);
    }

    // Soft delete
    public void eliminar(Long id) {
        repository.findById(id)
                .ifPresent(catalogo -> {
                    catalogo.setActivo(false);
                    repository.save(catalogo);
                });
    }

    // Reactivar
    public MalezasCatalogoDTO reactivar(Long id) {
        return repository.findById(id)
                .map(catalogo -> {
                    catalogo.setActivo(true);
                    MalezasCatalogo reactivado = repository.save(catalogo);
                    return mapearEntidadADTO(reactivado);
                })
                .orElse(null);
    }

    // Mapeo
    private MalezasCatalogoDTO mapearEntidadADTO(MalezasCatalogo catalogo) {
        MalezasCatalogoDTO dto = new MalezasCatalogoDTO();
        dto.setCatalogoID(catalogo.getCatalogoID());
        dto.setNombreComun(catalogo.getNombreComun());
        dto.setNombreCientifico(catalogo.getNombreCientifico());
        dto.setActivo(catalogo.getActivo()); // Mapear campo activo
        return dto;
    }

    // Obtener entidad para uso interno
    public MalezasCatalogo obtenerEntidadPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    // Listar Malezas con paginado (para listado)
    public Page<MalezasCatalogoDTO> obtenerMalezasPaginadas(Pageable pageable) {
        Page<MalezasCatalogo> malezasPage = repository.findByActivoTrueOrderByNombreComunAsc(pageable);
        return malezasPage.map(this::mapearEntidadADTO);
    }
    
    /**
     * Listar Malezas con paginado y filtros dinámicos
     * @param pageable Información de paginación
     * @param searchTerm Término de búsqueda (opcional)
     * @param activo Filtro por estado activo (opcional)
     * @return Página de MalezasCatalogoDTO filtrados
     */
    public Page<MalezasCatalogoDTO> obtenerMalezasPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo) {
        
        Page<MalezasCatalogo> malezasPage;
        
        // Si hay término de búsqueda, buscar en nombre común y científico
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<MalezasCatalogo> malezas;
            if (activo == null) {
                // Buscar en todas (activas e inactivas)
                malezas = repository.findAll().stream()
                    .filter(m -> 
                        (m.getNombreComun() != null && m.getNombreComun().toLowerCase().contains(searchTerm.toLowerCase())) ||
                        (m.getNombreCientifico() != null && m.getNombreCientifico().toLowerCase().contains(searchTerm.toLowerCase()))
                    )
                    .collect(Collectors.toList());
            } else if (activo) {
                // Buscar solo en activas
                List<MalezasCatalogo> porComun = repository.findByNombreComunContainingIgnoreCaseAndActivoTrue(searchTerm);
                List<MalezasCatalogo> porCientifico = repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(searchTerm);
                malezas = new java.util.ArrayList<>(porComun);
                porCientifico.forEach(m -> {
                    if (!malezas.contains(m)) malezas.add(m);
                });
            } else {
                // Buscar solo en inactivas
                malezas = repository.findAll().stream()
                    .filter(m -> !m.getActivo() &&
                        ((m.getNombreComun() != null && m.getNombreComun().toLowerCase().contains(searchTerm.toLowerCase())) ||
                         (m.getNombreCientifico() != null && m.getNombreCientifico().toLowerCase().contains(searchTerm.toLowerCase())))
                    )
                    .collect(Collectors.toList());
            }
            
            // Convertir lista a página
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), malezas.size());
            List<MalezasCatalogo> pageContent = malezas.subList(start, end);
            malezasPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, malezas.size());
        } else {
            // Sin término de búsqueda, aplicar solo filtro de activo
            if (activo == null) {
                malezasPage = repository.findAllByOrderByNombreComunAsc(pageable);
            } else if (activo) {
                malezasPage = repository.findByActivoTrueOrderByNombreComunAsc(pageable);
            } else {
                malezasPage = repository.findByActivoFalseOrderByNombreComunAsc(pageable);
            }
        }
        
        return malezasPage.map(this::mapearEntidadADTO);
    }
}
