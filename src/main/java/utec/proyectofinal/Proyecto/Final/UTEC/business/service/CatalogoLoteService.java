package utec.proyectofinal.Proyecto.Final.UTEC.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.dto.CatalogoLoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.CatalogoLote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repository.CatalogoLoteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoLoteService {

    @Autowired
    private CatalogoLoteRepository catalogoLoteRepository;

    public List<String> obtenerDatosHumedad() {
        CatalogoLote catalogo = catalogoLoteRepository.findFirstByTipoAndActivoTrue(CatalogoLote.TipoCatalogo.HUMEDAD_DATOS);
        return catalogo != null ? catalogo.getDatos() : List.of();
    }

    public List<String> obtenerNumerosArticulo() {
        CatalogoLote catalogo = catalogoLoteRepository.findFirstByTipoAndActivoTrue(CatalogoLote.TipoCatalogo.NUMERO_ARTICULO);
        return catalogo != null ? catalogo.getDatos() : List.of();
    }

    public List<CatalogoLoteDTO> obtenerTodos() {
        return catalogoLoteRepository.findAll().stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    public CatalogoLoteDTO obtenerPorId(Long id) {
        return catalogoLoteRepository.findById(id)
                .map(this::mapearEntidadADTO)
                .orElse(null);
    }

    public CatalogoLoteDTO guardar(CatalogoLoteDTO dto) {
        CatalogoLote entidad = mapearDTOAEntidad(dto);
        CatalogoLote guardado = catalogoLoteRepository.save(entidad);
        return mapearEntidadADTO(guardado);
    }

    public CatalogoLoteDTO actualizar(Long id, CatalogoLoteDTO dto) {
        return catalogoLoteRepository.findById(id)
                .map(catalogo -> {
                    catalogo.setDatos(dto.getDatos());
                    catalogo.setActivo(dto.getActivo());
                    if (dto.getTipo() != null) {
                        catalogo.setTipo(CatalogoLote.TipoCatalogo.valueOf(dto.getTipo()));
                    }
                    CatalogoLote guardado = catalogoLoteRepository.save(catalogo);
                    return mapearEntidadADTO(guardado);
                })
                .orElse(null);
    }

    public void eliminar(Long id) {
        catalogoLoteRepository.deleteById(id);
    }

    private CatalogoLoteDTO mapearEntidadADTO(CatalogoLote entidad) {
        CatalogoLoteDTO dto = new CatalogoLoteDTO();
        dto.setCatalogoLoteID(entidad.getCatalogoLoteID());
        dto.setTipo(entidad.getTipo().name());
        dto.setDatos(entidad.getDatos());
        dto.setActivo(entidad.getActivo());
        return dto;
    }

    private CatalogoLote mapearDTOAEntidad(CatalogoLoteDTO dto) {
        CatalogoLote entidad = new CatalogoLote();
        if (dto.getCatalogoLoteID() != null) {
            entidad.setCatalogoLoteID(dto.getCatalogoLoteID());
        }
        entidad.setTipo(CatalogoLote.TipoCatalogo.valueOf(dto.getTipo()));
        entidad.setDatos(dto.getDatos());
        entidad.setActivo(dto.getActivo());
        return entidad;
    }
}