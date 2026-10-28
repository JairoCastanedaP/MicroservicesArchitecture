package pe.zuncode.ticketing.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Propiedades {@code ticketing.jwt.*}.
 *
 * @param issuer quién emite el token (claim "iss")
 * @param ttl    vigencia del token de acceso
 */
@ConfigurationProperties(prefix = "ticketing.jwt")
public record JwtProperties(String issuer, Duration ttl) {
}
