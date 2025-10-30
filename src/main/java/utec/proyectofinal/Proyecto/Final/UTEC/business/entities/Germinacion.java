package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Table(name = "Germinacion")
@Data
@EqualsAndHashCode(callSuper = false)
public class Germinacion extends Analisis {

    @OneToMany(mappedBy = "germinacion", cascade = CascadeType.ALL)
    private List<TablaGerm> tablaGerm;

}
