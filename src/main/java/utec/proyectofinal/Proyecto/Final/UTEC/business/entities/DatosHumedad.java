package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "DatosHumedad")
@Data
public class DatosHumedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datosHumedadID;

    @ManyToOne
    @JoinColumn(name = "loteID")
    private Lote lote;

    private String tipoHumedad; // Valor seleccionado de la lista configurable

    private BigDecimal valor; // Valor numérico ingresado manualmente
}