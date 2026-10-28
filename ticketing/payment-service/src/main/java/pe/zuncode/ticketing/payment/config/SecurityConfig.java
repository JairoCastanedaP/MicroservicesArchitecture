package pe.zuncode.ticketing.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sesión 4: reglas de acceso de payment-service.
 * <ul>
 *   <li>Cobrar: rol CUSTOMER. booking-service reenvía el token del cliente que está pagando.</li>
 *   <li>Consultar pagos: solo ADMIN.</li>
 *   <li>{@code /api/v1/simulation}: público, porque es una herramienta del curso (no existiría en producción).</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errorHandler) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/simulation", "/h2-console/**").permitAll()   // solo para el curso
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(errorHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
