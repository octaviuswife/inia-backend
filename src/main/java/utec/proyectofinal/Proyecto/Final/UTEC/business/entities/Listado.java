package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

import java.util.List;

@Entity
@Table(name = "Listado")
@Data
public class Listado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listadoID;

    private TipoListado listadoTipo;
    private String listadoInsti;
    private Integer listadoNum;

    @ManyToOne
    @JoinColumn(name = "pureza_id", referencedColumnName = "analisisID")
    private Pureza pureza;

    @ManyToOne
    @JoinColumn(name = "dosn_id", referencedColumnName = "analisisID")
    private DOSN dosn;

    @ManyToOne
    @JoinColumn(name = "catalogo_id", referencedColumnName = "catalogoID")
    private Catalogo catalogo;
}

