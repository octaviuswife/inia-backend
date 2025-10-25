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
    private String lote; // nomLote
    private Long idLote;
    private String especie; // Nombre de la especie del lote
    private Boolean activo;
    private LocalDate fechaInicioGerm;
    private LocalDate fechaUltConteo;
    private String numDias;
    
    // Campos para mostrar en listado - Germinación no necesita campos adicionales por ahora
    // pero se pueden agregar según necesidad
    
    private String usuarioCreador;
    private String usuarioModificador;
    private Boolean cumpleNorma; // true si NO está "A REPETIR"
}