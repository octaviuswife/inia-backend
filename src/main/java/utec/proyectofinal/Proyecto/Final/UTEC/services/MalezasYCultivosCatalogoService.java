package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasYCultivosCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasYCultivosCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.MalezasYCultivosCatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasYCultivosCatalogoDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MalezasYCultivosCatalogoService {

    @Autowired
    private MalezasYCultivosCatalogoRepository repository;

    // Obtener todos activos
    public List<MalezasYCultivosCatalogoDTO> obtenerTodos() {
        return repository.findByActivoTrue().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener inactivos
    public List<MalezasYCultivosCatalogoDTO> obtenerInactivos() {
        return repository.findByActivoFalse().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Filtrar por maleza/cultivo
    public List<MalezasYCultivosCatalogoDTO> obtenerPorTipo(Boolean esMaleza) {
        return repository.findByMalezaAndActivoTrue(esMaleza).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar por nombre común
    public List<MalezasYCultivosCatalogoDTO> buscarPorNombreComun(String nombre) {
        return repository.findByNombreComunContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar por nombre científico
    public List<MalezasYCultivosCatalogoDTO> buscarPorNombreCientifico(String nombre) {
        return repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener por ID
    public MalezasYCultivosCatalogoDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    // Crear
    public MalezasYCultivosCatalogoDTO crear(MalezasYCultivosCatalogoRequestDTO solicitud) {
        MalezasYCultivosCatalogo catalogo = new MalezasYCultivosCatalogo();
        catalogo.setNombreComun(solicitud.getNombreComun());
        catalogo.setNombreCientifico(solicitud.getNombreCientifico());
        catalogo.setMaleza(solicitud.getMaleza());
        catalogo.setActivo(true);

        MalezasYCultivosCatalogo guardado = repository.save(catalogo);
        return mapearEntidadADTO(guardado);
    }

    // Actualizar
    public MalezasYCultivosCatalogoDTO actualizar(Long id, MalezasYCultivosCatalogoRequestDTO solicitud) {
        return repository.findById(id)
                .map(catalogo -> {
                    catalogo.setNombreComun(solicitud.getNombreComun());
                    catalogo.setNombreCientifico(solicitud.getNombreCientifico());
                    catalogo.setMaleza(solicitud.getMaleza());
                    if (solicitud.getActivo() != null) {
                        catalogo.setActivo(solicitud.getActivo());
                    }

                    MalezasYCultivosCatalogo actualizado = repository.save(catalogo);
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
    public MalezasYCultivosCatalogoDTO reactivar(Long id) {
        return repository.findById(id)
                .map(catalogo -> {
                    catalogo.setActivo(true);
                    MalezasYCultivosCatalogo reactivado = repository.save(catalogo);
                    return mapearEntidadADTO(reactivado);
                })
                .orElse(null);
    }

    // Mapeo
    private MalezasYCultivosCatalogoDTO mapearEntidadADTO(MalezasYCultivosCatalogo catalogo) {
        MalezasYCultivosCatalogoDTO dto = new MalezasYCultivosCatalogoDTO();
        dto.setCatalogoID(catalogo.getCatalogoID());
        dto.setNombreComun(catalogo.getNombreComun());
        dto.setNombreCientifico(catalogo.getNombreCientifico());
        dto.setMaleza(catalogo.getMaleza());
        // Campo activo removido del DTO - no necesario en responses
        return dto;
    }

    // Obtener entidad para uso interno
    public MalezasYCultivosCatalogo obtenerEntidadPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}