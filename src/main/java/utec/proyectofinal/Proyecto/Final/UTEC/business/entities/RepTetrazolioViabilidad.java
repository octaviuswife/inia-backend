package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "RepTetrazolioViabilidad")
@Data
public class RepTetrazolioViabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repTetrazolioViabID;

    private LocalDate fecha;
    private Integer viablesNum;
    private Integer noViablesNum;
    private Integer duras;

    @ManyToOne
    @JoinColumn(name = "tetrazolioID")
    private Tetrazolio tetrazolio;
}

