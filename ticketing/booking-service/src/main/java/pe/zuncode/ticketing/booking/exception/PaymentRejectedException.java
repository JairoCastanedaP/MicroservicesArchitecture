package pe.zuncode.ticketing.booking.exception;

/**
 * payment-service respondió bien, pero rechazó el cobro → 402 Payment Required.
 * La reserva sigue PENDING: el cliente puede volver a intentar mientras no venza.
 */
public class PaymentRejectedException extends RuntimeException {

    public PaymentRejectedException(String message) {
        super(message);
    }
}
