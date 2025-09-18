package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "DatosConfigurables")
@Data
public class DatosConfigurables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datosConfigurablesID;

    private String tipo; // "HUMEDAD", "NUMERO_ARTICULO", etc.
    private String valor; // El valor específico
    private Boolean activo; // Para poder desactivar sin eliminar

    public DatosConfigurables() {}

    public DatosConfigurables(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
        this.activo = true;
    }
}