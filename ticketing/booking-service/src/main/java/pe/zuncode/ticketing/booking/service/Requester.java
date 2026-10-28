package pe.zuncode.ticketing.booking.service;

import org.springframework.security.core.Authentication;

/**
 * Sesión 4: quién hace la petición, según su token ya validado.
 *
 * @param email el "subject" del token (claim {@code sub})
 * @param admin si tiene el rol ADMIN
 */
public record Requester(String email, boolean admin) {

    public static Requester from(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return new Requester(authentication.getName(), admin);
    }

    /** Un ADMIN accede a cualquier reserva; los demás, solo a las suyas. */
    public boolean canAccess(String customerEmail) {
        return admin || email.equalsIgnoreCase(customerEmail);
    }
}
