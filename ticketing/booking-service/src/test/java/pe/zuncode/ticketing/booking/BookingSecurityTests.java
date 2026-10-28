package pe.zuncode.ticketing.booking;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sesión 4: roles y propiedad de las reservas.
 * <p>
 * El rol decide QUÉ operación puedes hacer (SecurityConfig); el dueño de la reserva decide
 * SOBRE QUÉ reservas puedes hacerla (BookingService).
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:booking_security_test")
@AutoConfigureMockMvc
class BookingSecurityTests {

    static final WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort().http2PlainDisabled(true));

    static {
        wireMock.start();
    }

    @DynamicPropertySource
    static void remoteServiceUrls(DynamicPropertyRegistry registry) {
        // event-service es el WireMock
        registry.add("EVENT_SERVICE_URL", wireMock::baseUrl);
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void availability() {
        wireMock.stubFor(get("/api/v1/events/1/ticket-categories/1/availability").willReturn(okJson("""
                { "eventId": 1, "eventName": "Rock en el Nacional", "eventStatus": "PUBLISHED",
                  "ticketCategoryId": 1, "ticketCategoryName": "Campo VIP",
                  "price": 450.00, "availableTickets": 5000, "onSale": true }
                """)));
    }

    @Test
    void bookingWithoutATokenIs401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"eventId\": 1, \"ticketCategoryId\": 1, \"quantity\": 2 }"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anOrganizerCannotBuyTickets() throws Exception {
        createBooking(user("organizer@ticketing.pe", "ORGANIZER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void theCustomerComesFromTheTokenNotFromTheBody() throws Exception {
        createBooking(user("ana@mail.com", "CUSTOMER"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerEmail").value("ana@mail.com"));
    }

    @Test
    void aCustomerCannotSeeSomeoneElsesBooking() throws Exception {
        long id = createdBookingId(user("ana@mail.com", "CUSTOMER"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/{id}", id)
                        .with(user("luis@mail.com", "CUSTOMER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/{id}", id)
                        .with(user("admin@ticketing.pe", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void aCustomerCannotCancelSomeoneElsesBooking() throws Exception {
        long id = createdBookingId(user("ana@mail.com", "CUSTOMER"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/bookings/{id}/cancel", id)
                        .with(user("luis@mail.com", "CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    private ResultActions createBooking(RequestPostProcessor user) throws Exception {
        // El body trae otro correo: se ignora (BookingRequest ya no tiene customerEmail)
        return mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "eventId": 1, "ticketCategoryId": 1, "quantity": 2, "customerEmail": "otro@mail.com" }
                        """)
                .with(user));
    }

    private long createdBookingId(RequestPostProcessor user) throws Exception {
        String body = createBooking(user).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private static RequestPostProcessor user(String email, String role) {
        return jwt().jwt(token -> token.subject(email))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
