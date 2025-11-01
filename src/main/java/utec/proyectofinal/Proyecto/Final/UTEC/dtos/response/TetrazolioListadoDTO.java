package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Data
public class TetrazolioListadoDTO {
    private Long analisisID;
    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String lote;
    private Long idLote;
    private String especie; // Nombre de la especie del lote
    private Boolean activo;
    private LocalDate fecha;
    
    // Viabilidad con redondeo (Viabilidad INIA %)
    private BigDecimal viabilidadConRedondeo;
    
    // Viabilidad INASE %
    private BigDecimal viabilidadInase;
    
    private String usuarioCreador;
    private String usuarioModificador;
}
