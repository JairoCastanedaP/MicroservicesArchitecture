package pe.zuncode.ticketing.event.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TicketCategoryRequest(@NotBlank(message = "es obligatorio")
                                            @Size(max = 80, message = "debe tener como máximo {max} caracteres")
                                            String name,

                                    @NotNull(message = "es obligatorio")
                                            @Positive(message = "debe ser mayor a 0")
                                            @Digits(integer = 8, fraction = 2, message = "debe tener como máximo 2 decimales")
                                    BigDecimal price,

                                    @NotNull(message = "es obligatorio")
                                            @Positive(message = "debe ser mayor a 0")
                                            Integer capacity) {
}
