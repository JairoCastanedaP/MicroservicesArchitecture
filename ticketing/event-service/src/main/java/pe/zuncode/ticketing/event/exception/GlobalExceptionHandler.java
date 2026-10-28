package pe.zuncode.ticketing.event.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Manejo global de errores: traduce excepciones a respuestas HTTP con el formato
 * estándar Problem Details (RFC 9457), Content-Type {@code application/problem+json}.
 * <p>
 * Extiende {@link ResponseEntityExceptionHandler}, que ya convierte a ProblemDetail las
 * excepciones propias de Spring MVC (JSON mal formado, parámetro con tipo incorrecto,
 * método no soportado...). Aquí agregamos las nuestras y personalizamos algunas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ProblemDetail handleInvalidRequest(InvalidRequestException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Solicitud inválida", ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return problem(HttpStatus.CONFLICT, "Regla de negocio incumplida", ex.getMessage());
    }

    // Una restricción de la base de datos (FK, UNIQUE, CHECK) rechazó la operación
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación no se puede completar porque entra en conflicto con datos existentes");
    }

    // Último recurso: nunca devolvemos el detalle técnico al cliente, solo lo registramos en el log
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Error no controlado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Intenta nuevamente más tarde.");
    }

    /** Errores de Bean Validation en un @RequestBody anotado con @Valid. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", String.valueOf(error.getDefaultMessage())))
                .toList();

        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "La solicitud tiene " + errors.size() + " error(es) de validación");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /** JSON mal formado o con un valor que no se puede convertir (p. ej. una categoría inexistente). */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail body = problem(HttpStatus.BAD_REQUEST, "Cuerpo de la solicitud ilegible",
                "El JSON está mal formado o tiene un valor con formato incorrecto");
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
