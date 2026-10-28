package pe.zuncode.ticketing.payment.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Solicitud de cobro. La envía booking-service, no el cliente final.
 */
public record PaymentRequest(
        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Long bookingId,

        @NotBlank(message = "es obligatorio")
        @Email(message = "debe ser un correo válido")
        @Size(max = 150, message = "debe tener como máximo {max} caracteres")
        String customerEmail,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        @Digits(integer = 10, fraction = 2, message = "debe tener como máximo 2 decimales")
        BigDecimal amount
) {
}
