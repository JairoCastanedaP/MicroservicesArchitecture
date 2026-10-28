package pe.zuncode.ticketing.booking.client;

/**
 * Respuesta de payment-service (tolerant reader: solo los campos que usamos).
 *
 * @param status APPROVED o REJECTED
 */
public record PaymentResult(Long id, String status, String rejectionReason) {

    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
}
