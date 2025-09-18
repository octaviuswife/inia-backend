package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "ConfiguracionDatos")
@Data
public class ConfiguracionDatos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configuracionID;

    private String tipo; // "HUMEDAD" o "NUMERO_ARTICULO"

    @ElementCollection
    @CollectionTable(name = "configuracion_valores", joinColumns = @JoinColumn(name = "configuracion_id"))
    @Column(name = "valor")
    private List<String> valores; // Lista de valores disponibles

    private Boolean activo;
}