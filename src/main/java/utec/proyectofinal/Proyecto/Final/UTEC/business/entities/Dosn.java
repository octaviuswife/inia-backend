package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoDOSN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "DOSN")
@Data
public class Dosn extends Analisis {

    private Boolean cumpleEstandar;
    private LocalDate fechaINIA;
    private BigDecimal gramosAnalizadosINIA;

    @ElementCollection
    private List<TipoDOSN> tipoINIA;

    private LocalDate fechaINASE;
    private BigDecimal gramosAnalizadosINASE;

    @ElementCollection
    private List<TipoDOSN> tipoINASE;

    @OneToMany(mappedBy = "dosn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CuscutaRegistro> cuscutaRegistros;

    @OneToMany(mappedBy = "dosn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listado> listados;

}
