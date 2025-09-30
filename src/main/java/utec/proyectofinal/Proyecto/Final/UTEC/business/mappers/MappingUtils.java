package utec.proyectofinal.Proyecto.Final.UTEC.business.mappers;

import java.util.Optional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasYCultivosCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasYCultivosCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasYCultivosCatalogoDTO;

public class MappingUtils {

    public static MalezasYCultivosCatalogoDTO toCatalogoDTO(MalezasYCultivosCatalogo malezasYCultivosCatalogo) {
        MalezasYCultivosCatalogoDTO dto = new MalezasYCultivosCatalogoDTO();
        dto.setCatalogoID(malezasYCultivosCatalogo.getCatalogoID());
        dto.setNombreComun(malezasYCultivosCatalogo.getNombreComun());
        dto.setNombreCientifico(malezasYCultivosCatalogo.getNombreCientifico());
        dto.setTipoMYCCatalogo(malezasYCultivosCatalogo.getTipoMYCCatalogo());
        // Omitir campo activo intencionalmente
        return dto;
    }

    public static ListadoDTO toListadoDTO(Listado listado) {
        ListadoDTO dto = new ListadoDTO();
        dto.setListadoID(listado.getListadoID());
        dto.setListadoTipo(listado.getListadoTipo());
        dto.setListadoInsti(listado.getListadoInsti());
        dto.setListadoNum(listado.getListadoNum());

        if (listado.getCatalogo() != null) {
            dto.setCatalogo(toCatalogoDTO(listado.getCatalogo()));
        }
        return dto;
    }

    public static Listado fromListadoRequest(ListadoRequestDTO solicitud, MalezasYCultivosCatalogoRepository catalogoRepository) {
        Listado listado = new Listado();
        listado.setListadoTipo(solicitud.getListadoTipo());
        listado.setListadoInsti(solicitud.getListadoInsti());
        listado.setListadoNum(solicitud.getListadoNum());

        if (solicitud.getIdCatalogo() != null) {
            Optional<MalezasYCultivosCatalogo> catalogoOpt = catalogoRepository.findById(solicitud.getIdCatalogo());
            if (catalogoOpt.isPresent()) {
                listado.setCatalogo(catalogoOpt.get());
            } else {
                throw new RuntimeException("Catálogo no encontrado con ID: " + solicitud.getIdCatalogo());
            }
        }
        return listado;
    }
}
