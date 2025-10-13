package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoLote;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Lote")
@Data
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loteID;

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
    
    // Lista de tipos de análisis asignados a este lote
    @ElementCollection(targetClass = TipoAnalisis.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "lote_tipos_analisis", 
        joinColumns = @JoinColumn(name = "lote_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"lote_id", "tipo_analisis"})
    )
    @Column(name = "tipo_analisis")
    private List<TipoAnalisis> tiposAnalisisAsignados;
    
    private Boolean activo;
    
    // Métodos personalizados para manejar tipos de análisis sin duplicados
    public void setTiposAnalisisAsignados(List<TipoAnalisis> tipos) {
        if (tipos == null) {
            this.tiposAnalisisAsignados = null;
            return;
        }
        
        // Usar LinkedHashSet para mantener el orden y eliminar duplicados
        Set<TipoAnalisis> tiposUnicos = new LinkedHashSet<>(tipos);
        this.tiposAnalisisAsignados = new ArrayList<>(tiposUnicos);
    }
    
    public List<TipoAnalisis> getTiposAnalisisAsignados() {
        if (this.tiposAnalisisAsignados == null) {
            return new ArrayList<>();
        }
        
        // Asegurar que no hay duplicados al retornar
        Set<TipoAnalisis> tiposUnicos = new LinkedHashSet<>(this.tiposAnalisisAsignados);
        return new ArrayList<>(tiposUnicos);
    }
    
    public void agregarTipoAnalisis(TipoAnalisis tipo) {
        if (tipo == null) return;
        
        if (this.tiposAnalisisAsignados == null) {
            this.tiposAnalisisAsignados = new ArrayList<>();
        }
        
        if (!this.tiposAnalisisAsignados.contains(tipo)) {
            this.tiposAnalisisAsignados.add(tipo);
        }
    }
    
    public void removerTipoAnalisis(TipoAnalisis tipo) {
        if (this.tiposAnalisisAsignados != null) {
            this.tiposAnalisisAsignados.remove(tipo);
        }
    }
}

