package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Tetrazolio")
@Data
public class Tetrazolio extends Analisis {

    private Integer numSemillasPorRep;
    private String pretratamiento;
    private String concentracion;
    private Integer tincionHs;
    private Integer tincionTemp;
    private LocalDate fecha;
    private Integer numRepeticionesEsperadas;

    @OneToMany(mappedBy = "tetrazolio", cascade = CascadeType.ALL)
    private List<RepTetrazolioViabilidad> repeticiones;

    private BigDecimal porcViablesRedondeo;
    private BigDecimal porcNoViablesRedondeo;
    private BigDecimal porcDurasRedondeo;

    @Column(name = "viabilidad_inase")
    private BigDecimal viabilidadInase;
}
