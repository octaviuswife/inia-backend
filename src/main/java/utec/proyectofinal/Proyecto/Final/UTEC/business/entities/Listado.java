package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Listado")
@Data
public class Listado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listadoID;

    private String listadoTipo;
    private String listadoInsti;
    private Integer listadoNum;

    @ManyToMany(mappedBy = "listados")
    private List<DOSN> dosns;

    @ManyToMany(mappedBy = "otrasSemillas")
    private List<Pureza> purezas;
}

