package pe.zuncode.ticketing.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Pruebas del gateway de punta a punta: enrutamiento + validación del JWT.
 * <p>
 * WireMock hace de dos servicios a la vez: de user-service (publica la clave pública en el JWKS)
 * y de event-service (responde las rutas del catálogo). El test firma sus propios tokens con la
 * clave privada correspondiente, igual que lo haría user-service.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayTests {

    static final WireMockServer backend = new WireMockServer(wireMockConfig().dynamicPort().http2PlainDisabled(true));
    static final RSAKey key;

    static {
        backend.start();
        try {
            key = new RSAKeyGenerator(2048).keyID("test-key").generate();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // La ruta de event-service apunta al WireMock
        registry.add("EVENT_SERVICE_URL", backend::baseUrl);
        registry.add("JWT_JWK_SET_URI", () -> backend.baseUrl() + "/.well-known/jwks.json");
    }

    @AfterAll
    static void stopBackend() {
        backend.stop();
    }

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void stubs() {
        backend.resetAll();
        backend.stubFor(get("/.well-known/jwks.json")
                .willReturn(okJson(new JWKSet(key.toPublicJWK()).toString())));
        backend.stubFor(get("/api/v1/events").willReturn(okJson("[]")));
        backend.stubFor(post("/api/v1/events").willReturn(okJson("{}").withStatus(201)));
    }

    @Test
    void publicRoutesAreForwardedWithoutAToken() {
        client.get().uri("/api/v1/events").exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Gateway", "ticketing-api-gateway");

        backend.verify(getRequestedFor(urlEqualTo("/api/v1/events")));
    }

    @Test
    void protectedRoutesWithoutATokenStopAtTheGateway() {
        client.post().uri("/api/v1/events").exchange()
                .expectStatus().isUnauthorized()
                .expectBody().jsonPath("$.title").isEqualTo("No autenticado");

        // la petición nunca llegó a event-service
        backend.verify(0, postRequestedFor(urlEqualTo("/api/v1/events")));
    }

    @Test
    void aValidTokenIsForwardedToTheService() throws Exception {
        String token = sign("organizer@ticketing.pe", "http://localhost:8084", Instant.now().plusSeconds(300));

        client.post().uri("/api/v1/events").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange()
                .expectStatus().isCreated();

        // el servicio recibe el MISMO header: así puede validar el token y revisar los roles
        backend.verify(postRequestedFor(urlEqualTo("/api/v1/events"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + token)));
    }

    @Test
    void anExpiredTokenIsRejected() throws Exception {
        String token = sign("organizer@ticketing.pe", "http://localhost:8084", Instant.now().minusSeconds(600));

        client.post().uri("/api/v1/events").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void aTokenFromAnotherIssuerIsRejected() throws Exception {
        String token = sign("organizer@ticketing.pe", "http://otro-emisor", Instant.now().plusSeconds(300));

        client.post().uri("/api/v1/events").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange()
                .expectStatus().isUnauthorized();
    }

    // Firma un JWT con RS256, como lo hace user-service
    private static String sign(String subject, String issuer, Instant expiresAt) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(Date.from(expiresAt))
                .claim("roles", List.of("ORGANIZER"))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
        jwt.sign(new RSASSASigner(key));
        return jwt.serialize();
    }
}
