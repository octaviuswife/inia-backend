package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DatosHumedadDTO {
    private Long datosHumedadID;
    private Long tipoHumedadID;
    private String tipoHumedadValor; // El valor del catálogo para mostrar
    private BigDecimal valor;
}