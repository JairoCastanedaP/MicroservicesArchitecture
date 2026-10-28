package pe.zuncode.ticketing.event.dto;

import pe.zuncode.ticketing.event.model.Event;
import pe.zuncode.ticketing.event.model.EventStatus;
import pe.zuncode.ticketing.event.model.TicketCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sesión 3: lo que booking-service necesita saber para aceptar una reserva.
 * <p>
 * Es un contrato pensado para OTRO servicio (no para la pantalla del cliente): trae
 * solo los datos necesarios y una conclusión ya calculada ({@code onSale}), para que
 * booking-service no tenga que conocer las reglas internas del catálogo.
 * <p>
 * {@code availableTickets} es por ahora la capacidad de la zona. En la Sesión 7,
 * event-service descontará las entradas vendidas al recibir el evento {@code booking.confirmed}.
 */
public record TicketAvailabilityResponse(
        Long eventId,
        String eventName,
        EventStatus eventStatus,
        LocalDateTime eventDateTime,
        Long ticketCategoryId,
        String ticketCategoryName,
        BigDecimal price,
        int availableTickets,
        boolean onSale
) {

    public static TicketAvailabilityResponse from(Event event, TicketCategory category, LocalDateTime now) {
        boolean onSale = event.getStatus() == EventStatus.PUBLISHED && event.getDateTime().isAfter(now);
        return new TicketAvailabilityResponse(event.getId(), event.getName(), event.getStatus(),
                event.getDateTime(), category.getId(), category.getName(), category.getPrice(),
                category.getCapacity(), onSale);
    }
}
