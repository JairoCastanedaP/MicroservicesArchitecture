package pe.zuncode.ticketing.event.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.zuncode.ticketing.event.model.EventCategory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos que el cliente envía para crear o reemplazar un evento.
 * <p>
 * Ya no tiene {@code id} ni {@code status}: el cliente no puede enviarlos
 * (en la Sesión 1 el servicio tenía que "neutralizarlos" a mano).
 */
public record EventRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 150, message = "debe tener como máximo {max} caracteres")
        String name,

        @Size(max = 1000, message = "debe tener como máximo {max} caracteres")
        String description,

        @NotNull(message = "es obligatorio")
        EventCategory category,

        @NotNull(message = "es obligatorio")
        Long venueId,

        @NotNull(message = "es obligatorio")
        @Future(message = "debe ser una fecha futura")
        LocalDateTime dateTime,

        // @Valid propaga la validación a cada zona de la lista
        @Valid
        List<TicketCategoryRequest> ticketCategories
) {

    /** Si el cliente no envía zonas, trabajamos con una lista vacía en lugar de null. */
    public EventRequest {
        ticketCategories = ticketCategories == null ? List.of() : ticketCategories;
    }

    public int totalCapacity() {
        return ticketCategories.stream().mapToInt(TicketCategoryRequest::capacity).sum();
    }
}
