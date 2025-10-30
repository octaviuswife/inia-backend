package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "Pureza")
@Data
public class Pureza extends Analisis {

    private LocalDate fecha;
    private Boolean cumpleEstandar;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal pesoInicial_g;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal semillaPura_g;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal materiaInerte_g;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal otrosCultivos_g;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal malezas_g;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal malezasToleradas_g;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal malezasTolCero_g;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal pesoTotal_g;

    private BigDecimal redonSemillaPura;
    private BigDecimal redonMateriaInerte;
    private BigDecimal redonOtrosCultivos;
    private BigDecimal redonMalezas;
    private BigDecimal redonMalezasToleradas;
    private BigDecimal redonMalezasTolCero;
    private BigDecimal redonPesoTotal;

    private BigDecimal inasePura;
    private BigDecimal inaseMateriaInerte;
    private BigDecimal inaseOtrosCultivos;
    private BigDecimal inaseMalezas;
    private BigDecimal inaseMalezasToleradas;
    private BigDecimal inaseMalezasTolCero;

    private LocalDate inaseFecha;

    @OneToMany(mappedBy = "pureza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listado> listados;
}
