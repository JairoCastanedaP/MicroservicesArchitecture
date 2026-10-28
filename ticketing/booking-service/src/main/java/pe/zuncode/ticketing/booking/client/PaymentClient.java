package pe.zuncode.ticketing.booking.client;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Cliente HTTP declarativo hacia payment-service, con las interfaces {@code @HttpExchange}
 * de Spring Framework (sin Spring Cloud).
 * <p>
 * Spring Boot crea la implementación sobre un {@code RestClient}; se registra en
 * {@code HttpClientsConfig} con {@code @ImportHttpServices}. La URL base y los timeouts se leen de
 * {@code spring.http.serviceclient.payment-service.*} (en application.yml).
 */
@HttpExchange("/api/v1/payments")
public interface PaymentClient {

    @PostExchange
    PaymentResult pay(@RequestBody PaymentRequest request);
}
