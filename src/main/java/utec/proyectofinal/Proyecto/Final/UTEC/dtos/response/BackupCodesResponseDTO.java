package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * DTO de respuesta con códigos de respaldo generados
 * 
 * IMPORTANTE: Los códigos se muestran UNA SOLA VEZ.
 * El usuario debe guardarlos en un lugar seguro.
 */
@Data
@AllArgsConstructor
public class BackupCodesResponseDTO {
    private List<String> codes;
    private int totalCodes;
    private String mensaje;
}
