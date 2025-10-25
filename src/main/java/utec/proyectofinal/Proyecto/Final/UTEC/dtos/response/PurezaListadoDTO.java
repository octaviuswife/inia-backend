package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Data
public class PurezaListadoDTO {
    private Long analisisID;
    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String lote;
    private Long idLote;
    private String especie; // Nombre de la especie del lote
    private Boolean activo;
    
    // Campos para mostrar en listado
    private BigDecimal redonSemillaPura; // Pureza INIA (%)
    private BigDecimal inasePura; // Pureza INASE (%)
    
    private String usuarioCreador;
    private String usuarioModificador;
}
