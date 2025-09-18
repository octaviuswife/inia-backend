package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "RepGerm")
@Data
public class RepGerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repGermID;

    private Integer numRep;

    @ElementCollection
    private List<Integer> normales;

    private Integer anormales;
    private Integer duras;
    private Integer frescas;
    private Integer muertas;
    private Integer total;

    @ManyToOne
    @JoinColumn(name = "contGermID")
    private ContGerm contGerm;
}

