package utec.proyectofinal.Proyecto.Final.UTEC.exceptions;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la base de datos
 * Se mapea a HTTP 404 Not Found
 */
public class NotFoundException extends RuntimeException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
