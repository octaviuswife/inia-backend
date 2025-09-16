package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table (name = "Catalogo")
@Data
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long catalogoID;
    private String nombreComun;
    private String nombreCientifico;
    private Boolean maleza;

    @OneToOne
    @JoinColumn(name = "listado_id", referencedColumnName = "listadoID")
    private Listado listado;
}
