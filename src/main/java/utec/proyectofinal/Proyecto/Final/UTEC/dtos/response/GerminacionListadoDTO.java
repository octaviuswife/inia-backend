package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
@Data
public class GerminacionListadoDTO {
    private Long analisisID;
    private Estado estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String lote;
    private Long idLote;
    private LocalDate fechaInicioGerm;
    private LocalDate fechaUltConteo;
    private String numDias;
    private String usuarioCreador;
    private String usuarioModificador;
    private Boolean cumpleNorma; // true si NO está "A REPETIR"
    }