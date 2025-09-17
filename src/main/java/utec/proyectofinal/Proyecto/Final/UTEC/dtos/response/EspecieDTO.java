package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;


import lombok.Data;
import java.util.List;

@Data
public class EspecieDTO {
    private Long especieID;

    private String nombreCientifico;
    private String nombreComun;

    private List<String> cultivares;
}
