package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;

@Entity
@Table(name = "catalogo")
@Data
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // HUMEDAD, ORIGEN, ARTICULO, ESTADO, DEPOSITO, etc.
    @Enumerated(EnumType.STRING)
    private TipoCatalogo tipo;

    private String valor;

    private Boolean activo = true;

}
