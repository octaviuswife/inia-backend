package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class AnalisisDTO {

    private Long analisisID;

    private Long idLote; // ID del lote
    private String lote; // Nombre del lote (nomLote)
    private String ficha; // Ficha del lote
    private String cultivarNombre; // Nombre del cultivar
    private String especieNombre; // Nombre de la especie

    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private String comentarios;

    private Boolean activo;

    private List<AnalisisHistorialDTO> historial;
}
