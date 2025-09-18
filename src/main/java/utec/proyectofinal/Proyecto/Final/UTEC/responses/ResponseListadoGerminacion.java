package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;

import java.util.List;

@Data
public class ResponseListadoGerminacion {
    private List<GerminacionDTO> germinaciones;
}