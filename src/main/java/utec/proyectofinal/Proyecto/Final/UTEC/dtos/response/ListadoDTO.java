package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

@Data
public class ListadoDTO {
    private Long listadoID;

    private TipoListado listadoTipo;
    private String listadoInsti;
    private Integer listadoNum;
    private Boolean maleza;

    private ListadoDTO listado;

}
