package pe.zuncode.ticketing.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos que el cliente envía para crear un recinto.
 * No tiene {@code id}: el cliente ya no puede "inventarlo".
 */
public record VenueRequest(
        @NotBlank(message = "es obligatorio")
        @Size(max = 120, message = "debe tener como máximo {max} caracteres")
        String name,

        @NotBlank(message = "es obligatorio")
        @Size(max = 80, message = "debe tener como máximo {max} caracteres")
        String city,

        @Size(max = 200, message = "debe tener como máximo {max} caracteres")
        String address,

        @NotNull(message = "es obligatorio")
        @Positive(message = "debe ser mayor a 0")
        Integer capacity
) {
}
