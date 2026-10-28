package pe.zuncode.ticketing.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Configuración tipada de {@code ticketing.payment.*}.
 *
 * @param maxAmount  límite de la "tarjeta": un pago mayor se rechaza (REJECTED) de forma determinista
 * @param simulation valores INICIALES de la simulación de fallas; se pueden cambiar en caliente
 *                   con {@code PUT /api/v1/simulation} (ver {@link FailureSimulator})
 */
@ConfigurationProperties(prefix = "ticketing.payment")
public record PaymentProperties(@DefaultValue("2000.00") BigDecimal maxAmount,
                                @DefaultValue Simulation simulation) {

    /**
     * @param failureRate probabilidad (0.0 a 1.0) de que un pago falle con 503
     * @param delay       demora artificial de cada pago (simula un procesador lento)
     */
    public record Simulation(@DefaultValue("0.0") double failureRate,
                             @DefaultValue("0ms") Duration delay) {
    }
}
