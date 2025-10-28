package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table (name = "Malezas")
@Data
public class MalezasCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long catalogoID;
    private String nombreComun;
    private String nombreCientifico;
    
    private Boolean activo = true;

    @OneToMany(mappedBy = "catalogo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listado> listados;
}
