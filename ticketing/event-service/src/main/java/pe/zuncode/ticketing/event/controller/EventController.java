package pe.zuncode.ticketing.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.zuncode.ticketing.event.model.Event;
import pe.zuncode.ticketing.event.model.EventCategory;
import pe.zuncode.ticketing.event.model.EventStatus;
import pe.zuncode.ticketing.event.model.TicketCategory;
import pe.zuncode.ticketing.event.service.EventService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // GET /api/v1/events?city=Lima&category=MUSIC&status=PUBLISHED  (todos los filtros son opcionales)
    @GetMapping
    public List<Event> findAll(@RequestParam(required = false) String city,
                               @RequestParam(required = false) EventCategory category,
                               @RequestParam(required = false) EventStatus status) {
        return eventService.findAll(city, category, status);
    }

    @GetMapping("/{id}")
    public Event findById(@PathVariable Long id) {
        return eventService.findById(id);
    }

    @GetMapping("/{id}/ticket-categories")
    public List<TicketCategory> findTicketCategories(@PathVariable Long id) {
        return eventService.findTicketCategories(id);
    }

    @PostMapping
    public ResponseEntity<Event> create(@RequestBody Event event) {
        Event created = eventService.create(event);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable Long id, @RequestBody Event event) {
        return eventService.update(id, event);
    }

    // Acción de negocio sobre el recurso: cambia el estado a PUBLISHED
    @PatchMapping("/{id}/publish")
    public Event publish(@PathVariable Long id) {
        return eventService.publish(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
