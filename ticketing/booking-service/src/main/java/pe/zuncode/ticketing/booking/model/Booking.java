package pe.zuncode.ticketing.booking.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reserva de N entradas de una zona de un evento.
 * <p>
 * {@code eventId} y {@code ticketCategoryId} son referencias a datos de event-service:
 * se guardan como simples ids (no hay @ManyToOne ni FK porque viven en otra base de datos).
 * <p>
 * {@code unitPrice} es una copia del precio al momento de reservar (snapshot): si mañana
 * el organizador cambia el precio de la zona, esta reserva conserva el que se le cobró al cliente.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "ticket_category_id", nullable = false)
    private Long ticketCategoryId;

    @Column(name = "customer_email", nullable = false, length = 150)
    private String customerEmail;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /**
     * Bloqueo optimista. Cada UPDATE se ejecuta como
     * {@code ... WHERE id = ? AND version = ?} e incrementa la versión.
     * Si otra transacción ya modificó la fila, el UPDATE no afecta ninguna fila y JPA lanza
     * {@code ObjectOptimisticLockingFailureException} en lugar de pisar el cambio ajeno.
     */
    @Version
    private Long version;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Ticket> tickets = new ArrayList<>();

    protected Booking() {
    }

    public Booking(Long eventId, Long ticketCategoryId, String customerEmail, int quantity,
                   BigDecimal unitPrice, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.eventId = eventId;
        this.ticketCategoryId = ticketCategoryId;
        this.customerEmail = customerEmail;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.status = BookingStatus.PENDING; // toda reserva nace retenida (hold)
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public void addTicket(Ticket ticket) {
        ticket.setBooking(this);
        tickets.add(ticket);
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getTicketCategoryId() {
        return ticketCategoryId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
}
