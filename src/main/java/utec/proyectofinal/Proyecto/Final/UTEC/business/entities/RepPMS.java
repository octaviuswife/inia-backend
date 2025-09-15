package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "RepPMS")
@Data
public class RepPMS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repPMSID;

    private Integer numRep;
    private BigDecimal peso;

    @ManyToOne
    @JoinColumn(name = "pmsID")
    private PMS pms;
}

