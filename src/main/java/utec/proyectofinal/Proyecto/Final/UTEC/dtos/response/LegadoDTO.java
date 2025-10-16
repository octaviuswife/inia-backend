package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LegadoDTO {
    private Long legadoID;
    
    // Información del lote relacionado
    private LoteDTO lote;
    
    // Datos del documento
    private String codDoc;
    private String nomDoc;
    private String nroDoc;
    private LocalDate fechaDoc;
    private String familia;
    
    // Información del tipo de semilla y tratamiento
    private String tipoSemilla;
    private String tratada;
    private String tipoTratGerm;
    
    // Precios y montos
    private BigDecimal precioUnit;
    private String unidad;
    private String moneda;
    private BigDecimal importeMN;
    private BigDecimal importeMO;
    
    // Datos de germinación
    private Integer germC;
    private Integer germSC;
    private BigDecimal peso1000;
    
    // Datos de pureza
    private BigDecimal pura;
    private BigDecimal oc;
    private BigDecimal porcOC;
    private BigDecimal maleza;
    private BigDecimal malezaTol;
    private BigDecimal matInerte;
    
    // Datos de pureza inicial
    private BigDecimal puraI;
    private BigDecimal ocI;
    private BigDecimal malezaI;
    private BigDecimal malezaTolI;
    private BigDecimal matInerteI;
    
    // Otros datos
    private BigDecimal pesoHEC;
    private String nroTrans;
    private String ctaMov;
    private String caCC;
    private String ff;
    private String titular;
    private String ctaArt;
    private String proveedor;
    private String docAfect;
    private String nroAfect;
    private BigDecimal stk;
    private String referencia;
    
    // Fechas adicionales
    private LocalDate fechaSC_I;
    private LocalDate fechaC_I;
    private Integer germTotalSC_I;
    private Integer germTotalC_I;
    
    // Observaciones
    private String obsTrans;
    private String otrasSemillasObser;
    private String semillaPura;
    private String semillaOtrosCultivos;
    private String semillaMalezas;
    private String semillaMalezasToleradas;
    private String materiaInerte;
    
    // Metadatos
    private LocalDate fechaImportacion;
    private String archivoOrigen;
    private Integer filaExcel;
    private Boolean activo;
}
