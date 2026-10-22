package pe.zuncode.ticketing.booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entrada emitida al confirmar una reserva. Su {@code code} es único (se imprimiría como QR).
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String code;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    protected Ticket() {
    }

    public Ticket(LocalDateTime issuedAt) {
        this.code = UUID.randomUUID().toString();
        this.issuedAt = issuedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public Booking getBooking() {
        return booking;
    }

    void setBooking(Booking booking) {
        this.booking = booking;
    }
}
