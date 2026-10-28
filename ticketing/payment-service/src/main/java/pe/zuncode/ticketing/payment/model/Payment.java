package pe.zuncode.ticketing.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Intento de cobro de una reserva. Se guarda tanto si se aprueba como si se rechaza.
 * <p>
 * {@code bookingId} es una referencia lógica a booking_db (otra base de datos): no hay FK.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "customer_email", nullable = false, length = 150)
    private String customerEmail;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Payment() {
    }

    private Payment(Long bookingId, String customerEmail, BigDecimal amount, PaymentStatus status,
                    String rejectionReason, LocalDateTime createdAt) {
        this.bookingId = bookingId;
        this.customerEmail = customerEmail;
        this.amount = amount;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
    }

    public static Payment approved(Long bookingId, String customerEmail, BigDecimal amount, LocalDateTime now) {
        return new Payment(bookingId, customerEmail, amount, PaymentStatus.APPROVED, null, now);
    }

    public static Payment rejected(Long bookingId, String customerEmail, BigDecimal amount, String reason,
                                   LocalDateTime now) {
        return new Payment(bookingId, customerEmail, amount, PaymentStatus.REJECTED, reason, now);
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
