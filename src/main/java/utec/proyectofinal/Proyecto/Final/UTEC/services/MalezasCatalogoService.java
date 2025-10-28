package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
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
        // Campo activo removido del DTO - no necesario en responses
        return dto;
    }

    // Obtener entidad para uso interno
    public MalezasCatalogo obtenerEntidadPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}
