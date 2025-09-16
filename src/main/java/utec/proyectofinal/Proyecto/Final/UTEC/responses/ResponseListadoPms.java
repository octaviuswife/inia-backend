package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import java.util.List;

@Data
public class ResponseListadoPms {
    private List<PmsDTO> pms;
}
