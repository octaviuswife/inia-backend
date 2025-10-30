package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Analisis")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Analisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analisisID;

    @ManyToOne
    @JoinColumn(name = "loteID")
    private Lote lote;

    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @Column(columnDefinition = "TEXT")
    private String comentarios;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "analisis", cascade = CascadeType.ALL)
    private List<AnalisisHistorial> historial;


}
