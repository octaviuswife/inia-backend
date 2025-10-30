package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Data
public class PmsListadoDTO {
    private Long analisisID;
    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String lote; // nomLote
    private Long idLote;
    private String especie; // Nombre de la especie del lote
    private Boolean activo;
    
    // Campos para mostrar en listado
    private BigDecimal pms_g; // PMS en gramos
    private BigDecimal coeficienteVariacion; // Coeficiente de Variación
    
    private String usuarioCreador;
    private String usuarioModificador;
}
