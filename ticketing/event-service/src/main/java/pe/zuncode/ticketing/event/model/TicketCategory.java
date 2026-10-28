package pe.zuncode.ticketing.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Zona o tipo de entrada de un evento (VIP, Platea, General...).
 * Define el precio y cuántas entradas existen en esa zona.
 * <p>
 * Sesión 2: entidad JPA. Es el lado "dueño" de la relación con {@link Event}
 * porque su tabla tiene la columna {@code event_id}.
 */
@Entity
@Table(name = "ticket_categories")
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    protected TicketCategory() {
    }

    public TicketCategory(String name, BigDecimal price, int capacity) {
        this.name = name;
        this.price = price;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getCapacity() {
        return capacity;
    }

    public Event getEvent() {
        return event;
    }

    // Solo lo usa Event.addTicketCategory para mantener la relación sincronizada
    void setEvent(Event event) {
        this.event = event;
    }
}
