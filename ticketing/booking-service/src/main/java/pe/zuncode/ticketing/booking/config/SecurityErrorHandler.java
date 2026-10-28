package pe.zuncode.ticketing.booking.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Los errores de seguridad ocurren en los filtros, ANTES de llegar al controlador, así que
 * {@code @RestControllerAdvice} no los ve. Esta clase los reenvía al mecanismo de Spring MVC
 * para que {@code GlobalExceptionHandler} responda 401 y 403 con el mismo formato ProblemDetail.
 * <ul>
 *   <li>{@link AuthenticationEntryPoint}: no hay token o no es válido → 401.</li>
 *   <li>{@link AccessDeniedHandler}: el token es válido, pero el rol no alcanza → 403.</li>
 * </ul>
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolver;

    public SecurityErrorHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) {
        resolver.resolveException(request, response, null, exception);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) {
        resolver.resolveException(request, response, null, exception);
    }
}
