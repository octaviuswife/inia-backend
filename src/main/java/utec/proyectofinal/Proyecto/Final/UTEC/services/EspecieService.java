package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.EspecieRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;

import java.util.List;
import java.util.stream.Collectors;

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
}