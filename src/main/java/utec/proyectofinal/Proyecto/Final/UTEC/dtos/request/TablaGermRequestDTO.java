package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import java.time.LocalDate;
import java.util.List;

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
    private String temperatura;
    
    // Campos de prefrío (boolean + descripción + días)
    private Boolean tienePrefrio;
    private String descripcionPrefrio;
    
    // Campos de pretratamiento (boolean + descripción, sin días)
    private Boolean tienePretratamiento;
    private String descripcionPretratamiento;
    
    // Campos de fechas y control de conteos movidos desde Germinacion
    private LocalDate fechaIngreso; // Nueva fecha de ingreso
    private LocalDate fechaGerminacion; // Renombrado de fechaInicioGerm
    private List<LocalDate> fechaConteos;
    private LocalDate fechaUltConteo;
    private String numDias;
    
    // Campos de control
    private Integer numeroRepeticiones;
    private Integer numeroConteos;
    
    // Campo para días de prefrío (pretratamiento ya no tiene días)
    private Integer diasPrefrio;
}