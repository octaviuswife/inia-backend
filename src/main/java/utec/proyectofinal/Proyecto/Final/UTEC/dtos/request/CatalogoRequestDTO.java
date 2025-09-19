package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class CatalogoRequestDTO {
    private String tipo; // HUMEDAD, ORIGEN, ARTICULO, ESTADO, DEPOSITO
    private String valor;
    private Boolean activo = true;
}