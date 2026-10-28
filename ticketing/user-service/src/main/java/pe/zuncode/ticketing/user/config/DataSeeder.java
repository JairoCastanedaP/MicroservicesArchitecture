package pe.zuncode.ticketing.user.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pe.zuncode.ticketing.user.model.Role;
import pe.zuncode.ticketing.user.model.User;
import pe.zuncode.ticketing.user.repository.UserRepository;

import java.time.LocalDateTime;

/**
 * Crea los usuarios de ejemplo que todavía no existan. Solo para desarrollo y para el curso:
 * <pre>
 *   admin@ticketing.pe     / Admin123!      ADMIN
 *   organizer@ticketing.pe / Organizer123!  ORGANIZER
 *   ana@mail.com           / Customer123!   CUSTOMER
 *   luis@mail.com          / Customer123!   CUSTOMER
 * </pre>
 * Las contraseñas se guardan con su hash BCrypt, como cualquier otra.
 */
@Component
@ConditionalOnProperty(name = "ticketing.seed-data.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();
        int created = seed("admin@ticketing.pe", "Admin123!", "Administradora Ticketing", Role.ADMIN, now)
                + seed("organizer@ticketing.pe", "Organizer123!", "Producciones Lima", Role.ORGANIZER, now)
                + seed("ana@mail.com", "Customer123!", "Ana Torres", Role.CUSTOMER, now)
                + seed("luis@mail.com", "Customer123!", "Luis Rojas", Role.CUSTOMER, now);
        log.info("Usuarios semilla: {} creados (los que ya existían se conservan)", created);
    }

    // Crea el usuario solo si su correo no existe todavía
    private int seed(String email, String password, String fullName, Role role, LocalDateTime now) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return 0;
        }
        userRepository.save(new User(email, passwordEncoder.encode(password), fullName, role, now));
        return 1;
    }
}
