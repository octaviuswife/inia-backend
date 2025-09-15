package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Tetrazolio")
@Data
public class Tetrazolio extends Analisis {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tetrazGeneralID;

    private Integer numSemillasPorRep;
    private String pretratamiento;
    private String concentracion;
    private Integer tincionHs;
    private Integer tincionTemp;
    private LocalDate fecha;

    @OneToMany(mappedBy = "tetrazolio", cascade = CascadeType.ALL)
    private List<RepTetrazolioViabilidad> repeticiones;
}
