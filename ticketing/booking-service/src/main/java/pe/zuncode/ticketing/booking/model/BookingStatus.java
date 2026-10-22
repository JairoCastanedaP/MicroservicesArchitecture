package pe.zuncode.ticketing.booking.model;

/**
 * Ciclo de vida de una reserva.
 * <pre>
 * PENDING ──confirm──► CONFIRMED (se emiten los tickets)
 *    │ ──cancel───► CANCELLED
 *    └ ──vence────► EXPIRED   (pasó el tiempo de retención sin confirmar)
 * </pre>
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    EXPIRED
}
