package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import java.util.List;

@Data
public class ResponseListadoDosn {
    private List<DosnDTO> dosns;
}
