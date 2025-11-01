package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.EspecieRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;

@Service
public class EspecieService {

    @Autowired
    private EspecieRepository especieRepository;

    // Obtener todas activas
    public List<EspecieDTO> obtenerTodas() {
        return especieRepository.findByActivoTrue().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener inactivas
    public List<EspecieDTO> obtenerInactivas() {
        return especieRepository.findByActivoFalse().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener con filtro de estado opcional
    public List<EspecieDTO> obtenerTodas(Boolean activo) {
        if (activo == null) {
            // Devolver todas (activas e inactivas)
            return especieRepository.findAll().stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else if (activo) {
            return obtenerTodas();
        } else {
            return obtenerInactivas();
        }
    }

    // Buscar por nombre común
    public List<EspecieDTO> buscarPorNombreComun(String nombre) {
        return especieRepository.findByNombreComunContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar por nombre científico
    public List<EspecieDTO> buscarPorNombreCientifico(String nombre) {
        return especieRepository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    public EspecieDTO obtenerPorId(Long id) {
        return especieRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    // Crear
    public EspecieDTO crear(EspecieRequestDTO solicitud) {
        Especie especie = new Especie();
        especie.setNombreComun(solicitud.getNombreComun());
        especie.setNombreCientifico(solicitud.getNombreCientifico());
        especie.setActivo(true);

        Especie guardada = especieRepository.save(especie);
        return mapearEntidadADTO(guardada);
    }

    // Actualizar
    public EspecieDTO actualizar(Long id, EspecieRequestDTO solicitud) {
        return especieRepository.findById(id)
                .map(especie -> {
                    especie.setNombreComun(solicitud.getNombreComun());
                    especie.setNombreCientifico(solicitud.getNombreCientifico());

                    Especie actualizada = especieRepository.save(especie);
                    return mapearEntidadADTO(actualizada);
                })
                .orElse(null);
    }

    // Soft delete
    public void eliminar(Long id) {
        especieRepository.findById(id)
                .ifPresent(especie -> {
                    especie.setActivo(false);
                    // También desactivar cultivares asociados
                    if (especie.getCultivares() != null) {
                        especie.getCultivares().forEach(cultivar -> cultivar.setActivo(false));
                    }
                    especieRepository.save(especie);
                });
    }

    // Reactivar
    public EspecieDTO reactivar(Long id) {
        return especieRepository.findById(id)
                .map(especie -> {
                    if (especie.getActivo() != null && especie.getActivo()) {
                        throw new RuntimeException("La especie ya está activa");
                    }
                    especie.setActivo(true);
                    // También reactivar cultivares asociados
                    if (especie.getCultivares() != null) {
                        especie.getCultivares().forEach(cultivar -> cultivar.setActivo(true));
                    }
                    Especie reactivada = especieRepository.save(especie);
                    return mapearEntidadADTO(reactivada);
                })
                .orElse(null);
    }

    // Mapeo
    private EspecieDTO mapearEntidadADTO(Especie especie) {
        EspecieDTO dto = new EspecieDTO();
        dto.setEspecieID(especie.getEspecieID());
        dto.setNombreComun(especie.getNombreComun());
        dto.setNombreCientifico(especie.getNombreCientifico());
        dto.setActivo(especie.getActivo());
        
        // Mapear cultivares activos
        if (especie.getCultivares() != null) {
            List<String> cultivaresNombres = especie.getCultivares().stream()
                    .filter(cultivar -> cultivar.getActivo() != null && cultivar.getActivo())
                    .map(cultivar -> cultivar.getNombre())
                    .collect(Collectors.toList());
            dto.setCultivares(cultivaresNombres);
        }
        
        return dto;
    }

    // Obtener entidad para uso interno
    public Especie obtenerEntidadPorId(Long id) {
        return especieRepository.findById(id).orElse(null);
    }

    // Listar Especies con paginado (para listado)
    public Page<EspecieDTO> obtenerEspeciesPaginadas(Pageable pageable) {
        Page<Especie> especiePage = especieRepository.findByActivoTrueOrderByNombreComunAsc(pageable);
        return especiePage.map(this::mapearEntidadADTO);
    }

    // Listar Especies con paginado y filtro por activo
    public Page<EspecieDTO> obtenerEspeciesPaginadasConFiltro(Pageable pageable, String filtroActivo) {
        Page<Especie> especiePage;
        
        if ("activos".equalsIgnoreCase(filtroActivo)) {
            especiePage = especieRepository.findByActivoTrueOrderByNombreComunAsc(pageable);
        } else if ("inactivos".equalsIgnoreCase(filtroActivo)) {
            especiePage = especieRepository.findByActivoFalseOrderByNombreComunAsc(pageable);
        } else {
            // "todos" o cualquier otro valor
            especiePage = especieRepository.findAllByOrderByNombreComunAsc(pageable);
        }
        
        return especiePage.map(this::mapearEntidadADTO);
    }

    /**
     * Listar Especies con paginado y filtros dinámicos
     * @param pageable Información de paginación
     * @param searchTerm Término de búsqueda (opcional)
     * @param activo Filtro por estado activo (opcional)
     * @return Página de EspecieDTO filtrados
     */
    public Page<EspecieDTO> obtenerEspeciesPaginadasConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo) {
        
        Page<Especie> especiePage;
        
        // Si hay término de búsqueda, buscar en nombre común y científico
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            List<Especie> especies;
            if (activo == null) {
                // Buscar en todas (activas e inactivas)
                especies = especieRepository.findAll().stream()
                    .filter(e -> 
                        (e.getNombreComun() != null && e.getNombreComun().toLowerCase().contains(searchTerm.toLowerCase())) ||
                        (e.getNombreCientifico() != null && e.getNombreCientifico().toLowerCase().contains(searchTerm.toLowerCase()))
                    )
                    .collect(Collectors.toList());
            } else if (activo) {
                // Buscar solo en activas
                List<Especie> porComun = especieRepository.findByNombreComunContainingIgnoreCaseAndActivoTrue(searchTerm);
                List<Especie> porCientifico = especieRepository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(searchTerm);
                especies = new java.util.ArrayList<>(porComun);
                porCientifico.forEach(e -> {
                    if (!especies.contains(e)) especies.add(e);
                });
            } else {
                // Buscar solo en inactivas
                especies = especieRepository.findAll().stream()
                    .filter(e -> !e.getActivo() &&
                        ((e.getNombreComun() != null && e.getNombreComun().toLowerCase().contains(searchTerm.toLowerCase())) ||
                         (e.getNombreCientifico() != null && e.getNombreCientifico().toLowerCase().contains(searchTerm.toLowerCase())))
                    )
                    .collect(Collectors.toList());
            }
            
            // Convertir lista a página
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), especies.size());
            List<Especie> pageContent = especies.subList(start, end);
            especiePage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, especies.size());
        } else {
            // Sin término de búsqueda, aplicar solo filtro de activo
            if (activo == null) {
                especiePage = especieRepository.findAllByOrderByNombreComunAsc(pageable);
            } else if (activo) {
                especiePage = especieRepository.findByActivoTrueOrderByNombreComunAsc(pageable);
            } else {
                especiePage = especieRepository.findByActivoFalseOrderByNombreComunAsc(pageable);
            }
        }
        
        return especiePage.map(this::mapearEntidadADTO);
    }
}