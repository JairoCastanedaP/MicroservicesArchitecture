package pe.zuncode.ticketing.booking.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;
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
 */
@Configuration
@EnableFeignClients(clients = EventClient.class)
@ImportHttpServices(group = "payment-service", types = PaymentClient.class)
public class HttpClientsConfig {
}
