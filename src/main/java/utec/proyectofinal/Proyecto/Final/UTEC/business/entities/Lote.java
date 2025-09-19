package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoLote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Lote")
@Data
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loteID;

    private Integer numeroFicha;
    private String ficha;

    @ManyToOne
    @JoinColumn(name = "cultivarID")
    private Cultivar cultivar;

    private TipoLote tipo;
    
    @ManyToOne
    @JoinColumn(name = "empresaID")
    private Contacto empresa;

    @ManyToOne
    @JoinColumn(name = "clienteID")
    private Contacto cliente;

    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;

    @ManyToOne
    @JoinColumn(name = "depositoID")
    private Catalogo deposito;

    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;

    private BigDecimal kilosLimpios;
    
    // Relación con datos de humedad (múltiples conjuntos de tipo + valor)
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatosHumedad> datosHumedad;
    
    @ManyToOne
    @JoinColumn(name = "numeroArticuloID")
    private Catalogo numeroArticulo;
    
    private Double cantidad;

    @ManyToOne
    @JoinColumn(name = "origenID")
    private Catalogo origen;
    
    @ManyToOne
    @JoinColumn(name = "estadoID")
    private Catalogo estado;
    private LocalDate fechaCosecha;
    private Boolean activo;
}

