package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "CatalogoLote")
@Data
public class CatalogoLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long catalogoLoteID;

    @Enumerated(EnumType.STRING)
    private TipoCatalogo tipo;

    @ElementCollection
    @CollectionTable(name = "catalogo_lote_datos", joinColumns = @JoinColumn(name = "catalogo_lote_id"))
    private List<String> datos;

    private Boolean activo = true;

    public enum TipoCatalogo {
        HUMEDAD_DATOS,
        NUMERO_ARTICULO
    }
}