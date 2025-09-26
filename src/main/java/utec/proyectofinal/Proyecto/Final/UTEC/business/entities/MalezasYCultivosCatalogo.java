package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoMYCCatalogo;

import java.util.List;

@Entity
@Table (name = "MalezasYCultivos")
@Data
public class MalezasYCultivosCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long catalogoID;
    private String nombreComun;
    private String nombreCientifico;
    
    @Enumerated(EnumType.STRING)
    private TipoMYCCatalogo tipoMYCCatalogo;
    
    private Boolean activo = true;

    @OneToMany(mappedBy = "catalogo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Listado> listados;
}
