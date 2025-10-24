package utec.proyectofinal.Proyecto.Final.UTEC.exceptions;

/**
 * Excepción lanzada cuando hay un conflicto de estado o de recursos duplicados
 * Se mapea a HTTP 409 Conflict
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
