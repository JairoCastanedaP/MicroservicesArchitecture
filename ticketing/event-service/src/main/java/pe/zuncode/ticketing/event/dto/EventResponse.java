package pe.zuncode.ticketing.event.dto;

import pe.zuncode.ticketing.event.model.Event;
import pe.zuncode.ticketing.event.model.EventCategory;
import pe.zuncode.ticketing.event.model.EventStatus;
import pe.zuncode.ticketing.event.model.Venue;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representación de un evento en la API.
 * <p>
 * Su forma NO tiene que ser igual a la entidad: aquí devolvemos un resumen del recinto
 * (para que el cliente no tenga que hacer otra llamada) y el total de entradas calculado.
 */
public record EventResponse(
        Long id,
        String name,
        String description,
        EventCategory category,
        EventStatus status,
        LocalDateTime dateTime,
        VenueSummary venue,
        int totalCapacity,
        List<TicketCategoryResponse> ticketCategories
) {

    public record VenueSummary(Long id, String name, String city) {

        static VenueSummary from(Venue venue) {
            return new VenueSummary(venue.getId(), venue.getName(), venue.getCity());
        }
    }

    /**
     * Debe llamarse dentro de una transacción: lee relaciones LAZY (venue y ticketCategories).
     */
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getCategory(),
                event.getStatus(),
                event.getDateTime(),
                VenueSummary.from(event.getVenue()),
                event.totalCapacity(),
                event.getTicketCategories().stream().map(TicketCategoryResponse::from).toList());
    }
}
