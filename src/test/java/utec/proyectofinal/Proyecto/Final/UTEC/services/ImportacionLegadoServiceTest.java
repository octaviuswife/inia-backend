package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ImportacionLegadoService")
class ImportacionLegadoServiceTest {

    @Mock
    private LegadoRepository legadoRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private CultivarRepository cultivarRepository;

    @Mock
    private EspecieRepository especieRepository;

    @Mock
    private ContactoRepository contactoRepository;

    @Mock
    private CatalogoCrudRepository catalogoRepository;

    @Mock
    private MultipartFile archivo;

    @InjectMocks
    private ImportacionLegadoService importacionLegadoService;

    private Especie especie;
    private Cultivar cultivar;
    private Contacto empresa;
    private Catalogo deposito;
    private Catalogo origen;
    private Lote lote;

    @BeforeEach
    void setUp() {
        // Preparar entidades de prueba
        especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun("Trigo");
        especie.setNombreCientifico("Triticum aestivum");
        especie.setActivo(true);

        cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        cultivar.setActivo(true);

        empresa = new Contacto();
        empresa.setContactoID(1L);
        empresa.setNombre("Empresa Test");
        empresa.setContacto("test@test.com");
        empresa.setTipo(TipoContacto.EMPRESA);
        empresa.setActivo(true);

        deposito = new Catalogo();
        deposito.setId(1L);
        deposito.setTipo(TipoCatalogo.DEPOSITO);
        deposito.setValor("Depósito Test");
        deposito.setActivo(true);

        origen = new Catalogo();
        origen.setId(2L);
        origen.setTipo(TipoCatalogo.ORIGEN);
        origen.setValor("Origen Test");
        origen.setActivo(true);

        lote = new Lote();
        lote.setLoteID(1L);
        lote.setFicha("FICHA-001");
        lote.setCultivar(cultivar);
        lote.setEmpresa(empresa);
        lote.setDeposito(deposito);
        lote.setOrigen(origen);
        lote.setActivo(true);
    }

    @Test
    @DisplayName("Importar archivo con error IO - debe manejar correctamente")
    void importarDesdeExcel_errorIO_debeManejarCorrectamente() throws IOException {
        when(archivo.getInputStream()).thenThrow(new IOException("Error al leer archivo"));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertFalse(resultado.getExitoso());
        assertTrue(resultado.getMensaje().contains("Error al leer"));
    }

    @Test
    @DisplayName("Importar archivo vacío - debe procesar sin errores")
    void importarDesdeExcel_archivoVacio_debeProcesarSinErrores() throws IOException {
        // Crear workbook vacío
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Empresa");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
        assertEquals(0, resultado.getTotalFilas());
    }

