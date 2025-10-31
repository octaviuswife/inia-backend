package utec.proyectofinal.Proyecto.Final.UTEC.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta genérica para endpoints REST
 * @param <T> Tipo de datos en la respuesta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    
    /**
     * Constructor para respuestas exitosas sin datos
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }
}
