package pe.zuncode.ticketing.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Cliente HTTP declarativo hacia event-service, con OpenFeign.
 * <p>
 * Solo declaramos la interfaz con las mismas anotaciones de un controlador (@GetMapping,
 * @PathVariable): Spring Cloud OpenFeign genera la implementación que arma la URL, hace la
 * llamada y convierte el JSON en {@link TicketAvailability}.
 * <p>
 * La URL y los timeouts NO están aquí: se leen de
 * {@code spring.cloud.openfeign.client.config.event-service.*} (en application.yml).
 */
@FeignClient(name = "event-service")
public interface EventClient {

    @GetMapping("/api/v1/events/{eventId}/ticket-categories/{ticketCategoryId}/availability")
    TicketAvailability getAvailability(@PathVariable("eventId") Long eventId,
                                       @PathVariable("ticketCategoryId") Long ticketCategoryId);
}
