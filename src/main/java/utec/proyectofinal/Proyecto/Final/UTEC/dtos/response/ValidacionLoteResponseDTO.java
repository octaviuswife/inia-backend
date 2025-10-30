package utec.proyectofinal.Proyecto.Final.UTEC.dtos.response;

import lombok.Data;

@Data
public class ValidacionLoteResponseDTO {
    private boolean fichaExiste;
    private boolean nomLoteExiste;
    
    // Constructor sin argumentos
    public ValidacionLoteResponseDTO() {
        this.fichaExiste = false;
        this.nomLoteExiste = false;
    }
}