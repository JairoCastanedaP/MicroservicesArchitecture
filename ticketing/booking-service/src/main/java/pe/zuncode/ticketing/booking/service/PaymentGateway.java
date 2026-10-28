package pe.zuncode.ticketing.booking.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import pe.zuncode.ticketing.booking.client.PaymentClient;
import pe.zuncode.ticketing.booking.client.PaymentRequest;
import pe.zuncode.ticketing.booking.client.PaymentResult;
import pe.zuncode.ticketing.booking.exception.RemoteServiceUnavailableException;

/**
 * Cobro en payment-service, protegido con Circuit Breaker.
 * <p>
 * ⚠️ SIN @Retry a propósito: un POST de pago no es idempotente. Si el primer intento llegó
 * a cobrar pero la respuesta se perdió (timeout), reintentar cobraría dos veces.
 * En la Sesión 8 lo resolvemos con una {@code Idempotency-Key}.
 */
@Service
public class PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(PaymentGateway.class);

    private final PaymentClient paymentClient;

    public PaymentGateway(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @CircuitBreaker(name = "payment-service", fallbackMethod = "payFallback")
    public PaymentResult pay(PaymentRequest request) {
        log.info("Solicitando pago a payment-service: reserva {}, monto {}", request.bookingId(), request.amount());
        return paymentClient.pay(request);
    }

    private PaymentResult payFallback(PaymentRequest request, Throwable ex) {
        if (ex instanceof HttpClientErrorException clientError) {
            throw clientError; // 4xx: enviamos algo mal. Es un bug nuestro, no una caída de payment-service
        }
        log.warn("payment-service no disponible para la reserva {} ({}): {}", request.bookingId(),
                ex.getClass().getSimpleName(), ex.getMessage());
        throw new RemoteServiceUnavailableException("payment-service",
                "No pudimos procesar el pago en este momento. Tu reserva sigue pendiente: "
                        + "intenta pagar nuevamente en unos minutos.", ex);
    }
}
