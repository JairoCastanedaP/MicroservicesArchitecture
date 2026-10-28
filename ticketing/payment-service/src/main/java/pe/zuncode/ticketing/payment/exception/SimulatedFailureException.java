package pe.zuncode.ticketing.payment.exception;

/**
 * Falla provocada por {@code FailureSimulator} → 503 Service Unavailable.
 */
public class SimulatedFailureException extends RuntimeException {

    public SimulatedFailureException() {
        super("El procesador de pagos no está disponible (falla simulada)");
    }
}
