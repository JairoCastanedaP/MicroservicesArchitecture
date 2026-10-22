package pe.zuncode.ticketing.event.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.zuncode.ticketing.event.dto.EventRequest;
import pe.zuncode.ticketing.event.dto.EventResponse;
import pe.zuncode.ticketing.event.dto.TicketCategoryResponse;
import pe.zuncode.ticketing.event.model.EventCategory;
import pe.zuncode.ticketing.event.model.EventStatus;
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
    public List<EventResponse> findAll(@RequestParam(required = false) String city,
                                       @RequestParam(required = false) EventCategory category,
                                       @RequestParam(required = false) EventStatus status) {
        return eventService.findAll(city, category, status);
    }

    @GetMapping("/{id}")
    public EventResponse findById(@PathVariable Long id) {
        return eventService.findById(id);
    }

    @GetMapping("/{id}/ticket-categories")
    public List<TicketCategoryResponse> findTicketCategories(@PathVariable Long id) {
        return eventService.findTicketCategories(id);
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        EventResponse created = eventService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        return eventService.update(id, request);
    }

    // Acción de negocio sobre el recurso: cambia el estado a PUBLISHED
    @PatchMapping("/{id}/publish")
    public EventResponse publish(@PathVariable Long id) {
        return eventService.publish(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
