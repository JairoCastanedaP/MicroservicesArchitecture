package pe.zuncode.ticketing.user.dto;

/**
 * Respuesta del login. El cliente envía {@code accessToken} en cada petición:
 * {@code Authorization: Bearer <accessToken>}.
 *
 * @param expiresIn segundos de vigencia del token
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    public static TokenResponse bearer(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
