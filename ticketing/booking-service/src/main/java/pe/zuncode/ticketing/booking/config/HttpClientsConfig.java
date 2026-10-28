package pe.zuncode.ticketing.booking.config;

import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import java.util.Optional;
import pe.zuncode.ticketing.booking.client.EventClient;
import pe.zuncode.ticketing.booking.client.PaymentClient;

/**
 * Registro de los clientes HTTP hacia otros microservicios.
 * <ul>
 *   <li>{@code @EnableFeignClients}: busca interfaces {@code @FeignClient} (EventClient) y crea su implementación.</li>
 *   <li>{@code @ImportHttpServices}: crea la implementación de las interfaces {@code @HttpExchange}
 *       (PaymentClient) sobre un RestClient. El nombre del grupo, "payment-service", es la clave
 *       de su configuración: {@code spring.http.serviceclient.payment-service.*}.</li>
 * </ul>
 * Sesión 4: <b>propagación del token</b> (token relay). booking-service llama a los otros servicios
 * EN NOMBRE del usuario, así que reenvía su mismo header {@code Authorization: Bearer <jwt>}.
 * payment-service lo valida igual que si la petición viniera directo del cliente.
 */
@Configuration
@EnableFeignClients(clients = EventClient.class)
@ImportHttpServices(group = "payment-service", types = PaymentClient.class)
public class HttpClientsConfig {

    // OpenFeign: un RequestInterceptor se ejecuta antes de CADA llamada de los clientes Feign
    @Bean
    RequestInterceptor bearerTokenRelayForFeign() {
        return template -> currentBearerToken()
                .ifPresent(token -> template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    // RestClient (@HttpExchange): agrega un interceptor a los clientes del grupo "payment-service"
    @Bean
    RestClientHttpServiceGroupConfigurer bearerTokenRelayForPaymentService() {
        return groups -> groups.filterByName("payment-service").forEachClient((group, builder) ->
                builder.requestInterceptor((request, body, execution) -> {
                    currentBearerToken().ifPresent(token -> request.getHeaders().setBearerAuth(token));
                    return execution.execute(request, body);
                }));
    }

    // El token de la petición en curso: Spring Security lo guardó en el SecurityContext al validarlo
    private static Optional<String> currentBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Optional.of(jwtAuthentication.getToken().getTokenValue());
        }
        return Optional.empty();
    }
}
