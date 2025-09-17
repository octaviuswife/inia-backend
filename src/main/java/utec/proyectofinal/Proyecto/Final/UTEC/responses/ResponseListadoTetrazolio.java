package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;

import java.util.List;

@Data
public class ResponseListadoTetrazolio {
    private List<TetrazolioDTO> tetrazolios;
}