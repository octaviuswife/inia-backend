package utec.proyectofinal.Proyecto.Final.UTEC.business.mappers;

import jakarta.persistence.EntityManager;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;

public class MappingUtils {

    public static CatalogoDTO toCatalogoDTO(Catalogo catalogo) {
        CatalogoDTO dto = new CatalogoDTO();
        dto.setCatalogoID(catalogo.getCatalogoID());
        dto.setNombreComun(catalogo.getNombreComun());
        dto.setNombreCientifico(catalogo.getNombreCientifico());
        dto.setMaleza(catalogo.getMaleza());
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

    public static Listado fromListadoRequest(ListadoRequestDTO solicitud, EntityManager entityManager) {
        Listado listado = new Listado();
        listado.setListadoTipo(solicitud.getListadoTipo());
        listado.setListadoInsti(solicitud.getListadoInsti());
        listado.setListadoNum(solicitud.getListadoNum());

        if (solicitud.getIdCatalogo() != null) {
            Catalogo catalogo = entityManager.getReference(Catalogo.class, solicitud.getIdCatalogo());
            listado.setCatalogo(catalogo);
        }
        return listado;
    }
}
