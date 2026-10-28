package pe.zuncode.ticketing.event.exception;

/**
 * La operación choca con el estado actual del recurso
 * (p. ej. publicar un evento cancelado) → 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
