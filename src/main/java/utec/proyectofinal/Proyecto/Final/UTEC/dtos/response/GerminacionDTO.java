package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.OneToMany;
import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ContGerm;

import java.time.LocalDate;
import java.util.List;

@Data
public class GerminacionDTO extends AnalisisDTO{
    private LocalDate fechaInicio;

    private List<LocalDate> fechaConteos;

    private LocalDate fechaFin;

    private String tratamiento;
    private String productoYDosis;
    private Integer numSemillasPRep;
    private String metodo;
    private Double temperatura;
    private String prefrio;
    private String pretratamiento;
    private String numDias;

    private List<ContGermDTO> contGerm;
}
