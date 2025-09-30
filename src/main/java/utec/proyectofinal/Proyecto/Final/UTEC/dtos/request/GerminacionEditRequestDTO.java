package utec.proyectofinal.Proyecto.Final.UTEC.dtos.request;

import lombok.Data;

@Data
public class GerminacionEditRequestDTO {
    // Solo campos editables después de la creación
    private Long idLote;
    private String comentarios;
    
}