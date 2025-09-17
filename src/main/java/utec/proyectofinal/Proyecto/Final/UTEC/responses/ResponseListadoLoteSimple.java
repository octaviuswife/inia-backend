package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteSimpleDTO;

import java.util.List;

@Data
public class ResponseListadoLoteSimple {
    private List<LoteSimpleDTO> lotes;
}