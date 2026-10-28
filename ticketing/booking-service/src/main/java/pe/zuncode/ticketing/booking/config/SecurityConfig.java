package pe.zuncode.ticketing.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sesión 4: reglas de acceso de booking-service.
 * <ul>
 *   <li>Reservar, pagar y cancelar: rol CUSTOMER.</li>
 *   <li>Consultar reservas: cualquier usuario autenticado. Que solo vea las SUYAS (salvo un ADMIN)
 *       lo decide BookingService, porque depende del dato y no solo del rol.</li>
 *   <li>Actuator (estado de los circuitos): público en desarrollo. En producción se restringe.</li>
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
                        .requestMatchers("/actuator/**", "/h2-console/**").permitAll()   // solo para desarrollo
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings", "/api/v1/bookings/*/pay").hasRole("CUSTOMER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bookings/*/cancel").hasRole("CUSTOMER")
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
