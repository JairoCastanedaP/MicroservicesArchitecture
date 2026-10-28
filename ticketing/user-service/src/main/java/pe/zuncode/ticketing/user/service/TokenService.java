package pe.zuncode.ticketing.user.service;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import pe.zuncode.ticketing.user.config.JwtProperties;
import pe.zuncode.ticketing.user.dto.TokenResponse;
import pe.zuncode.ticketing.user.model.User;

import java.time.Instant;
import java.util.List;

/**
 * Emite el token de acceso (JWT firmado con RS256).
 * <p>
 * El payload NO está cifrado: cualquiera puede leerlo (por ejemplo, en jwt.io). Por eso solo lleva
 * datos no sensibles (correo, nombre, roles) y nunca la contraseña. Lo que lo protege es la FIRMA:
 * si alguien cambia un claim (por ejemplo, "roles"), la firma ya no coincide y el token se rechaza.
 */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public TokenResponse issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())                       // iss: quién lo emite
                .subject(user.getEmail())                             // sub: de quién es
                .issuedAt(now)                                        // iat: cuándo se emitió
                .expiresAt(now.plus(jwtProperties.ttl()))             // exp: hasta cuándo vale
                .claim("name", user.getFullName())
                .claim("roles", List.of(user.getRole().name()))       // los servicios autorizan con esto
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return TokenResponse.bearer(token, jwtProperties.ttl().toSeconds());
    }
}
