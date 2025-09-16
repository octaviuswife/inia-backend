package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

@Data
public class ListadoRequestDTO {
    private TipoListado listadoTipo;
    private String listadoInsti;
    private Integer listadoNum;
    private Long idCatalogo;  // Para seleccionar del catálogo precargado
}
