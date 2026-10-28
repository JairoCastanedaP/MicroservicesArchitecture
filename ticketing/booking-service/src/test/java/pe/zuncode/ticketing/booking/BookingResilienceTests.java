package pe.zuncode.ticketing.booking;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sesión 3: pruebas de la comunicación con event-service y payment-service.
 * <p>
 * WireMock levanta un servidor HTTP falso que hace de event-service Y de payment-service
 * (las URLs de ambos clientes apuntan a él). Así podemos simular respuestas lentas,
 * errores 5xx o 404 sin levantar los servicios reales, y contar cuántas llamadas llegaron.
 * <p>
 * Para que corran rápido se acortan los timeouts (500 ms) y la espera entre reintentos (50 ms).
 * <p>
 * Sesión 4: las peticiones llevan un token simulado de la clienta Ana ({@link #ana()}).
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking_test",
        "spring.cloud.openfeign.client.config.event-service.read-timeout=500",
        "spring.http.serviceclient.payment-service.read-timeout=500ms",
        "resilience4j.retry.instances.event-service.wait-duration=50ms"
})
@AutoConfigureMockMvc
class BookingResilienceTests {

    private static final String AVAILABILITY_URL = "/api/v1/events/1/ticket-categories/1/availability";
    private static final String AVAILABILITY_JSON = """
            { "eventId": 1, "eventName": "Rock en el Nacional", "eventStatus": "PUBLISHED",
              "ticketCategoryId": 1, "ticketCategoryName": "Campo VIP",
              "price": 450.00, "availableTickets": 5000, "onSale": true }
            """;
    private static final String BOOKING_JSON = """
            { "eventId": 1, "ticketCategoryId": 1, "quantity": 2 }
            """;

