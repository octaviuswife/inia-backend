package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "AnalisisHistorial")
@Data
public class AnalisisHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "analisisID")
    private Analisis analisis;

    @ManyToOne
    @JoinColumn(name = "usuarioID")
    private Usuario usuario;

    private LocalDateTime fechaHora;
}

