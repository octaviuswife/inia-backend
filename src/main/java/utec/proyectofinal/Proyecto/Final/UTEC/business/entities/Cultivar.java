package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Cultivar")
@Data
public class Cultivar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cultivarID;

    @ManyToOne
    @JoinColumn(name = "especieID")
    private Especie especie;

    private String nombre;
}

