package pe.zuncode.ticketing.event.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.zuncode.ticketing.event.model.*;
import pe.zuncode.ticketing.event.repository.EventRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final VenueService venueService;

    public EventService(EventRepository eventRepository, VenueService venueService) {
        this.eventRepository = eventRepository;
        this.venueService = venueService;
    }

    public List<Event> findAll(String city, EventCategory category, EventStatus status) {
        return eventRepository.findAll().stream()
                .filter(event -> category == null || event.getCategory() == category)
                .filter(event -> status == null || event.getStatus() == status)
                .filter(event -> city == null
                        || venueService.findById(event.getVenueId()).getCity().equalsIgnoreCase(city))
                .sorted(Comparator.comparing(Event::getDateTime))
                .toList();
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Evento no encontrado: " + id));
    }

    public List<TicketCategory> findTicketCategories(Long eventId) {
        return findById(eventId).getTicketCategories();
    }

    public Event create(Event event) {
        validate(event);
        event.setId(null);
        event.setStatus(EventStatus.DRAFT); // todo evento nace como borrador
        event.getTicketCategories().forEach(category -> category.setId(null));
        return eventRepository.save(event);
    }

    public Event update(Long id, Event changes) {
        Event current = findById(id);
        if (current.getStatus() == EventStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede modificar un evento cancelado");
        }
        validate(changes);
        current.setName(changes.getName());
        current.setDescription(changes.getDescription());
        current.setCategory(changes.getCategory());
        current.setVenueId(changes.getVenueId());
        current.setDateTime(changes.getDateTime());
        current.setTicketCategories(changes.getTicketCategories());
        return eventRepository.save(current);
    }

    public Event publish(Long id) {
        Event event = findById(id);
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede publicar un evento cancelado");
        }
        if (event.getTicketCategories().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El evento debe tener al menos una zona (ticketCategory) para publicarse");
        }
        event.setStatus(EventStatus.PUBLISHED);
        return eventRepository.save(event);
    }

    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado: " + id);
        }
        eventRepository.deleteById(id);
    }

    private void validate(Event event) {
        if (event.getName() == null || event.getName().isBlank()) {
            throw badRequest("name es obligatorio");
        }
        if (event.getCategory() == null) {
            throw badRequest("category es obligatorio");
        }
        if (event.getVenueId() == null) {
            throw badRequest("venueId es obligatorio");
        }
        if (event.getDateTime() == null || !event.getDateTime().isAfter(LocalDateTime.now())) {
            throw badRequest("dateTime debe ser una fecha futura");
        }
        for (TicketCategory category : event.getTicketCategories()) {
            if (category.getName() == null || category.getName().isBlank()) {
                throw badRequest("Cada zona debe tener name");
            }
            if (category.getPrice() == null || category.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw badRequest("El precio de la zona " + category.getName() + " debe ser mayor a 0");
            }
            if (category.getCapacity() <= 0) {
                throw badRequest("La capacidad de la zona " + category.getName() + " debe ser mayor a 0");
            }
        }

        Venue venue;
        try {
            venue = venueService.findById(event.getVenueId());
        } catch (ResponseStatusException ex) {
            throw badRequest("El recinto " + event.getVenueId() + " no existe");
        }
        if (event.totalCapacity() > venue.getCapacity()) {
            throw badRequest("Las zonas suman " + event.totalCapacity()
                    + " entradas y el aforo del recinto es " + venue.getCapacity());
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
