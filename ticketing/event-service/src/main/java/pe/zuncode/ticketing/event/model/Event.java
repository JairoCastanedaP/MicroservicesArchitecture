package pe.zuncode.ticketing.event.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private Long id;
    private String name;
    private String description;
    private EventCategory category;
    private Long venueId;
    private LocalDateTime dateTime;
    private EventStatus status;
    private List<TicketCategory> ticketCategories = new ArrayList<>();

    public Event() {

    }

    public Event(Long id, String name, String description, EventCategory category, Long venueId, LocalDateTime dateTime, List<TicketCategory> ticketCategories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.venueId = venueId;
        this.dateTime = dateTime;
        this.ticketCategories = new ArrayList<>(ticketCategories);
    }

    public int totalCapacity() {
        return ticketCategories.stream().mapToInt(TicketCategory::getCapacity).sum();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public List<TicketCategory> getTicketCategories() {
        return ticketCategories;
    }

    public void setTicketCategories(List<TicketCategory> ticketCategories) {
        this.ticketCategories = ticketCategories == null ? new ArrayList<>() : ticketCategories;
    }
}
