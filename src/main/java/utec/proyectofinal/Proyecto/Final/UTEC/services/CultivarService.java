package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CultivarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CultivarRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CultivarDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CultivarService {

    @Autowired
    private CultivarRepository cultivarRepository;

    @Autowired
    private EspecieService especieService;

    // Obtener todos activos
    public List<CultivarDTO> obtenerTodos() {
        return cultivarRepository.findByActivoTrue().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener inactivos
    public List<CultivarDTO> obtenerInactivos() {
        return cultivarRepository.findByActivoFalse().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener con filtro de estado opcional
    public List<CultivarDTO> obtenerTodos(Boolean activo) {
        if (activo == null) {
            // Devolver todos (activos e inactivos)
            return cultivarRepository.findAll().stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else if (activo) {
            return obtenerTodos();
        } else {
            return obtenerInactivos();
        }
    }

    // Obtener por especie
    public List<CultivarDTO> obtenerPorEspecie(Long especieID) {
        return cultivarRepository.findByEspecieEspecieIDAndActivoTrue(especieID).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar por nombre
    public List<CultivarDTO> buscarPorNombre(String nombre) {
        return cultivarRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    public CultivarDTO obtenerPorId(Long id) {
        return cultivarRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    // Crear
    public CultivarDTO crear(CultivarRequestDTO solicitud) {
        Especie especie = especieService.obtenerEntidadPorId(solicitud.getEspecieID());
        if (especie == null) {
            throw new RuntimeException("Especie no encontrada con ID: " + solicitud.getEspecieID());
        }

        Cultivar cultivar = new Cultivar();
        cultivar.setEspecie(especie);
        cultivar.setNombre(solicitud.getNombre());
        cultivar.setActivo(true);

        Cultivar guardado = cultivarRepository.save(cultivar);
        return mapearEntidadADTO(guardado);
    }

    // Actualizar
    public CultivarDTO actualizar(Long id, CultivarRequestDTO solicitud) {
        return cultivarRepository.findById(id)
                .map(cultivar -> {
                    if (solicitud.getEspecieID() != null) {
                        Especie especie = especieService.obtenerEntidadPorId(solicitud.getEspecieID());
                        if (especie == null) {
                            throw new RuntimeException("Especie no encontrada con ID: " + solicitud.getEspecieID());
                        }
                        cultivar.setEspecie(especie);
                    }
                    
                    cultivar.setNombre(solicitud.getNombre());

                    Cultivar actualizado = cultivarRepository.save(cultivar);
                    return mapearEntidadADTO(actualizado);
                })
                .orElse(null);
    }

    // Soft delete
    public void eliminar(Long id) {
        cultivarRepository.findById(id)
                .ifPresent(cultivar -> {
                    cultivar.setActivo(false);
                    cultivarRepository.save(cultivar);
                });
    }

    // Reactivar
    public CultivarDTO reactivar(Long id) {
        return cultivarRepository.findById(id)
                .map(cultivar -> {
                    if (cultivar.getActivo() != null && cultivar.getActivo()) {
                        throw new RuntimeException("El cultivar ya está activo");
                    }
                    cultivar.setActivo(true);
                    Cultivar reactivado = cultivarRepository.save(cultivar);
                    return mapearEntidadADTO(reactivado);
                })
                .orElse(null);
    }

    // Mapeo
    private CultivarDTO mapearEntidadADTO(Cultivar cultivar) {
        CultivarDTO dto = new CultivarDTO();
        dto.setCultivarID(cultivar.getCultivarID());
        dto.setNombre(cultivar.getNombre());
        dto.setActivo(cultivar.getActivo());
        
        if (cultivar.getEspecie() != null) {
            dto.setEspecieID(cultivar.getEspecie().getEspecieID());
            dto.setEspecieNombre(cultivar.getEspecie().getNombreComun());
        }
        
        return dto;
    }

    // Obtener entidad para uso interno
    public Cultivar obtenerEntidadPorId(Long id) {
        return cultivarRepository.findById(id).orElse(null);
    }
}