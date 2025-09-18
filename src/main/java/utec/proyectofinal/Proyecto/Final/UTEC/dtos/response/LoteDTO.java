package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class LoteDTO {
    private Long loteID;
    private Integer numeroFicha;
    private String ficha;

    private String cultivar;

    private String tipo;
    private String empresa;

    private String cliente;

    private String codigoCC;
    private String codigoFF;
    private LocalDate fechaEntrega;
    private LocalDate fechaRecibo;

    private String deposito;

    private String unidadEmbolsado;
    private String remitente;
    private String observaciones;

    private BigDecimal kilosLimpios;
    
    // Datos de humedad (múltiples conjuntos de tipo + valor)
    private List<DatosHumedadDTO> datosHumedad;
    
    // Número de artículo - un solo valor seleccionado del catálogo
    private String numeroArticulo;
    
    private Double cantidad;

    private String especie; //esto se saca de cultivar, por eso no va en la entidad lote
    private String origen;
    private String estado;
    private LocalDate fechaCosecha;

    private Boolean activo;
}
