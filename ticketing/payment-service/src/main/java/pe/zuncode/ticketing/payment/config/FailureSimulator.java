package pe.zuncode.ticketing.payment.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.zuncode.ticketing.payment.dto.SimulationSettings;
import pe.zuncode.ticketing.payment.exception.SimulatedFailureException;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * "Caos controlado" para la Sesión 3: hace que payment-service sea lento o falle a propósito,
 * para ver cómo reaccionan los timeouts, el Retry y el Circuit Breaker de booking-service.
 * <p>
 * Los valores arrancan con lo que diga application.yml y se pueden cambiar sin reiniciar
 * con {@code PUT /api/v1/simulation}. {@code volatile}: el cambio lo ven de inmediato
 * todos los hilos que atienden peticiones.
 * <p>
 * ⚠️ Esto existe solo para el curso. Un servicio real no expone un endpoint así.
 */
@Component
public class FailureSimulator {

    private static final Logger log = LoggerFactory.getLogger(FailureSimulator.class);

    private volatile SimulationSettings settings;

    public FailureSimulator(PaymentProperties properties) {
        this.settings = new SimulationSettings(properties.simulation().failureRate(),
                properties.simulation().delay());
    }

    public SimulationSettings getSettings() {
        return settings;
    }

    public SimulationSettings update(SimulationSettings newSettings) {
        this.settings = newSettings;
        log.warn("Simulación actualizada: failureRate={}, delay={}", newSettings.failureRate(), newSettings.delay());
        return newSettings;
    }

    /** Aplica la demora y, según la probabilidad configurada, lanza una falla. */
    public void apply() {
        SimulationSettings current = settings;
        sleep(current.delay());
        if (ThreadLocalRandom.current().nextDouble() < current.failureRate()) {
            log.warn("Falla simulada (failureRate={})", current.failureRate());
            throw new SimulatedFailureException();
        }
    }

    private static void sleep(Duration delay) {
        if (delay.isZero() || delay.isNegative()) {
            return;
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
