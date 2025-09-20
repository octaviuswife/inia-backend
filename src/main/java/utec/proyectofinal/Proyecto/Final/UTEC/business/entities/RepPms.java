package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "RepPMS")
@Data
public class RepPms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repPMSID;

    private Integer numRep;
    private Integer numTanda;
    private BigDecimal peso;
    private Boolean valido; // Indica si la repetición es válida según el CV

    @ManyToOne
    @JoinColumn(name = "pmsID")
    private Pms pms;
}

