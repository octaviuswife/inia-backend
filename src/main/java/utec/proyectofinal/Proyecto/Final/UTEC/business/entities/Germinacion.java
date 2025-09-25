package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Germinacion")
@Data
public class Germinacion extends Analisis {

    private LocalDate fechaInicioGerm;

    @ElementCollection
    private List<LocalDate> fechaConteos;

    private LocalDate fechaUltConteo;

    private String numDias;
    
    // Nuevos campos de control
    private Integer numeroRepeticiones;
    private Integer numeroConteos;

    @OneToMany(mappedBy = "germinacion", cascade = CascadeType.ALL)
    private List<TablaGerm> tablaGerm;

}
