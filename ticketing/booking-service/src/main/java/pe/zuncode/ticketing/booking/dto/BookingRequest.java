package pe.zuncode.ticketing.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Datos para crear una reserva.
 * <p>
 * Sesión 3: ya no trae {@code unitPrice}. El precio lo decide el dueño del catálogo:
 * booking-service se lo pide a event-service al reservar. Así el cliente no puede
 * enviar el precio que quiera.
 * <p>
 * Sesión 4: tampoco trae {@code customerEmail}. El cliente es el usuario del token (claim "sub"):
 * nadie puede reservar a nombre de otro.
 */
public record BookingRequest(
        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Long eventId,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Long ticketCategoryId,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser al menos {value}")
        @Max(value = 10, message = "no puede superar {value} entradas por reserva")
        Integer quantity
) {
}
