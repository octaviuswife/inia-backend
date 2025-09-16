package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Lote")
@Data
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer loteID;

    private Integer numeroFicha;
    private String ficha;

    @ManyToOne
    @JoinColumn(name = "cultivarID")
    private Cultivar cultivar;

    private String tipo;
    private String empresa;

    @ManyToOne
    @JoinColumn(name = "clienteID")
    private Cliente cliente;

    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;

    @ManyToOne
    @JoinColumn(name = "depositoID")
    private Deposito deposito;

    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;

    private BigDecimal kilosLimpios;
    private BigDecimal humedad;
    private Double cantidad;

    private String origen;
    private String estado;
    private LocalDate fechaCosecha;
    private Boolean activo;
}

