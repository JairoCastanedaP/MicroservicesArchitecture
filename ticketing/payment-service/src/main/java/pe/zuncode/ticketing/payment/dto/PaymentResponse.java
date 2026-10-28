package pe.zuncode.ticketing.payment.dto;

import pe.zuncode.ticketing.payment.model.Payment;
import pe.zuncode.ticketing.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long bookingId,
        String customerEmail,
        BigDecimal amount,
        PaymentStatus status,
        String rejectionReason,
        LocalDateTime createdAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getBookingId(), payment.getCustomerEmail(),
                payment.getAmount(), payment.getStatus(), payment.getRejectionReason(), payment.getCreatedAt());
    }
}
