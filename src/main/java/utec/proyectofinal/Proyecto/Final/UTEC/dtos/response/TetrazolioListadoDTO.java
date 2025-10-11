package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

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
    private LocalDate fecha;
    private String usuarioCreador;
    private String usuarioModificador;
}
