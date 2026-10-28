package pe.zuncode.ticketing.booking.client;

import java.math.BigDecimal;

/**
 * Respuesta de {@code GET .../availability} de event-service, vista desde booking-service.
 * <p>
 * <b>Tolerant reader:</b> event-service devuelve más campos (eventStatus, eventDateTime...),
 * pero aquí declaramos solo los que usamos; Jackson ignora el resto. Si mañana event-service
 * agrega un campo nuevo, booking-service no se rompe.
 * <p>
 * Es una copia del contrato, no una clase compartida: cada servicio tiene la suya.
 */
public record TicketAvailability(
        Long eventId,
        String eventName,
        Long ticketCategoryId,
        String ticketCategoryName,
        BigDecimal price,
        int availableTickets,
        boolean onSale
) {
}
