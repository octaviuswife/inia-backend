package utec.proyectofinal.Proyecto.Final.UTEC.exceptions;

/**
 * Excepción lanzada cuando una petición tiene datos inválidos o no cumple las validaciones de negocio
 * Se mapea a HTTP 400 Bad Request
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
