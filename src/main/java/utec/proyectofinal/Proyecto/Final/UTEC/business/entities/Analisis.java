package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Analisis")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Analisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer analisisID;

    @ManyToOne
    @JoinColumn(name = "loteID")
    private Lote lote;

    private String tipo;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;


    private Boolean publicadoParcial;
    private Boolean cumpleEstandar;

    @Column(columnDefinition = "TEXT")
    private String comentarios;

    @OneToMany(mappedBy = "analisis", cascade = CascadeType.ALL)
    private List<AnalisisHistorial> historial;


}
