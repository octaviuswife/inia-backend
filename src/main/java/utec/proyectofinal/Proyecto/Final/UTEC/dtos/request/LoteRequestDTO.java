package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoteRequestDTO {
    private Integer numeroFicha;
    private String ficha;
    private Long cultivarID;
    private String tipo;
    private String empresa;
    private Long clienteID;
    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;
    private Long depositoID;
    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;
    private BigDecimal kilosLimpios;
    private BigDecimal humedad;
    private Double cantidad;
    private String origen;
    private String estado;
    private LocalDate fechaCosecha;
}