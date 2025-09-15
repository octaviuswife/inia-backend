package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "RepGerm")
@Data
public class RepGerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repGermID;

    @ElementCollection
    private java.util.List<Integer> normales;

    private Integer anormales;
    private Integer duras;
    private Integer frescas;
    private Integer muertas;
    private Integer total;

    @ManyToOne
    @JoinColumn(name = "contGermID")
    private ContGerm contGerm;
}

