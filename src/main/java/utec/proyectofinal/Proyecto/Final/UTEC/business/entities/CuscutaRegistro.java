package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "CuscutaRegistro")
@Data
public class CuscutaRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Instituto instituto;

    private BigDecimal cuscuta_g;
    private Integer cuscutaNum;
    private LocalDate fechaCuscuta;

    @ManyToOne
    @JoinColumn(name = "dosn_id", referencedColumnName = "analisisID")
    private Dosn dosn;
}
