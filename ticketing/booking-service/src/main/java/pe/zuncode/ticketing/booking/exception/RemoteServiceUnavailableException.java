package pe.zuncode.ticketing.booking.exception;

/**
 * Otro microservicio no respondió a tiempo, falló o tiene el circuito abierto → 503 Service Unavailable.
 * <p>
 * Traduce los errores técnicos del cliente HTTP (FeignException, ResourceAccessException,
 * CallNotPermittedException...) a un error de nuestra API, sin filtrar detalles internos.
 */
public class RemoteServiceUnavailableException extends RuntimeException {

    private final String service;

    public RemoteServiceUnavailableException(String service, String message, Throwable cause) {
        super(message, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
