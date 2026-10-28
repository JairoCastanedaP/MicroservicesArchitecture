package pe.zuncode.ticketing.user.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Publica la clave PÚBLICA en formato JWKS (JSON Web Key Set, RFC 7517).
 * <p>
 * api-gateway, event-service, booking-service y payment-service la descargan
 * ({@code jwk-set-uri}) para verificar la firma de los tokens. La clave privada nunca sale de aquí.
 */
@RestController
public class JwksController {

    private final JWKSet publicKeys;

    public JwksController(RSAKey signingKey) {
        // toPublicJWK() descarta la parte privada de la clave
        this.publicKeys = new JWKSet(signingKey.toPublicJWK());
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return publicKeys.toJSONObject();
    }
}
