package pe.zuncode.ticketing.user.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.util.UUID;

/**
 * Claves para firmar (RS256) y verificar los tokens.
 * <ul>
 *   <li>La clave <b>privada</b> solo la tiene user-service: con ella firma los tokens.</li>
 *   <li>La clave <b>pública</b> se publica en {@code /.well-known/jwks.json}: con ella los demás
 *       servicios verifican la firma, pero no pueden emitir tokens.</li>
 * </ul>
 * Para el curso, el par de claves se genera al arrancar: si reinicias user-service, los tokens
 * anteriores dejan de ser válidos y hay que volver a iniciar sesión. En producción la clave se
 * carga desde un almacén seguro (por ejemplo, Azure Key Vault).
 */
@Configuration
public class JwtKeyConfig {

    @Bean
    RSAKey signingKey() throws JOSEException {
        // Par de claves RSA de 2048 bits. El "kid" (key id) identifica la clave dentro del JWKS
        return new RSAKeyGenerator(2048)
                .keyID(UUID.randomUUID().toString())
                .generate();
    }

    // Emite (firma) tokens con la clave privada
    @Bean
    JwtEncoder jwtEncoder(RSAKey signingKey) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(signingKey)));
    }

    // Valida los tokens que llegan a /api/v1/users/**: firma, vigencia (exp) y emisor (iss)
    @Bean
    JwtDecoder jwtDecoder(RSAKey signingKey, JwtProperties jwtProperties) throws JOSEException {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(signingKey.toRSAPublicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
        return decoder;
    }
}
