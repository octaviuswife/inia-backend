package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PurezaDTO;

import java.util.List;

@Data
public class ResponseListadoPureza {
    private List<PurezaDTO> purezas;
}
