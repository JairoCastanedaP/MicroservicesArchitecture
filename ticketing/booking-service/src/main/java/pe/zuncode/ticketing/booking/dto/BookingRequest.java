package pe.zuncode.ticketing.booking.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos para crear una reserva.
 * <p>
 * Deuda técnica consciente: hoy el cliente envía {@code unitPrice}. En la Sesión 3,
 * booking-service consultará el precio y la disponibilidad a event-service por HTTP
 * y este campo desaparecerá del request.
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
        Integer quantity,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "debe tener como máximo 2 decimales")
        BigDecimal unitPrice
) {
}
