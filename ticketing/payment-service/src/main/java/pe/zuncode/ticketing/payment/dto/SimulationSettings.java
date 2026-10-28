package pe.zuncode.ticketing.payment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

/**
 * Configuración de la simulación de fallas. JSON de ejemplo:
 * <pre>{ "failureRate": 0.5, "delay": "PT3S" }</pre>
 * {@code delay} usa el formato ISO-8601 de {@link Duration}: PT3S = 3 segundos, PT0.5S = 500 ms.
 */
public record SimulationSettings(
        @DecimalMin(value = "0.0", message = "debe estar entre 0.0 y 1.0")
        @DecimalMax(value = "1.0", message = "debe estar entre 0.0 y 1.0")
        double failureRate,

        @NotNull(message = "es obligatorio")
        Duration delay
) {
}
