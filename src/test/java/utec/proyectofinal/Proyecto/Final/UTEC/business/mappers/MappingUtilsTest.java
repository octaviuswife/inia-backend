package utec.proyectofinal.Proyecto.Final.UTEC.business.mappers;

import org.junit.jupiter.api.Test;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MappingUtilsTest {

    @Test
    public void toEspecieDTO_mapsFields() {
        Especie e = new Especie();
        e.setEspecieID(42L);
        e.setNombreComun("Trigo");
        e.setNombreCientifico("Triticum aestivum");
        e.setActivo(Boolean.TRUE);

        EspecieDTO dto = MappingUtils.toEspecieDTO(e);

        assertNotNull(dto);
        assertEquals(42L, dto.getEspecieID());
        assertEquals("Trigo", dto.getNombreComun());
        assertEquals("Triticum aestivum", dto.getNombreCientifico());
        assertTrue(dto.getActivo());
    }

    @Test
    public void toListadoDTO_withCatalogoAndEspecie_mapsNestedDTOs() {
        MalezasCatalogo c = new MalezasCatalogo();
        c.setCatalogoID(7L);
        c.setNombreComun("Maleza X");
        c.setNombreCientifico("Maleza X cient.");

        Especie e = new Especie();
        e.setEspecieID(99L);
        e.setNombreComun("Especie Y");
        e.setNombreCientifico("Especie Y cient.");
        e.setActivo(Boolean.FALSE);

        Listado l = new Listado();
        l.setListadoID(123L);
        l.setListadoTipo(TipoListado.OTROS);
        l.setListadoInsti(Instituto.INIA);
        l.setListadoNum(5);
        l.setCatalogo(c);
        l.setEspecie(e);

        ListadoDTO dto = MappingUtils.toListadoDTO(l);

        assertNotNull(dto);
        assertEquals(123L, dto.getListadoID());
        assertEquals(TipoListado.OTROS, dto.getListadoTipo());
        assertEquals(Instituto.INIA, dto.getListadoInsti());
        assertEquals(5, dto.getListadoNum());

        MalezasCatalogoDTO catDto = dto.getCatalogo();
        assertNotNull(catDto);
        assertEquals(7L, catDto.getCatalogoID());
        assertEquals("Maleza X", catDto.getNombreComun());

        EspecieDTO espDto = dto.getEspecie();
        assertNotNull(espDto);
        assertEquals(99L, espDto.getEspecieID());
        assertEquals("Especie Y", espDto.getNombreComun());
        assertFalse(espDto.getActivo());
    }

    @Test
    public void fromListadoRequest_withoutIds_setsBasicFieldsAndNullsForRelations() {
        ListadoRequestDTO req = new ListadoRequestDTO();
        req.setListadoTipo(TipoListado.MAL_COMUNES);
        req.setListadoInsti(Instituto.INASE);
        req.setListadoNum(11);

        MalezasCatalogoRepository catRepo = mock(MalezasCatalogoRepository.class);
        EspecieRepository espRepo = mock(EspecieRepository.class);

        Listado result = MappingUtils.fromListadoRequest(req, catRepo, espRepo);

        assertNotNull(result);
        assertEquals(TipoListado.MAL_COMUNES, result.getListadoTipo());
        assertEquals(Instituto.INASE, result.getListadoInsti());
        assertEquals(11, result.getListadoNum());
        assertNull(result.getCatalogo());
        assertNull(result.getEspecie());
    }

    @Test
    public void fromListadoRequest_withCatalogoPresent_setsCatalogo() {
        ListadoRequestDTO req = new ListadoRequestDTO();
        req.setIdCatalogo(55L);
        req.setListadoTipo(TipoListado.MAL_TOLERANCIA);

        MalezasCatalogoRepository catRepo = mock(MalezasCatalogoRepository.class);
        EspecieRepository espRepo = mock(EspecieRepository.class);

        MalezasCatalogo c = new MalezasCatalogo();
        c.setCatalogoID(55L);
        c.setNombreComun("Found");

        when(catRepo.findById(55L)).thenReturn(Optional.of(c));

        Listado res = MappingUtils.fromListadoRequest(req, catRepo, espRepo);

        assertNotNull(res);
        assertNotNull(res.getCatalogo());
        assertEquals(55L, res.getCatalogo().getCatalogoID());
    }

    @Test
    public void fromListadoRequest_withCatalogoMissing_throwsRuntimeException() {
        ListadoRequestDTO req = new ListadoRequestDTO();
        req.setIdCatalogo(77L);

        MalezasCatalogoRepository catRepo = mock(MalezasCatalogoRepository.class);
        EspecieRepository espRepo = mock(EspecieRepository.class);

        when(catRepo.findById(77L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> MappingUtils.fromListadoRequest(req, catRepo, espRepo));
        assertTrue(ex.getMessage().contains("Catálogo no encontrado"));
        assertTrue(ex.getMessage().contains("77"));
    }

    @Test
    public void fromListadoRequest_withEspeciePresent_setsEspecie() {
        ListadoRequestDTO req = new ListadoRequestDTO();
        req.setIdEspecie(200L);

        MalezasCatalogoRepository catRepo = mock(MalezasCatalogoRepository.class);
        EspecieRepository espRepo = mock(EspecieRepository.class);

        Especie e = new Especie();
        e.setEspecieID(200L);
        e.setNombreComun("EspFound");

        when(espRepo.findById(200L)).thenReturn(Optional.of(e));

        Listado res = MappingUtils.fromListadoRequest(req, catRepo, espRepo);

        assertNotNull(res);
        assertNotNull(res.getEspecie());
        assertEquals(200L, res.getEspecie().getEspecieID());
    }

    @Test
    public void fromListadoRequest_withEspecieMissing_throwsRuntimeException() {
        ListadoRequestDTO req = new ListadoRequestDTO();
        req.setIdEspecie(301L);

        MalezasCatalogoRepository catRepo = mock(MalezasCatalogoRepository.class);
        EspecieRepository espRepo = mock(EspecieRepository.class);

        when(espRepo.findById(301L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> MappingUtils.fromListadoRequest(req, catRepo, espRepo));
        assertTrue(ex.getMessage().contains("Especie no encontrada"));
        assertTrue(ex.getMessage().contains("301"));
    }
}
