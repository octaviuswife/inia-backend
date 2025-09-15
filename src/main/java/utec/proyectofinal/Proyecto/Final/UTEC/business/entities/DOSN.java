package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "DOSN")
@Data
public class DOSN extends Analisis {

    private LocalDate fechaINIA;
    private BigDecimal gramosAnalizadosINIA;

    @ElementCollection
    private List<String> tipoINIA;

    private LocalDate fechaINASE;
    private BigDecimal gramosAnalizadosINASE;

    @ElementCollection
    private List<String> tipoINASE;

    private BigDecimal cuscuta_g;
    private Integer cuscutaNum;
    private LocalDate fecha;

    @ManyToMany
    @JoinTable(
            name = "DOSN_Listado",
            joinColumns = @JoinColumn(name = "dosnID"),
            inverseJoinColumns = @JoinColumn(name = "listadoID")
    )
    private List<Listado> listados;

}
