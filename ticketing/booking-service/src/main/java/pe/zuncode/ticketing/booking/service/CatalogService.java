package pe.zuncode.ticketing.booking.service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.zuncode.ticketing.booking.client.EventClient;
import pe.zuncode.ticketing.booking.client.TicketAvailability;
import pe.zuncode.ticketing.booking.exception.InvalidRequestException;
import pe.zuncode.ticketing.booking.exception.RemoteServiceUnavailableException;

/**
 * Acceso al catálogo de event-service, protegido con Resilience4j.
 * <p>
 * Las anotaciones van en este servicio (un bean de Spring) y no en la interfaz Feign:
 * Resilience4j funciona con proxies AOP, igual que @Transactional.
 * <p>
 * Orden de ejecución: Retry( CircuitBreaker( llamada ) ). Cada reintento pasa por el
 * Circuit Breaker y cuenta como una llamada más en su ventana de estadísticas.
 */
@Service
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final EventClient eventClient;

    public CatalogService(EventClient eventClient) {
        this.eventClient = eventClient;
    }

    /**
     * Consultar es seguro de reintentar (GET, no cambia nada): por eso lleva @Retry.
     * El fallback va en @Retry (el más externo): si estuviera en @CircuitBreaker, convertiría
     * la falla en otra excepción ANTES de que Retry pudiera reintentar.
     */
    @Retry(name = "event-service", fallbackMethod = "availabilityFallback")
    @CircuitBreaker(name = "event-service")
    public TicketAvailability getAvailability(Long eventId, Long ticketCategoryId) {
        log.info("Consultando disponibilidad a event-service: evento {}, zona {}", eventId, ticketCategoryId);
        try {
            return eventClient.getAvailability(eventId, ticketCategoryId);
        } catch (FeignException.NotFound ex) {
            // 404: el evento o la zona no existen. Es un error del cliente, no una falla de event-service:
            // no se reintenta ni cuenta para el Circuit Breaker (ver retry-exceptions / record-exceptions)
            throw new InvalidRequestException("El evento " + eventId + " no existe o no tiene la zona "
                    + ticketCategoryId);
        }
    }

    /**
     * Se ejecuta cuando getAvailability termina con excepción: se agotaron los reintentos,
     * el circuito está abierto (CallNotPermittedException) o el error no era reintentable.
     */
    private TicketAvailability availabilityFallback(Long eventId, Long ticketCategoryId, Throwable ex) {
        if (ex instanceof InvalidRequestException invalid) {
            throw invalid; // un error del cliente no se disfraza de "servicio no disponible"
        }
        log.warn("event-service no disponible ({}): {}", ex.getClass().getSimpleName(), ex.getMessage());
        throw new RemoteServiceUnavailableException("event-service",
                "No pudimos consultar el catálogo de eventos. Intenta nuevamente en unos momentos.", ex);
    }
}
