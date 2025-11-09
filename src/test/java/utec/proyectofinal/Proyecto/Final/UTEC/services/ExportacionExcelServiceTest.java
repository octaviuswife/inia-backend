package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ExportacionRequestDTO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ExportacionExcelService")
class ExportacionExcelServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private PurezaRepository purezaRepository;

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @Mock
    private DosnRepository dosnRepository;

    @InjectMocks
    private ExportacionExcelService exportacionExcelService;

    private Lote lote;
    private Cultivar cultivar;
    private Especie especie;

    @BeforeEach
    void setUp() {
        especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreCientifico("Especie Test");
        especie.setNombreComun("Especie Común");
        especie.setActivo(true);

        cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        cultivar.setActivo(true);

        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-001");
        lote.setFicha("FICHA-001");
        lote.setFechaRecibo(LocalDate.now());
        lote.setCultivar(cultivar);
        lote.setActivo(true);
    }

    @Test
    @DisplayName("Generar reporte Excel con lista vacía - debe generar archivo")
    void generarReporteExcel_listaVacia_debeGenerarArchivo() throws IOException {
        List<Long> loteIds = new ArrayList<>();

        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte Excel avanzado vacío - debe generar archivo")
    void generarReporteExcelAvanzado_vacio_debeGenerarArchivo() throws IOException {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setIncluirEncabezados(true);

        byte[] resultado = exportacionExcelService.generarReporteExcelAvanzado(solicitud);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte Excel sin encabezados - debe generar archivo")
    void generarReporteExcelAvanzado_sinEncabezados_debeGenerarArchivo() throws IOException {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setIncluirEncabezados(false);

        byte[] resultado = exportacionExcelService.generarReporteExcelAvanzado(solicitud);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte Excel con lote inexistente - debe generar archivo")
    void generarReporteExcel_loteInexistente_debeGenerarArchivo() throws IOException {
        List<Long> loteIds = Arrays.asList(999L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList());

        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(loteRepository, times(1)).findAllById(loteIds);
    }

    @Test
    @DisplayName("Generar reporte con múltiples lotes - debe generar archivo")
    void generarReporteExcel_multiples_debeGenerarArchivo() throws IOException {
        Lote lote2 = new Lote();
        lote2.setLoteID(2L);
        lote2.setNomLote("LOTE-002");
        lote2.setActivo(true);

        List<Long> loteIds = Arrays.asList(1L, 2L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote, lote2));

        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(loteRepository, times(1)).findAllById(loteIds);
    }
}
