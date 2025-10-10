package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DatosExportacionExcelDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ExportacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoMYCCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExportacionExcelService {

    @Autowired
    private LoteRepository loteRepository;
    
    @Autowired
    private PurezaRepository purezaRepository;
    
    @Autowired
    private GerminacionRepository germinacionRepository;
    
    @Autowired
    private PmsRepository pmsRepository;
    
    @Autowired
    private TetrazolioRepository tetrazolioRepository;
    
    @Autowired
    private DosnRepository dosnRepository;

    /**
     * Genera un archivo Excel con los datos de análisis de semillas según la estructura de la planilla de ejemplo
     */
    public byte[] generarReporteExcel(List<Long> loteIds) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Análisis de Semillas");
        
        // Configurar estilos
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle subHeaderStyle = crearEstiloSubEncabezado(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle yellowHeaderStyle = crearEstiloEncabezadoAmarillo(workbook);
        
        // Crear encabezados
        crearEncabezados(sheet, headerStyle, subHeaderStyle, yellowHeaderStyle);
        
        // Obtener datos y crear filas
        List<DatosExportacionExcelDTO> datos = obtenerDatosParaExportacion(loteIds);
        int filaActual = 2; // Empezar después de los encabezados
        
        for (DatosExportacionExcelDTO dato : datos) {
            crearFilaDatos(sheet, filaActual, dato, dataStyle);
            filaActual++;
        }
        
        // Ajustar anchos de columnas
        ajustarAnchoColumnas(sheet);
        
        // Convertir a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    /**
     * Genera un archivo Excel con filtros avanzados
     */
    public byte[] generarReporteExcelAvanzado(ExportacionRequestDTO solicitud) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Análisis de Semillas");
        
        // Configurar estilos
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle subHeaderStyle = crearEstiloSubEncabezado(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle yellowHeaderStyle = crearEstiloEncabezadoAmarillo(workbook);
        
        // Crear encabezados solo si se solicita
        if (solicitud.getIncluirEncabezados()) {
            crearEncabezados(sheet, headerStyle, subHeaderStyle, yellowHeaderStyle);
        }
        
        // Obtener datos con filtros
        List<DatosExportacionExcelDTO> datos = obtenerDatosConFiltros(solicitud);
        int filaActual = solicitud.getIncluirEncabezados() ? 2 : 0;
        
        for (DatosExportacionExcelDTO dato : datos) {
            crearFilaDatos(sheet, filaActual, dato, dataStyle);
            filaActual++;
        }
        
        // Ajustar anchos de columnas
        ajustarAnchoColumnas(sheet);
        
        // Convertir a byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    private void crearEncabezados(Sheet sheet, CellStyle headerStyle, CellStyle subHeaderStyle, CellStyle yellowHeaderStyle) {
        // Fila 0: Encabezados principales
        Row row0 = sheet.createRow(0);
        
        // Encabezados básicos
        String[] encabezadosPrincipales = {
            "", "", "", "", "", "", "", "", "",
            "Pureza INIA", "", "", "", "", "",
            "Pureza INASE", "", "", "", "",
            "Descripción", "", "", "",
            "DOSN", "", "", "",
            "DOSN-I", "", "", "", "",
            "PMS", "Fecha Análisis", "TS",
            "Germinación", "", "", "", "",
            "Germinación -I", "", "", "", "",
            "V%", "V-I%"
        };
        
        for (int i = 0; i < encabezadosPrincipales.length; i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue(encabezadosPrincipales[i]);
            
            // Aplicar estilo según el contenido
            if (encabezadosPrincipales[i].contains("INASE") || encabezadosPrincipales[i].contains("Germinación -I") || encabezadosPrincipales[i].contains("V-I%")) {
                cell.setCellStyle(yellowHeaderStyle);
            } else if (!encabezadosPrincipales[i].isEmpty()) {
                cell.setCellStyle(headerStyle);
            }
        }
        
        // Fila 1: Subencabezados
        Row row1 = sheet.createRow(1);
        String[] subEncabezados = {
            "Especie", "Variedad", "Lote", "Deposito", "N° de articulo", "N° análisis", "Nro. Ficha", "Kilos", "H%",
            "SP%", "MI%", "OC%", "M%", "MT.%", "M.T.C%",
            "SP-I%", "MI-I%", "OC-I%", "M%", "M.T-I%", "M.T.C%",
            "MI", "OC", "MT", "MTC",
            "OC", "M", "MT", "MTC",
            "OC", "M", "MT", "MTC", "DB",
            "", "", "",
            "PN%", "AN%", "D%", "F%", "M%", "G%",
            "PN-I%", "AN-I%", "D-I%", "F-I%", "M-I%", "G-I%",
            "", ""
        };
        
        for (int i = 0; i < subEncabezados.length; i++) {
            Cell cell = row1.createCell(i);
            cell.setCellValue(subEncabezados[i]);
            cell.setCellStyle(subHeaderStyle);
        }
        
        // Crear celdas combinadas para encabezados principales
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 14));   // Pureza INIA
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 15, 20));  // Pureza INASE
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 21, 24));  // Descripción
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 25, 28));  // DOSN
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 29, 33));  // DOSN-I
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 37, 42));  // Germinación
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 43, 48));  // Germinación -I
    }

    private void crearFilaDatos(Sheet sheet, int numeroFila, DatosExportacionExcelDTO datos, CellStyle style) {
        Row row = sheet.createRow(numeroFila);
        int col = 0;
        
        // Datos básicos
        crearCelda(row, col++, datos.getEspecie(), style);
        crearCelda(row, col++, datos.getVariedad(), style);
        crearCelda(row, col++, datos.getLote(), style);
        crearCelda(row, col++, datos.getDeposito(), style);
        crearCelda(row, col++, datos.getNumeroArticulo(), style);
        crearCelda(row, col++, datos.getNumeroAnalisis(), style);
        crearCelda(row, col++, datos.getNumeroFicha(), style);
        crearCelda(row, col++, datos.getKilos(), style);
        crearCelda(row, col++, datos.getHumedad(), style);
        
        // Pureza INIA
        crearCelda(row, col++, datos.getPurezaSemillaPura(), style);
        crearCelda(row, col++, datos.getPurezaMateriaInerte(), style);
        crearCelda(row, col++, datos.getPurezaOtrosCultivos(), style);
        crearCelda(row, col++, datos.getPurezaMalezas(), style);
        crearCelda(row, col++, datos.getPurezaMalezasToleradas(), style);
        crearCelda(row, col++, datos.getPurezaMateriaTotal(), style);
        
        // Pureza INASE
        crearCelda(row, col++, datos.getPurezaInaseSemillaPura(), style);
        crearCelda(row, col++, datos.getPurezaInaseMateriaInerte(), style);
        crearCelda(row, col++, datos.getPurezaInaseOtrosCultivos(), style);
        crearCelda(row, col++, datos.getPurezaInaseMalezas(), style);
        crearCelda(row, col++, datos.getPurezaInaseMalezasToleradas(), style);
        crearCelda(row, col++, datos.getPurezaInaseMateriaTotal(), style);
        
        // Descripción
        crearCelda(row, col++, datos.getDescripcionMalezas(), style);
        crearCelda(row, col++, datos.getDescripcionOtrosCultivos(), style);
        crearCelda(row, col++, datos.getDescripcionMalezasToleradas(), style);
        crearCelda(row, col++, datos.getDescripcionMateriaTotal(), style);
        
        // DOSN
        crearCelda(row, col++, datos.getDosnOtrosCultivos(), style);
        crearCelda(row, col++, datos.getDosnMalezas(), style);
        crearCelda(row, col++, datos.getDosnMalezasToleradas(), style);
        crearCelda(row, col++, datos.getDosnMateriaTotal(), style);
        
        // DOSN-I
        crearCelda(row, col++, datos.getDosnInaseOtrosCultivos(), style);
        crearCelda(row, col++, datos.getDosnInaseMalezas(), style);
        crearCelda(row, col++, datos.getDosnInaseMalezasToleradas(), style);
        crearCelda(row, col++, datos.getDosnInaseMateriaTotal(), style);
        crearCelda(row, col++, datos.getDosnInaseDB(), style);
        
        // PMS y otros
        crearCelda(row, col++, datos.getPms(), style);
        crearCelda(row, col++, datos.getFechaAnalisis() != null ? datos.getFechaAnalisis().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", style);
        crearCelda(row, col++, datos.getTratamientoSemillas(), style);
        
        // Germinación INIA
        crearCelda(row, col++, datos.getGerminacionPlantulasNormales(), style);
        crearCelda(row, col++, datos.getGerminacionPlantulasAnormales(), style);
        crearCelda(row, col++, datos.getGerminacionSemillasDeterioras(), style);
        crearCelda(row, col++, datos.getGerminacionSemillasFrescas(), style);
        crearCelda(row, col++, datos.getGerminacionSemillasMuertas(), style);
        crearCelda(row, col++, datos.getGerminacionTotal(), style);
        
        // Germinación INASE
        crearCelda(row, col++, datos.getGerminacionInasePlantulasNormales(), style);
        crearCelda(row, col++, datos.getGerminacionInasePlantulasAnormales(), style);
        crearCelda(row, col++, datos.getGerminacionInaseSemillasDeterioras(), style);
        crearCelda(row, col++, datos.getGerminacionInaseSemillasFrescas(), style);
        crearCelda(row, col++, datos.getGerminacionInaseSemillasMuertas(), style);
        crearCelda(row, col++, datos.getGerminacionInaseTotal(), style);
        
        // Viabilidad
        crearCelda(row, col++, datos.getViabilidadPorcentaje(), style);
        crearCelda(row, col++, datos.getViabilidadInasePorcentaje(), style);
    }

    private void crearCelda(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof BigDecimal) {
                cell.setCellValue(((BigDecimal) value).doubleValue());
            } else if (value instanceof Integer) {
                cell.setCellValue((Integer) value);
            } else if (value instanceof Double) {
                cell.setCellValue((Double) value);
            } else {
                cell.setCellValue(value.toString());
            }
        }
        
        cell.setCellStyle(style);
    }

    private List<DatosExportacionExcelDTO> obtenerDatosParaExportacion(List<Long> loteIds) {
        List<DatosExportacionExcelDTO> datos = new ArrayList<>();
        
        List<Lote> lotes;
        if (loteIds == null || loteIds.isEmpty()) {
            lotes = loteRepository.findByActivoTrue();
        } else {
            lotes = loteRepository.findAllById(loteIds);
        }
        
        System.out.println("Exportando datos para " + lotes.size() + " lotes");
        
        for (Lote lote : lotes) {
            System.out.println("Procesando lote ID: " + lote.getLoteID() + ", Ficha: " + lote.getFicha());
            
            DatosExportacionExcelDTO dto = new DatosExportacionExcelDTO();
            
            // Datos básicos del lote
            mapearDatosBasicosLote(dto, lote);
            System.out.println("Datos básicos mapeados - Especie: " + dto.getEspecie() + ", Variedad: " + dto.getVariedad());
            
            // Obtener y mapear análisis de pureza
            mapearDatosPureza(dto, lote);
            System.out.println("Datos pureza mapeados - SP: " + dto.getPurezaSemillaPura() + ", MI: " + dto.getPurezaMateriaInerte());
            
            // Obtener y mapear análisis de germinación
            mapearDatosGerminacion(dto, lote);
            System.out.println("Datos germinación mapeados - PN: " + dto.getGerminacionPlantulasNormales());
            
            // Obtener y mapear análisis de PMS
            mapearDatosPms(dto, lote);
            System.out.println("Datos PMS mapeados - PMS: " + dto.getPms());
            
            // Obtener y mapear análisis de tetrazolio
            mapearDatosTetrazolio(dto, lote);
            System.out.println("Datos tetrazolio mapeados - V%: " + dto.getViabilidadPorcentaje());
            
            // Obtener y mapear DOSN
            mapearDatosDosn(dto, lote);
            System.out.println("Datos DOSN mapeados");
            
            datos.add(dto);
        }
        
        return datos;
    }

    private List<DatosExportacionExcelDTO> obtenerDatosConFiltros(ExportacionRequestDTO solicitud) {
        List<DatosExportacionExcelDTO> datos = new ArrayList<>();
        
        List<Lote> lotes;
        
        // Aplicar filtros para obtener lotes
        if (solicitud.getLoteIds() != null && !solicitud.getLoteIds().isEmpty()) {
            lotes = loteRepository.findAllById(solicitud.getLoteIds());
        } else {
            // Obtener lotes según si incluir inactivos o no
            if (solicitud.getIncluirInactivos()) {
                lotes = loteRepository.findAll();
            } else {
                lotes = loteRepository.findByActivoTrue();
            }
        }
        
        // Filtrar por fechas si se especifican
        if (solicitud.getFechaDesde() != null || solicitud.getFechaHasta() != null) {
            lotes = lotes.stream()
                .filter(lote -> {
                    LocalDate fechaLote = lote.getFechaRecibo(); // o la fecha que consideres relevante
                    if (fechaLote == null) return false;
                    
                    boolean cumpleFechaDesde = solicitud.getFechaDesde() == null || 
                        !fechaLote.isBefore(solicitud.getFechaDesde());
                    boolean cumpleFechaHasta = solicitud.getFechaHasta() == null || 
                        !fechaLote.isAfter(solicitud.getFechaHasta());
                    
                    return cumpleFechaDesde && cumpleFechaHasta;
                })
                .collect(Collectors.toList());
        }
        
        // Filtrar por especies si se especifican
        if (solicitud.getEspecieIds() != null && !solicitud.getEspecieIds().isEmpty()) {
            lotes = lotes.stream()
                .filter(lote -> lote.getCultivar() != null && 
                               lote.getCultivar().getEspecie() != null &&
                               solicitud.getEspecieIds().contains(lote.getCultivar().getEspecie().getEspecieID()))
                .collect(Collectors.toList());
        }
        
        // Filtrar por cultivares si se especifican
        if (solicitud.getCultivarIds() != null && !solicitud.getCultivarIds().isEmpty()) {
            lotes = lotes.stream()
                .filter(lote -> lote.getCultivar() != null &&
                               solicitud.getCultivarIds().contains(lote.getCultivar().getCultivarID()))
                .collect(Collectors.toList());
        }
        
        // Procesar cada lote
        for (Lote lote : lotes) {
            DatosExportacionExcelDTO dto = new DatosExportacionExcelDTO();
            
            // Datos básicos del lote
            mapearDatosBasicosLote(dto, lote);
            
            // Mapear solo los tipos de análisis solicitados
            List<String> tiposAnalisis = solicitud.getTiposAnalisis();
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("PUREZA")) {
                mapearDatosPureza(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("GERMINACION")) {
                mapearDatosGerminacion(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("PMS")) {
                mapearDatosPms(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("TETRAZOLIO")) {
                mapearDatosTetrazolio(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("DOSN")) {
                mapearDatosDosn(dto, lote);
            }
            
            datos.add(dto);
        }
        
        return datos;
    }

    private void mapearDatosBasicosLote(DatosExportacionExcelDTO dto, Lote lote) {
        // Especie y variedad
        if (lote.getCultivar() != null) {
            dto.setVariedad(lote.getCultivar().getNombre());
            if (lote.getCultivar().getEspecie() != null) {
                dto.setEspecie(lote.getCultivar().getEspecie().getNombreComun());
            }
        }
        
        // Datos del lote
        dto.setLote(lote.getFicha());
        dto.setNumeroFicha(lote.getNumeroFicha() != null ? lote.getNumeroFicha().toString() : "");
        dto.setKilos(lote.getKilosLimpios() != null ? lote.getKilosLimpios().toString() : "");
        
        // Número de análisis (usando el ID del lote como referencia temporal)
        dto.setNumeroAnalisis(lote.getLoteID() != null ? lote.getLoteID().toString() : "");
        
        // Deposito
        if (lote.getDeposito() != null) {
            dto.setDeposito(lote.getDeposito().getValor());
        }
        
        // Número de artículo
        if (lote.getNumeroArticulo() != null) {
            dto.setNumeroArticulo(lote.getNumeroArticulo().getValor());
        }
        
        // Humedad (tomar el primer valor de datos de humedad)
        if (lote.getDatosHumedad() != null && !lote.getDatosHumedad().isEmpty()) {
            dto.setHumedad(lote.getDatosHumedad().get(0).getValor());
        }
    }

    private void mapearDatosPureza(DatosExportacionExcelDTO dto, Lote lote) {
        List<Pureza> analisisPureza = purezaRepository.findByLoteLoteID(lote.getLoteID());
        
        System.out.println("Buscando análisis de pureza para lote ID: " + lote.getLoteID());
        System.out.println("Análisis de pureza encontrados: " + analisisPureza.size());
        
        if (!analisisPureza.isEmpty()) {
            Pureza pureza = analisisPureza.get(0); // Tomar el más reciente o el primero
            
            System.out.println("Datos INIA - SP: " + pureza.getRedonSemillaPura() + ", MI: " + pureza.getRedonMateriaInerte());
            System.out.println("Datos INASE - SP: " + pureza.getInasePura() + ", MI: " + pureza.getInaseMateriaInerte());
            
            // Datos INIA
            dto.setPurezaSemillaPura(pureza.getRedonSemillaPura());
            dto.setPurezaMateriaInerte(pureza.getRedonMateriaInerte());
            dto.setPurezaOtrosCultivos(pureza.getRedonOtrosCultivos());
            dto.setPurezaMalezas(pureza.getRedonMalezas());
            dto.setPurezaMalezasToleradas(pureza.getRedonMalezasToleradas());
            dto.setPurezaMateriaTotal(pureza.getRedonPesoTotal());
            
            // Datos INASE
            dto.setPurezaInaseSemillaPura(pureza.getInasePura());
            dto.setPurezaInaseMateriaInerte(pureza.getInaseMateriaInerte());
            dto.setPurezaInaseOtrosCultivos(pureza.getInaseOtrosCultivos());
            dto.setPurezaInaseMalezas(pureza.getInaseMalezas());
            dto.setPurezaInaseMalezasToleradas(pureza.getInaseMalezasToleradas());
            // Para material total INASE, si no hay campo específico, usar el mismo que INIA
            dto.setPurezaInaseMateriaTotal(pureza.getRedonPesoTotal());
            
            if (pureza.getFecha() != null) {
                dto.setFechaAnalisis(pureza.getFecha());
            }
            
            // Mapear descripción de malezas y otros cultivos desde listados
            if (pureza.getListados() != null && !pureza.getListados().isEmpty()) {
                System.out.println("Listados encontrados: " + pureza.getListados().size());
                StringBuilder descripcionMalezas = new StringBuilder();
                StringBuilder descripcionOtrosCultivos = new StringBuilder();
                
                for (Listado listado : pureza.getListados()) {
                    if (listado.getCatalogo() != null) {
                        String nombre = listado.getCatalogo().getNombreComun();
                        TipoMYCCatalogo tipo = listado.getCatalogo().getTipoMYCCatalogo();
                        
                        System.out.println("Procesando listado - Nombre: " + nombre + ", Tipo: " + tipo);
                        
                        if (TipoMYCCatalogo.MALEZA.equals(tipo)) {
                            if (descripcionMalezas.length() > 0) descripcionMalezas.append(", ");
                            descripcionMalezas.append(nombre);
                        } else if (TipoMYCCatalogo.CULTIVO.equals(tipo)) {
                            if (descripcionOtrosCultivos.length() > 0) descripcionOtrosCultivos.append(", ");
                            descripcionOtrosCultivos.append(nombre);
                        }
                    }
                }
                
                dto.setDescripcionMalezas(descripcionMalezas.toString());
                dto.setDescripcionOtrosCultivos(descripcionOtrosCultivos.toString());
                
                System.out.println("Descripciones - Malezas: " + dto.getDescripcionMalezas() + ", Otros cultivos: " + dto.getDescripcionOtrosCultivos());
            } else {
                System.out.println("No se encontraron listados para este análisis de pureza");
            }
        } else {
            System.out.println("No se encontraron análisis de pureza para el lote");
        }
    }

    private void mapearDatosGerminacion(DatosExportacionExcelDTO dto, Lote lote) {
        List<Germinacion> analisisGerminacion = germinacionRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisGerminacion.isEmpty()) {
            Germinacion germinacion = analisisGerminacion.get(0);
            
            if (germinacion.getTablaGerm() != null && !germinacion.getTablaGerm().isEmpty()) {
                TablaGerm tabla = germinacion.getTablaGerm().get(0);
                
                // Buscar valores de INIA e INASE en ValoresGerm
                if (tabla.getValoresGerm() != null) {
                    for (ValoresGerm valores : tabla.getValoresGerm()) {
                        if (valores.getInstituto() == Instituto.INIA) {
                            // Datos de germinación INIA
                            dto.setGerminacionPlantulasNormales(valores.getNormales());
                            dto.setGerminacionPlantulasAnormales(valores.getAnormales());
                            dto.setGerminacionSemillasDeterioras(valores.getDuras());
                            dto.setGerminacionSemillasFrescas(valores.getFrescas());
                            dto.setGerminacionSemillasMuertas(valores.getMuertas());
                            dto.setGerminacionTotal(valores.getGerminacion());
                        } else if (valores.getInstituto() == Instituto.INASE) {
                            // Datos de germinación INASE
                            dto.setGerminacionInasePlantulasNormales(valores.getNormales());
                            dto.setGerminacionInasePlantulasAnormales(valores.getAnormales());
                            dto.setGerminacionInaseSemillasDeterioras(valores.getDuras());
                            dto.setGerminacionInaseSemillasFrescas(valores.getFrescas());
                            dto.setGerminacionInaseSemillasMuertas(valores.getMuertas());
                            dto.setGerminacionInaseTotal(valores.getGerminacion());
                        }
                    }
                }
                
                // Mapear tratamiento de semillas
                if (tabla.getTratamiento() != null) {
                    dto.setTratamientoSemillas(tabla.getTratamiento());
                }
            }
        }
    }

    private void mapearDatosPms(DatosExportacionExcelDTO dto, Lote lote) {
        List<Pms> analisisPms = pmsRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisPms.isEmpty()) {
            Pms pms = analisisPms.get(0);
            dto.setPms(pms.getPmsconRedon());
        }
    }

    private void mapearDatosTetrazolio(DatosExportacionExcelDTO dto, Lote lote) {
        List<Tetrazolio> analisisTetrazolio = tetrazolioRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisTetrazolio.isEmpty()) {
            Tetrazolio tetrazolio = analisisTetrazolio.get(0);
            
            // Datos de viabilidad INIA
            dto.setViabilidadPorcentaje(tetrazolio.getPorcViablesRedondeo());
            
            // Para INASE, por ahora usar el mismo valor (podrías tener campos separados)
            dto.setViabilidadInasePorcentaje(tetrazolio.getPorcViablesRedondeo());
        }
    }

    private void mapearDatosDosn(DatosExportacionExcelDTO dto, Lote lote) {
        List<Dosn> analisisDosn = dosnRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisDosn.isEmpty()) {
            Dosn dosn = analisisDosn.get(0);
            
            // Mapear descripción de malezas y otros cultivos desde listados de DOSN
            if (dosn.getListados() != null) {
                StringBuilder dosnOtrosCultivos = new StringBuilder();
                StringBuilder dosnMalezas = new StringBuilder();
                StringBuilder dosnMalezasToleradas = new StringBuilder();
                StringBuilder dosnMateriaTotal = new StringBuilder();
                
                // Para DOSN INASE
                StringBuilder dosnInaseOtrosCultivos = new StringBuilder();
                StringBuilder dosnInaseMalezas = new StringBuilder();
                StringBuilder dosnInaseMalezasToleradas = new StringBuilder();
                StringBuilder dosnInaseMateriaTotal = new StringBuilder();
                
                for (Listado listado : dosn.getListados()) {
                    if (listado.getCatalogo() != null) {
                        String nombre = listado.getCatalogo().getNombreComun();
                        TipoMYCCatalogo tipo = listado.getCatalogo().getTipoMYCCatalogo();
                        
                        if (TipoMYCCatalogo.MALEZA.equals(tipo)) {
                            if (dosnMalezas.length() > 0) dosnMalezas.append(", ");
                            dosnMalezas.append(nombre);
                            
                            // También agregar a INASE
                            if (dosnInaseMalezas.length() > 0) dosnInaseMalezas.append(", ");
                            dosnInaseMalezas.append(nombre);
                        } else if (TipoMYCCatalogo.CULTIVO.equals(tipo)) {
                            if (dosnOtrosCultivos.length() > 0) dosnOtrosCultivos.append(", ");
                            dosnOtrosCultivos.append(nombre);
                            
                            // También agregar a INASE
                            if (dosnInaseOtrosCultivos.length() > 0) dosnInaseOtrosCultivos.append(", ");
                            dosnInaseOtrosCultivos.append(nombre);
                        }
                    }
                }
                
                dto.setDosnOtrosCultivos(dosnOtrosCultivos.toString());
                dto.setDosnMalezas(dosnMalezas.toString());
                dto.setDosnMalezasToleradas(dosnMalezasToleradas.toString());
                dto.setDosnMateriaTotal(dosnMateriaTotal.toString());
                
                dto.setDosnInaseOtrosCultivos(dosnInaseOtrosCultivos.toString());
                dto.setDosnInaseMalezas(dosnInaseMalezas.toString());
                dto.setDosnInaseMalezasToleradas(dosnInaseMalezasToleradas.toString());
                dto.setDosnInaseMateriaTotal(dosnInaseMateriaTotal.toString());
            }
        }
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    private CellStyle crearEstiloEncabezadoAmarillo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    private CellStyle crearEstiloSubEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 8);
        style.setFont(font);
        
        return style;
    }

    private CellStyle crearEstiloData(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    private void ajustarAnchoColumnas(Sheet sheet) {
        // Ajustar el ancho de las primeras columnas que contienen texto
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
            // Establecer un ancho mínimo
            if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
        }
        
        // Para el resto de columnas (datos numéricos), establecer un ancho fijo
        for (int i = 8; i < 50; i++) {
            sheet.setColumnWidth(i, 2500);
        }
    }
}