    // http2PlainDisabled: el cliente HTTP del JDK (el que usa RestClient por defecto) intenta pasar
    // a HTTP/2 y el WireMock embebido corta esos POST. Con HTTP/1.1 se comporta como un servicio real.
    static final WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort().http2PlainDisabled(true));

    static {
        wireMock.start();
    }

    @DynamicPropertySource
    static void remoteServiceUrls(DynamicPropertyRegistry registry) {
        // Los dos clientes apuntan al WireMock
        registry.add("EVENT_SERVICE_URL", wireMock::baseUrl);
        registry.add("PAYMENT_SERVICE_URL", wireMock::baseUrl);
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void reset() {
        wireMock.resetAll();
        // cada test empieza con los circuitos CLOSED y sin estadísticas
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
    }

    @Nested
    class CreateBooking {

        @Test
        void takesThePriceFromEventService() throws Exception {
            wireMock.stubFor(get(AVAILABILITY_URL).willReturn(okJson(AVAILABILITY_JSON)));

            createBooking()
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.unitPrice").value(450.0))
                    .andExpect(jsonPath("$.totalAmount").value(900.0))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        void rejectsEventNotOnSale() throws Exception {
            wireMock.stubFor(get(AVAILABILITY_URL).willReturn(okJson(
                    AVAILABILITY_JSON.replace("\"onSale\": true", "\"onSale\": false"))));

            createBooking()
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("El evento Rock en el Nacional no está a la venta"));
        }

        @Test
        void notFoundIsAClientErrorAndIsNotRetried() throws Exception {
            wireMock.stubFor(get(AVAILABILITY_URL).willReturn(aResponse().withStatus(404)));

            createBooking().andExpect(status().isBadRequest());

            wireMock.verify(1, getRequestedFor(urlEqualTo(AVAILABILITY_URL)));
        }

        @Test
        void retriesATransientFailureAndSucceeds() throws Exception {
            // 1.ª llamada: 503; 2.ª llamada: 200 (escenario con estados de WireMock)
            wireMock.stubFor(get(AVAILABILITY_URL).inScenario("flaky")
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse().withStatus(503))
                    .willSetStateTo("recovered"));
            wireMock.stubFor(get(AVAILABILITY_URL).inScenario("flaky")
                    .whenScenarioStateIs("recovered")
                    .willReturn(okJson(AVAILABILITY_JSON)));

            createBooking().andExpect(status().isCreated());

            wireMock.verify(2, getRequestedFor(urlEqualTo(AVAILABILITY_URL)));
        }

        @Test
        void returns503AfterExhaustingRetries() throws Exception {
            wireMock.stubFor(get(AVAILABILITY_URL).willReturn(aResponse().withStatus(500)));

            createBooking()
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.service").value("event-service"));

            wireMock.verify(3, getRequestedFor(urlEqualTo(AVAILABILITY_URL))); // max-attempts: 3
        }

        @Test
        void slowResponseIsCutByTheReadTimeout() throws Exception {
            wireMock.stubFor(get(AVAILABILITY_URL).willReturn(okJson(AVAILABILITY_JSON).withFixedDelay(1500)));

            long start = System.currentTimeMillis();
            createBooking().andExpect(status().isServiceUnavailable());
            long elapsed = System.currentTimeMillis() - start;

            wireMock.verify(3, getRequestedFor(urlEqualTo(AVAILABILITY_URL)));
            // 3 intentos de ~500 ms (no de 1500 ms) + esperas entre reintentos
            assertThat(elapsed).isLessThan(3000);
        }

        @Test
        void circuitOpensAndStopsCallingEventService() throws Exception {
            wireMock.stubFor(get(AVAILABILITY_URL).willReturn(aResponse().withStatus(500)));

            createBooking().andExpect(status().isServiceUnavailable()); // 3 llamadas fallidas
            createBooking().andExpect(status().isServiceUnavailable()); // 2 más → 5 de 5 fallan → OPEN
            assertThat(circuitBreakerRegistry.circuitBreaker("event-service").getState())
                    .isEqualTo(CircuitBreaker.State.OPEN);

            createBooking().andExpect(status().isServiceUnavailable()); // falla rápido, sin llamar

            wireMock.verify(5, getRequestedFor(urlEqualTo(AVAILABILITY_URL)));
        }
    }

    @Nested
    class PayBooking {

        @BeforeEach
        void availability() {
            wireMock.stubFor(get(AVAILABILITY_URL).willReturn(okJson(AVAILABILITY_JSON)));
        }

        @Test
        void approvedPaymentConfirmsTheBooking() throws Exception {
            wireMock.stubFor(post("/api/v1/payments").willReturn(aResponse().withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            { "id": 77, "bookingId": 1, "amount": 900.00, "status": "APPROVED" }
                            """)));
            long id = createdBookingId();

            pay(id)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.paymentId").value(77));
        }

        @Test
        void rejectedPaymentKeepsTheBookingPending() throws Exception {
            wireMock.stubFor(post("/api/v1/payments").willReturn(aResponse().withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                            { "id": 78, "status": "REJECTED", "rejectionReason": "Fondos insuficientes" }
                            """)));
            long id = createdBookingId();

            pay(id).andExpect(status().isPaymentRequired());

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/{id}", id).with(ana()))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        // Sesión 4: booking-service reenvía a payment-service el MISMO token de la clienta (token relay)
        @Test
        void paymentServiceReceivesTheCustomerToken() throws Exception {
            wireMock.stubFor(post("/api/v1/payments").willReturn(aResponse().withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"id\": 80, \"status\": \"APPROVED\" }")));
            long id = createdBookingId();

            pay(id).andExpect(status().isOk());

            wireMock.verify(postRequestedFor(urlPathMatching("/api/v1/payments"))
                    .withHeader("Authorization", equalTo("Bearer token-de-ana"))
                    .withRequestBody(matchingJsonPath(
                            "$.customerEmail", equalTo("ana@mail.com"))));
        }

        @Test
        void failedPaymentIsNotRetried() throws Exception {
            wireMock.stubFor(post("/api/v1/payments").willReturn(aResponse().withStatus(503)));
            long id = createdBookingId();

            pay(id)
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.service").value("payment-service"));

            // un POST de pago NO se reintenta: podría cobrar dos veces
            wireMock.verify(1, postRequestedFor(urlPathMatching("/api/v1/payments")));
        }

        @Test
        void slowPaymentTimesOutAndTheBookingStaysPending() throws Exception {
            wireMock.stubFor(post("/api/v1/payments").willReturn(aResponse().withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"id\": 79, \"status\": \"APPROVED\" }")
                    .withFixedDelay(1500)));
            long id = createdBookingId();

            pay(id).andExpect(status().isServiceUnavailable());

            // payment-service SÍ procesó el cobro (respondió tarde), pero booking-service no se enteró:
            // la reserva sigue PENDING. Este es el problema que resuelven la idempotencia y la
            // consistencia eventual (Sesión 8).
            wireMock.verify(1, postRequestedFor(urlPathMatching("/api/v1/payments")));
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/{id}", id).with(ana()))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        private long createdBookingId() throws Exception {
            String body = createBooking().andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
        }

        private ResultActions pay(long id) throws Exception {
            return mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/bookings/{id}/pay", id).with(ana()));
        }
    }

    private ResultActions createBooking() throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(BOOKING_JSON)
                .with(ana()));
    }

    // Token (simulado) de la clienta Ana: sub = ana@mail.com, rol CUSTOMER
    static RequestPostProcessor ana() {
        return jwt().jwt(token -> token.tokenValue("token-de-ana").subject("ana@mail.com"))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
}
