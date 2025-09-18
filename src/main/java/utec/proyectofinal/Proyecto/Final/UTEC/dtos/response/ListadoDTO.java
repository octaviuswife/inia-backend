package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

@Data
public class ListadoDTO {
    private Long listadoID;

    private TipoListado listadoTipo;
    private Instituto listadoInsti;
    private Integer listadoNum;
    private CatalogoDTO catalogo;
}
