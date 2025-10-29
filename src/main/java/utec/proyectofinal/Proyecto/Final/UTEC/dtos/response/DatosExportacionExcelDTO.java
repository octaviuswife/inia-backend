
package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class DatosExportacionExcelDTO {
    
    // Datos básicos del lote
    private String especie;
    private String variedad;
    private String lote;
    private String deposito;
    private String numeroArticulo;
    private String numeroAnalisis;
    private String numeroFicha;  // Mantener por compatibilidad
    private String nombreLote;    // ✅ NUEVO: Nombre del lote
    private String kilos;
    
    // Datos de humedad
    private BigDecimal humedad;
    
    // Datos de Pureza INIA
    private BigDecimal purezaSemillaPura;
    private BigDecimal purezaMateriaInerte;
    private BigDecimal purezaOtrosCultivos;
    private BigDecimal purezaMalezas;
    private BigDecimal purezaMalezasToleradas;
    private BigDecimal purezaMateriaTotal;
    
    // Datos de Pureza INASE
    private BigDecimal purezaInaseSemillaPura;
    private BigDecimal purezaInaseMateriaInerte;
    private BigDecimal purezaInaseOtrosCultivos;
    private BigDecimal purezaInaseMalezas;
    private BigDecimal purezaInaseMalezasToleradas;
    private BigDecimal purezaInaseMateriaTotal;
    
    // Descripción de malezas y otros cultivos
    private String descripcionMalezas;
    private String descripcionOtrosCultivos;
    private String descripcionMalezasToleradas;
    private String descripcionMateriaTotal;
    
    // DOSN (INIA)
    private String dosnOtrosCultivos;           // OC
    private String dosnMalezas;                 // M
    private String dosnMalezasToleradas;        // MT
    private String dosnMalezasToleranciaC;   // MTC (Malezas Tolerancia Cero)
    private String dosnBrassica;                // DB (Brassica)
    
    // DOSN-I (INASE)
    private String dosnInaseOtrosCultivos;      // OC
    private String dosnInaseMalezas;            // M
    private String dosnInaseMalezasToleradas;   // MT
    private String dosnInaseMalezasToleranciaC; // MTC (Malezas Tolerancia Cero)
    private String dosnInaseBrassica;           // DB (Brassica)
    
    // PMS
    private BigDecimal pms;
    
    // Fecha de análisis y tratamiento de semillas
    private LocalDate fechaAnalisis;
    private String tratamientoSemillas;
    
    // Datos de Germinación INIA
    private BigDecimal germinacionPlantulasNormales;
    private BigDecimal germinacionPlantulasAnormales;
    private BigDecimal germinacionSemillasDeterioras;
    private BigDecimal germinacionSemillasFrescas;
    private BigDecimal germinacionSemillasMuertas;
    private BigDecimal germinacionTotal;
    
    // Datos de Germinación INASE
    private BigDecimal germinacionInasePlantulasNormales;
    private BigDecimal germinacionInasePlantulasAnormales;
    private BigDecimal germinacionInaseSemillasDeterioras;
    private BigDecimal germinacionInaseSemillasFrescas;
    private BigDecimal germinacionInaseSemillasMuertas;
    private BigDecimal germinacionInaseTotal;
    
    // Viabilidad tetrazolio
    private BigDecimal viabilidadPorcentaje;
    private BigDecimal viabilidadInasePorcentaje;
}