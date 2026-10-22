package pe.zuncode.ticketing.booking.exception;

/**
 * La operación choca con el estado actual de la reserva
 * (p. ej. confirmar una reserva cancelada) → 409 Conflict.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
