package pe.zuncode.ticketing.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sesión 4: quién puede hacer qué en el catálogo.
 * <ul>
 *   <li>Consultar (GET) es público: cualquiera puede ver la cartelera, y booking-service consulta la disponibilidad.</li>
 *   <li>Crear y modificar recintos, eventos y zonas: ORGANIZER o ADMIN.</li>
 *   <li>Eliminar un evento: solo ADMIN.</li>
 * </ul>
 * Las reglas se evalúan EN ORDEN y gana la primera que coincide: van de la más específica a la más general.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errorHandler) throws Exception {
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()          // solo para desarrollo
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/events/**", "/api/v1/venues/**").hasAnyRole("ORGANIZER", "ADMIN")
                        .anyRequest().authenticated())
                // Cada petición con "Authorization: Bearer <jwt>" se valida con la clave pública de user-service
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
