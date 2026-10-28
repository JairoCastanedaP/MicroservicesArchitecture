package pe.zuncode.ticketing.booking.exception;

/**
 * La solicitud hace referencia a datos que no existen (p. ej. un evento o una zona
 * inexistentes en event-service) → 400 Bad Request.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