    @Test
    @DisplayName("Importar archivo con una fila válida - debe importar correctamente")
    void importarDesdeExcel_conFilaValida_debeImportarCorrectamente() throws IOException {
        // Crear workbook con datos válidos
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        
        // Encabezados
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        // Fila de datos
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");         // COL_EMPRESA
        dataRow.createCell(1).setCellValue("COD001");               // COL_COD_DOC
        dataRow.createCell(2).setCellValue("Documento Test");       // COL_NOM_DOC
        dataRow.createCell(3).setCellValue("123");                  // COL_NRO_DOC
        dataRow.createCell(4).setCellValue("Depósito Test");        // COL_DEPOSITO
        dataRow.createCell(5).setCellValue("Familia Test");         // COL_FAMILIA
        dataRow.createCell(6).setCellValue("Trigo");                // COL_ESPECIE
        dataRow.createCell(7).setCellValue("Cultivar Test");        // COL_VARIEDAD
        dataRow.createCell(8).setCellValue("LOTE-001");             // COL_LOTE
        dataRow.createCell(9).setCellValue("Certificada");          // COL_TIPO_SEMILLA
        dataRow.createCell(10).setCellValue(1000.0);               // COL_CANTIDAD
        dataRow.createCell(14).setCellValue("FICHA-001");          // COL_NRO_FICHA
        dataRow.createCell(12).setCellValue("Origen Test");        // COL_ORIGEN
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        // Configurar mocks
        when(contactoRepository.findByNombreAndTipoAndActivoTrue("Empresa Test", TipoContacto.EMPRESA))
            .thenReturn(Optional.empty());
        when(contactoRepository.save(any(Contacto.class))).thenReturn(empresa);
        
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(TipoCatalogo.DEPOSITO, "Depósito Test"))
            .thenReturn(Optional.empty());
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(TipoCatalogo.ORIGEN, "Origen Test"))
            .thenReturn(Optional.empty());
        when(catalogoRepository.save(any(Catalogo.class))).thenAnswer(i -> i.getArgument(0));
        
        when(especieRepository.findByNombreComunIgnoreCase("Trigo")).thenReturn(Optional.empty());
        when(especieRepository.buscarPorNombreComunInicio("Trigo")).thenReturn(java.util.Collections.emptyList());
        when(especieRepository.findByActivoTrue()).thenReturn(java.util.Collections.emptyList());
        when(especieRepository.save(any(Especie.class))).thenReturn(especie);
        
        when(cultivarRepository.findByNombreAndEspecie_EspecieID("Cultivar Test", 1L))
            .thenReturn(Optional.empty());
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);
        
        when(loteRepository.findByFicha("FICHA-001")).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
        assertEquals(1, resultado.getFilasImportadas());
        assertEquals(0, resultado.getFilasConErrores());
    }

    @Test
    @DisplayName("Importar solo validación - no debe guardar datos")
    void importarDesdeExcel_soloValidacion_noDebeGuardarDatos() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Empresa");
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, true);

        assertNotNull(resultado);
        assertTrue(resultado.getMensaje().contains("Validación completada"));
        verify(loteRepository, never()).save(any());
        verify(legadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Importar archivo con fila con error - debe registrar error")
    void importarDesdeExcel_conFilaConError_debeRegistrarError() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Empresa");
        
        // Fila con datos incompletos que causará error
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        // Faltan otros campos necesarios
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenThrow(new RuntimeException("Error de prueba"));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertFalse(resultado.getExitoso());
        assertEquals(1, resultado.getFilasConErrores());
        assertFalse(resultado.getErrores().isEmpty());
    }

    @Test
    @DisplayName("Obtener o crear empresa existente - debe retornar la existente")
    void obtenerOCrearEmpresa_empresaExistente_debeRetornarExistente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Existente");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-002");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue("Empresa Existente", TipoContacto.EMPRESA))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        verify(contactoRepository, never()).save(any(Contacto.class));
    }

    @Test
    @DisplayName("Obtener o crear catálogo existente - debe retornar el existente")
    void obtenerOCrearCatalogo_catalogoExistente_debeRetornarExistente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("207 - Depósito Existente");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Existente");
        dataRow.createCell(14).setCellValue("FICHA-003");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        verify(catalogoRepository, never()).save(any(Catalogo.class));
    }

    @Test
    @DisplayName("Obtener o crear especie con búsqueda flexible - debe encontrar por nombre común")
    void obtenerOCrearEspecie_busquedaFlexible_debeEncontrarPorNombreComun() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("1517 - RAIGRAS");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-004");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase("RAIGRAS"))
            .thenReturn(Optional.empty());
        when(especieRepository.buscarPorNombreComunInicio("RAIGRAS"))
            .thenReturn(java.util.Collections.singletonList(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        verify(especieRepository, never()).save(any(Especie.class));
    }

    @Test
    @DisplayName("Crear lote con ficha existente - debe retornar el existente sin guardar de nuevo")
    void crearLote_conFichaExistente_debeRetornarExistente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-EXISTENTE");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha("FICHA-EXISTENTE")).thenReturn(Optional.of(lote));
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        // El lote existente se reutiliza, no se guarda de nuevo
        verify(loteRepository, times(1)).findByFicha("FICHA-EXISTENTE");
        verify(legadoRepository, times(1)).save(any(Legado.class));
    }

    @Test
    @DisplayName("Importar con filas vacías - debe ignorar filas vacías")
    void importarDesdeExcel_conFilasVacias_debeIgnorarFilasVacias() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Empresa");
        
        // Fila 1: vacía
        sheet.createRow(1);
        
        // Fila 2: con datos
        Row dataRow = sheet.createRow(2);
        dataRow.createCell(0).setCellValue("Empresa Test");
        
        // Fila 3: vacía
        sheet.createRow(3);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenThrow(new RuntimeException("Error intencional"));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        // Solo debe procesar 1 fila (la que tiene datos)
        assertEquals(1, resultado.getTotalFilas());
    }

    @Test
    @DisplayName("Crear legado con todos los campos - debe mapear correctamente")
    void crearLegado_conTodosLosCampos_debeMapearcorrectamente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(1).setCellValue("COD001");
        dataRow.createCell(2).setCellValue("Documento Test");
        dataRow.createCell(3).setCellValue("123");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(5).setCellValue("Familia Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(8).setCellValue("LOTE-001");
        dataRow.createCell(9).setCellValue("Certificada");
        dataRow.createCell(10).setCellValue(1000.0);
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-005");
        dataRow.createCell(16).setCellValue(85);  // GERM_C
        dataRow.createCell(17).setCellValue(80);  // GERM_SC
        dataRow.createCell(18).setCellValue(5.5); // PESO_1000
        dataRow.createCell(19).setCellValue(95.0); // PURA
        dataRow.createCell(48).setCellValue("Pretratamiento");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
        verify(legadoRepository, times(1)).save(any(Legado.class));
    }

    @Test
    @DisplayName("getCellValueAsDate con celda numérica formateada - debe convertir correctamente")
    void getCellValueAsDate_celdaNumerica_debeConvertirCorrectamente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-006");
        
        // Crear celda de fecha con formato numérico
        Cell fechaCell = dataRow.createCell(15);
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("d/m/yyyy"));
        fechaCell.setCellStyle(dateStyle);
        fechaCell.setCellValue(java.time.LocalDateTime.of(2023, 11, 15, 0, 0));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("getCellValueAsDate con celda string - debe parsear correctamente")
    void getCellValueAsDate_celdaString_debeParsearCorrectamente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-007");
        dataRow.createCell(15).setCellValue("15/11/2023"); // Fecha como string
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("getCellValueAsDate con celda vacía - debe retornar null")
    void getCellValueAsDate_celdaVacia_debeRetornarNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-008");
        // No agregar celda de fecha
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("getCellValueAsBigDecimal con celda string - debe convertir correctamente")
    void getCellValueAsBigDecimal_celdaString_debeConvertirCorrectamente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(10).setCellValue("1234,56"); // Número con coma como decimal
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-009");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("getCellValueAsBigDecimal con celda string vacía - debe retornar null")
    void getCellValueAsBigDecimal_celdaStringVacia_debeRetornarNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(10).setCellValue("   "); // String con espacios
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-010");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("getCellValueAsString con tipos de celda variados - debe convertir correctamente")
    void getCellValueAsString_tiposVariados_debeConvertirCorrectamente() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(1).setCellValue(123.45); // Número como COD_DOC
        dataRow.createCell(2).setCellValue(true); // Boolean como NOM_DOC
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-011");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("getCellValueAsString con celda de fórmula - debe retornar la fórmula")
    void getCellValueAsString_celdaFormula_debeRetornarFormula() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-012");
        
        // Crear celda con fórmula
        Cell formulaCell = dataRow.createCell(10);
        formulaCell.setCellFormula("SUM(A1:A10)");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("parsearCodigo con valor completo - debe extraer solo el código")
    void parsearCodigo_conValorCompleto_debeExtraerCodigo() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("050 - Empresa INIA");
        dataRow.createCell(4).setCellValue("0522 - Depósito Central");
        dataRow.createCell(6).setCellValue("1517 - RAIGRAS");
        dataRow.createCell(7).setCellValue("123 - Cultivar Test");
        dataRow.createCell(12).setCellValue("999 - Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-013");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.empty());
        when(contactoRepository.save(any(Contacto.class))).thenReturn(empresa);
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.empty());
        when(catalogoRepository.save(any(Catalogo.class))).thenAnswer(i -> i.getArgument(0));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.empty());
        when(especieRepository.buscarPorNombreComunInicio(any()))
            .thenReturn(java.util.Collections.emptyList());
        when(especieRepository.findByActivoTrue()).thenReturn(java.util.Collections.emptyList());
        when(especieRepository.save(any(Especie.class))).thenReturn(especie);
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.empty());
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("obtenerOCrearEspecie con búsqueda por acentos - debe encontrar sin acentos")
    void obtenerOCrearEspecie_busquedaPorAcentos_debeEncontrarSinAcentos() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Raigras"); // Buscar "Raigras" debería encontrar "Raigrás"
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-014");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        // Crear especie con acentos
        Especie especieConAcentos = new Especie();
        especieConAcentos.setEspecieID(2L);
        especieConAcentos.setNombreComun("Raigrás");
        especieConAcentos.setActivo(true);
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase("Raigras"))
            .thenReturn(Optional.empty());
        when(especieRepository.buscarPorNombreComunInicio("Raigras"))
            .thenReturn(java.util.Collections.emptyList());
        when(especieRepository.findByActivoTrue())
            .thenReturn(java.util.Collections.singletonList(especieConAcentos));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
        verify(especieRepository, never()).save(any(Especie.class));
    }

    @Test
    @DisplayName("obtenerOCrearEmpresa con nombre null - debe retornar null")
    void obtenerOCrearEmpresa_nombreNull_debeRetornarNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        // No establecer empresa (columna 0)
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-015");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
        verify(contactoRepository, never()).save(any(Contacto.class));
    }

    @Test
    @DisplayName("obtenerOCrearCatalogo con valor null - debe retornar null")
    void obtenerOCrearCatalogo_valorNull_debeRetornarNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        // No establecer depósito (columna 4)
        dataRow.createCell(6).setCellValue("Especie Test");
        dataRow.createCell(7).setCellValue("Cultivar Test");
        // No establecer origen (columna 12)
        dataRow.createCell(14).setCellValue("FICHA-016");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(cultivarRepository.findByNombreAndEspecie_EspecieID(any(), any()))
            .thenReturn(Optional.of(cultivar));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
    }

    @Test
    @DisplayName("obtenerOCrearCultivar con nombre null - debe retornar null")
    void obtenerOCrearCultivar_nombreNull_debeRetornarNull() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Test");
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 60; i++) {
            headerRow.createCell(i).setCellValue("Col" + i);
        }
        
        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue("Empresa Test");
        dataRow.createCell(4).setCellValue("Depósito Test");
        dataRow.createCell(6).setCellValue("Especie Test");
        // No establecer cultivar (columna 7)
        dataRow.createCell(12).setCellValue("Origen Test");
        dataRow.createCell(14).setCellValue("FICHA-017");
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream(bos.toByteArray()));
        when(archivo.getOriginalFilename()).thenReturn("test.xlsx");
        
        when(contactoRepository.findByNombreAndTipoAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(empresa));
        when(catalogoRepository.findByTipoAndValorAndActivoTrue(any(), any()))
            .thenReturn(Optional.of(deposito));
        when(especieRepository.findByNombreComunIgnoreCase(any()))
            .thenReturn(Optional.of(especie));
        when(loteRepository.findByFicha(any())).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        when(legadoRepository.save(any(Legado.class))).thenAnswer(i -> i.getArgument(0));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertTrue(resultado.getExitoso());
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }
}
