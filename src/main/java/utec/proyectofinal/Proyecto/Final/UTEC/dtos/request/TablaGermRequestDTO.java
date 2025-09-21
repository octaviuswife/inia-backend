package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class TablaGermRequestDTO {
    // total se calcula automáticamente como suma de totales de repeticiones
    
    private LocalDate fechaFinal;

    // Campos movidos desde Germinacion
    private String tratamiento;
    private String productoYDosis;
    private Integer numSemillasPRep;
    private String metodo;
    private Double temperatura;
    private String prefrio;
    private String pretratamiento;
}