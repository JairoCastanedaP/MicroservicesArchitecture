package pe.zuncode.ticketing.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear una reserva.
 * <p>
 * Sesión 3: ya no trae {@code unitPrice}. El precio lo decide el dueño del catálogo:
 * booking-service se lo pide a event-service al reservar. Así el cliente no puede
 * enviar el precio que quiera.
 */
public record BookingRequest(
        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Long eventId,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Long ticketCategoryId,

        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un correo válido")
        @Size(max = 150, message = "debe tener como máximo {max} caracteres")
        String customerEmail,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser al menos {value}")
        @Max(value = 10, message = "no puede superar {value} entradas por reserva")
        Integer quantity
) {
}
