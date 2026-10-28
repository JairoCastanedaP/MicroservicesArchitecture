package pe.zuncode.ticketing.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sesión 4: reglas de acceso de payment-service.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:payment_security_test")
@AutoConfigureMockMvc
class PaymentSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void payingRequiresAToken() throws Exception {
        mockMvc.perform(pay()).andExpect(status().isUnauthorized());
    }

    @Test
    void aCustomerCanPay() throws Exception {
        mockMvc.perform(pay().with(as("CUSTOMER"))).andExpect(status().isCreated());
    }

    @Test
    void onlyAnAdminCanListPayments() throws Exception {
        mockMvc.perform(get("/api/v1/payments").with(as("CUSTOMER"))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/payments").with(as("ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void theSimulationIsPublicForTheCourse() throws Exception {
        mockMvc.perform(get("/api/v1/simulation")).andExpect(status().isOk());
    }

    private static MockHttpServletRequestBuilder pay() {
        return post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content("""
                { "bookingId": 10, "customerEmail": "ana@mail.com", "amount": 900.00 }
                """);
    }

    private static RequestPostProcessor as(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
