package pe.zuncode.ticketing.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.zuncode.ticketing.user.repository.UserRepository;

/**
 * Le dice a Spring Security cómo encontrar un usuario por su "username" (aquí, el correo).
 * El {@code AuthenticationManager} lo usa en el login y luego compara la contraseña con el hash.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())      // ADMIN → autoridad ROLE_ADMIN
                        .build())
                // Spring Security la convierte en BadCredentialsException: no revela si el correo existe
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
