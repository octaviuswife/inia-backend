package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "ContGerm")
@Data
public class ContGerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contGermID;

    @OneToMany(mappedBy = "contGerm", cascade = CascadeType.ALL)
    private List<RepGerm> repGerm;

    private Integer total;

    @ElementCollection
    private List<BigDecimal> promedioConRedondeo;

    @OneToMany(mappedBy = "contGerm", cascade = CascadeType.ALL) // Cambiado el valor de mappedBy
    private List<ValoresGerm> valoresGerm;

    @ManyToOne
    @JoinColumn(name = "germinacionID")
    private Germinacion germinacion;

}

