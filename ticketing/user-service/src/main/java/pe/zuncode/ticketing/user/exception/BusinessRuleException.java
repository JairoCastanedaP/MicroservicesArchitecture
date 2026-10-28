package pe.zuncode.ticketing.user.exception;

/**
 * Se viola una regla de negocio (por ejemplo, el correo ya está registrado). Se responde con 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
