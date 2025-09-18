package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

import java.math.BigDecimal;

@Entity
@Table(name = "ValoresGerm")
@Data
public class ValoresGerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long valoresGermID;

    private Instituto instituto;
    private BigDecimal normales;
    private BigDecimal anormales;
    private BigDecimal duras;
    private BigDecimal frescas;
    private BigDecimal muertas;
    private BigDecimal germinacion;

    @ManyToOne
    @JoinColumn(name = "tablaGermID")
    private TablaGerm tablaGerm;
}

