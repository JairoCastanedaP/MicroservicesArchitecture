package pe.zuncode.ticketing.user;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sesión 4: registro, login y emisión de tokens, con los usuarios semilla de DataSeeder.
 * Usa una base H2 en memoria propia de los tests (no toca data/user_db).
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:user_test")
@AutoConfigureMockMvc
class UserServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void registerCreatesACustomerWithoutExposingThePassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        { "email": "maria@mail.com", "password": "Secreta123", "fullName": "María Quispe" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerRejectsADuplicatedEmail() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                        { "email": "ana@mail.com", "password": "OtraClave123", "fullName": "Ana" }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void loginReturnsASignedTokenWithTheRoles() throws Exception {
        String token = login("organizer@ticketing.pe", "Organizer123!");

        Jwt jwt = jwtDecoder.decode(token);       // valida firma, exp e iss
        assertThat(jwt.getSubject()).isEqualTo("organizer@ticketing.pe");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ORGANIZER");
        assertThat(jwt.getIssuer()).hasToString("http://localhost:8084");
        assertThat(jwt.getHeaders()).containsEntry("alg", "RS256").containsKey("kid");
    }

    @Test
    void loginWithAWrongPasswordIs401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        { "email": "ana@mail.com", "password": "incorrecta" }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos"));
    }

    @Test
    void meWithoutTokenIs401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("No autenticado"));
    }

    @Test
    void meWithTokenReturnsTheProfile() throws Exception {
        String token = login("ana@mail.com", "Customer123!");

        mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@mail.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void listingUsersRequiresTheAdminRole() throws Exception {
        String customer = login("ana@mail.com", "Customer123!");
        String admin = login("admin@ticketing.pe", "Admin123!");

        mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + customer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"));
        mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + admin))
                .andExpect(status().isOk());
    }

    @Test
    void aTamperedTokenIsRejected() throws Exception {
        String[] parts = login("ana@mail.com", "Customer123!").split("\\.");
        // Cambiamos el payload (roles CUSTOMER → ADMIN) y conservamos la firma original
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("CUSTOMER", "ADMIN");
        String forged = parts[0] + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
                + "." + parts[2];

        mockMvc.perform(get("/api/v1/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + forged))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("El token no es válido o ya expiró"));
    }

    @Test
    void jwksPublishesOnlyThePublicKey() throws Exception {
        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].n").exists())     // módulo: parte pública
                .andExpect(jsonPath("$.keys[0].d").doesNotExist()); // exponente privado: nunca se publica
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"%s\", \"password\": \"%s\" }".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }
}
