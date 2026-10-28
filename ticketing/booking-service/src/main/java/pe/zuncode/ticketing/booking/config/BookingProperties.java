package pe.zuncode.ticketing.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuración tipada de las propiedades {@code ticketing.booking.*}.
 * Spring convierte "10m" o "30s" en un {@link Duration}.
 */
@ConfigurationProperties(prefix = "ticketing.booking")
public record BookingProperties(@DefaultValue("10m") Duration holdTime) {
}
