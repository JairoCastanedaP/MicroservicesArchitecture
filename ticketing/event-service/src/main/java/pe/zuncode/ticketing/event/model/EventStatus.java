package pe.zuncode.ticketing.event.model;

/**
 * Ciclo de vida de un evento.
 * DRAFT -> PUBLISHED (visible para la venta) -> CANCELLED
 */
public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED
}
