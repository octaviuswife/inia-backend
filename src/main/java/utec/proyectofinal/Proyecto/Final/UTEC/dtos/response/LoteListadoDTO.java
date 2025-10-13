package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.time.LocalDate;
import lombok.Data;

@Data
public class LoteListadoDTO {
    private Long loteID;
    private String ficha;
    private String numeroFicha;
    private LocalDate fechaCosecha;
    private Boolean activo;
}
