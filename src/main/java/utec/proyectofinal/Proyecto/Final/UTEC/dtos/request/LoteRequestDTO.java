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
    private Long empresaID;
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
    
    // Número de artículo - ID del catálogo seleccionado
    private Long numeroArticuloID;
    
    private Double cantidad;
    private Long origenID;
    private Long estadoID;
    private LocalDate fechaCosecha;
}