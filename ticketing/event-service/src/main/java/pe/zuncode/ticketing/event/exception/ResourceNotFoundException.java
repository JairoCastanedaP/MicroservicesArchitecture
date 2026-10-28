package pe.zuncode.ticketing.event.exception;

/**
 * El recurso solicitado no existe → 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " no encontrado: " + id);
    }
}
