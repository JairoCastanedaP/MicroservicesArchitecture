package pe.zuncode.ticketing.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sesión 4: reglas de acceso de event-service.
 * <p>
 * {@code jwt()} simula un token ya validado con los roles que indiquemos: no hace falta levantar
 * user-service ni firmar tokens de verdad. Lo que se prueba es la configuración de SecurityConfig.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:event_security_test")
@AutoConfigureMockMvc
class EventSecurityTests {

    private static final String VENUE_JSON = """
            { "name": "Coliseo Amauta", "city": "Lima", "address": "Av. Brasil s/n", "capacity": 8000 }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void theCatalogIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/events")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/venues")).andExpect(status().isOk());
    }

    @Test
    void writingWithoutATokenIs401() throws Exception {
        mockMvc.perform(createVenue())
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void aCustomerCannotCreateVenues() throws Exception {
        mockMvc.perform(createVenue().with(as("CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"));
    }

    @Test
    void anOrganizerCanCreateVenues() throws Exception {
        mockMvc.perform(createVenue().with(as("ORGANIZER")))
                .andExpect(status().isCreated());
    }

    @Test
    void onlyAnAdminCanDeleteEvents() throws Exception {
        mockMvc.perform(delete("/api/v1/events/999").with(as("ORGANIZER")))
                .andExpect(status().isForbidden());
        // El ADMIN sí pasa la seguridad: el 404 lo responde el servicio porque el evento 999 no existe
        mockMvc.perform(delete("/api/v1/events/999").with(as("ADMIN")))
                .andExpect(status().isNotFound());
    }

    private static MockHttpServletRequestBuilder createVenue() {
        return post("/api/v1/venues").contentType(MediaType.APPLICATION_JSON).content(VENUE_JSON);
    }

    // Un token válido de un usuario con el rol indicado (la autoridad es ROLE_ + rol)
    private static RequestPostProcessor as(String role) {
        return jwt().jwt(token -> token.subject(role.toLowerCase() + "@ticketing.pe"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
