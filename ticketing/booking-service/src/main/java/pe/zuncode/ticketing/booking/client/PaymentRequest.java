package pe.zuncode.ticketing.booking.client;

import java.math.BigDecimal;

/**
 * Cuerpo de {@code POST /api/v1/payments} de payment-service.
 */
public record PaymentRequest(Long bookingId, String customerEmail, BigDecimal amount) {
}
