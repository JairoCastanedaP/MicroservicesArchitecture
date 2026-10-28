package pe.zuncode.ticketing.event.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Zona que llega dentro de un {@link EventRequest}.
 */
public record TicketCategoryRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 80, message = "debe tener como máximo {max} caracteres")
        String name,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "debe tener como máximo 2 decimales")
        BigDecimal price,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Integer capacity
) {
}
