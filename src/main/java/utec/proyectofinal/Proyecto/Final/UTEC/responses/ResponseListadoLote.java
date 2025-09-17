package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;

import java.util.List;

@Data
public class ResponseListadoLote {
    private List<LoteDTO> lotes;
}