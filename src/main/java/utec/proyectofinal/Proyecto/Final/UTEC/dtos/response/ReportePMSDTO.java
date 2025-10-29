package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportePMSDTO {
    private Long totalPms;
    private Long muestrasConCVSuperado;
    private Double porcentajeMuestrasConCVSuperado;
    private Long muestrasConRepeticionesMaximas;
}
