package pe.zuncode.ticketing.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Seguridad del gateway (variante reactiva: {@code ServerHttpSecurity}, no {@code HttpSecurity}).
 * <p>
 * El gateway hace un control <b>grueso</b>: deja pasar lo público y exige un token válido para el resto.
 * Los <b>roles</b> los revisa cada microservicio, que conoce sus reglas de negocio (defensa en profundidad:
 * si alguien llega a un servicio sin pasar por el gateway, el servicio igual valida el token).
 * <p>
 * El header {@code Authorization} se reenvía tal cual al servicio de destino.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/events/**", "/api/v1/venues/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()          // solo para desarrollo
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(problemDetailEntryPoint()))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(problemDetailEntryPoint()))
                .build();
    }

    // 401 con el mismo formato ProblemDetail que responden los microservicios
    private static ServerAuthenticationEntryPoint problemDetailEntryPoint() {
        return (exchange, exception) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
            String json = """
                    {"title":"No autenticado","status":401,"detail":"Necesitas un token válido: \
                    envía el header Authorization: Bearer <token>","instance":"%s"}"""
                    .formatted(exchange.getRequest().getPath().value());
            DataBuffer body = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(body));
        };
    }
}
