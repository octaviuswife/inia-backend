package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    
    // Datos de humedad - lista de objetos con tipo y valor
    private List<DatosHumedadRequestDTO> datosHumedad;
    
    // Número de artículo - un solo valor seleccionado del catálogo
    private String numeroArticulo;
    
    private Double cantidad;
    private String origen;
    private String estado;
    private LocalDate fechaCosecha;
}