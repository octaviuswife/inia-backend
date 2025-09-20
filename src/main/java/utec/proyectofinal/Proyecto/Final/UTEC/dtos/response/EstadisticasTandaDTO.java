package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasTandaDTO {
    
    private BigDecimal promedio;
    private BigDecimal desviacion;
    private BigDecimal coeficienteVariacion;
    private BigDecimal pmsSinRedondeo;
    
}