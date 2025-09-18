package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
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

    private String tipo;
    private String empresa;

    @ManyToOne
    @JoinColumn(name = "clienteID")
    private Cliente cliente;

    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;

    @ManyToOne
    @JoinColumn(name = "depositoID")
    private Deposito deposito;

    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;

    private BigDecimal kilosLimpios;
    
    // Relación con datos de humedad (múltiples conjuntos de tipo + valor)
    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatosHumedad> datosHumedad;
    
    // Número de artículo - un solo valor seleccionado del catálogo
    private String numeroArticulo;
    
    private Double cantidad;

    private String origen;
    private String estado;
    private LocalDate fechaCosecha;
    private Boolean activo;
}

