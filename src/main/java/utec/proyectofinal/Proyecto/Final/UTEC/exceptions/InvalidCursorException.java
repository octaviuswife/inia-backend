package utec.proyectofinal.Proyecto.Final.UTEC.exceptions;

/**
 * Excepción lanzada cuando un cursor de paginación keyset es inválido o corrupto.
 * Debería resultar en un HTTP 400 Bad Request.
 */
public class InvalidCursorException extends RuntimeException {
    
    public InvalidCursorException(String message) {
        super(message);
    }
    
    public InvalidCursorException(String message, Throwable cause) {
        super(message, cause);
    }
}
