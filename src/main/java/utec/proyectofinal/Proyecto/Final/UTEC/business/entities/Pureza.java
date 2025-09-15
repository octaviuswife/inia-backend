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
    private BigDecimal pesoInicial_g;
    private BigDecimal semillaPura_g;
    private BigDecimal materiaInerte_g;
    private BigDecimal otrosCultivos_g;
    private BigDecimal malezas_g;
    private BigDecimal malezasToleradas_g;
    private BigDecimal pesoTotal_g;

    private BigDecimal redonSemillaPura;
    private BigDecimal redonMateriaInerte;
    private BigDecimal redonOtrosCultivos;
    private BigDecimal redonMalezas;
    private BigDecimal redonMalezasToleradas;
    private BigDecimal redonPesoTotal;

    private BigDecimal inaseValor;
    private LocalDate inaseFecha;

    @ManyToMany
    @JoinTable(
            name = "Pureza_Listado",
            joinColumns = @JoinColumn(name = "purezaID"),
            inverseJoinColumns = @JoinColumn(name = "listadoID")
    )
    private List<Listado> otrasSemillas;
}
