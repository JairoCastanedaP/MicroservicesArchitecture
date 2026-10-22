package pe.zuncode.ticketing.event.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    // STRING guarda "MUSIC"; ORDINAL guardaría 0, 1, 2... y se rompería al reordenar el enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    // Muchos eventos en un recinto. LAZY: el recinto se carga solo cuando se necesita
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    // Un evento tiene muchas zonas. El evento es el "dueño" de su ciclo de vida:
    // cascade = ALL guarda/borra las zonas junto con el evento y
    // orphanRemoval = true borra de la BD las zonas que se quitan de la lista
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<TicketCategory> ticketCategories = new ArrayList<>();

    protected Event() {
    }

    public Event(String name, String description, EventCategory category, Venue venue, LocalDateTime dateTime) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.venue = venue;
        this.dateTime = dateTime;
        this.status = EventStatus.DRAFT; // todo evento nace como borrador
    }

    /** Agrega una zona manteniendo sincronizados los dos lados de la relación. */
    public void addTicketCategory(TicketCategory ticketCategory) {
        ticketCategory.setEvent(this);
        ticketCategories.add(ticketCategory);
    }

    /** Reemplaza todas las zonas; las que salen de la lista se borran (orphanRemoval). */
    public void replaceTicketCategories(List<TicketCategory> newCategories) {
        ticketCategories.clear();
        newCategories.forEach(this::addTicketCategory);
    }

    /** Total de entradas a la venta sumando todas las zonas. */
    public int totalCapacity() {
        return ticketCategories.stream().mapToInt(TicketCategory::getCapacity).sum();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public List<TicketCategory> getTicketCategories() {
        return ticketCategories;
    }

}
