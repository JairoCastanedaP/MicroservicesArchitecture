package pe.zuncode.ticketing.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.zuncode.ticketing.event.dto.EventRequest;
import pe.zuncode.ticketing.event.dto.EventResponse;
import pe.zuncode.ticketing.event.dto.TicketAvailabilityResponse;
import pe.zuncode.ticketing.event.dto.TicketCategoryResponse;
import pe.zuncode.ticketing.event.exception.BusinessRuleException;
import pe.zuncode.ticketing.event.exception.InvalidRequestException;
import pe.zuncode.ticketing.event.exception.ResourceNotFoundException;
import pe.zuncode.ticketing.event.model.Event;
import pe.zuncode.ticketing.event.model.EventCategory;
import pe.zuncode.ticketing.event.model.EventStatus;
import pe.zuncode.ticketing.event.model.TicketCategory;
import pe.zuncode.ticketing.event.model.Venue;
import pe.zuncode.ticketing.event.repository.EventRepository;
import pe.zuncode.ticketing.event.repository.VenueRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio de eventos.
 * <p>
 * Sesión 2: las reglas de formato (obligatorios, fecha futura, precios positivos) las valida
 * Bean Validation en {@link EventRequest}. Aquí quedan solo las reglas que necesitan datos:
 * <ul>
 *   <li>El recinto debe existir.</li>
 *   <li>La suma de entradas de las zonas no puede superar el aforo del recinto.</li>
 *   <li>Un evento solo se publica si tiene al menos una zona.</li>
 *   <li>Un evento cancelado no se puede modificar ni publicar.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;

    public EventService(EventRepository eventRepository, VenueRepository venueRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
    }

    public List<EventResponse> findAll(String city, EventCategory category, EventStatus status) {
        // El filtrado ahora lo hace la base de datos (WHERE), no Java
        return eventRepository.search(city, category, status).stream()
                .map(EventResponse::from)
                .toList();
    }

    public EventResponse findById(Long id) {
        return EventResponse.from(getEventWithDetails(id));
    }

    public List<TicketCategoryResponse> findTicketCategories(Long eventId) {
        return getEventWithDetails(eventId).getTicketCategories().stream()
                .map(TicketCategoryResponse::from)
                .toList();
    }

    /**
     * Sesión 3: disponibilidad y precio de una zona. Lo consulta booking-service antes de reservar.
     * La zona debe pertenecer al evento: si no, para quien llama es como si no existiera (404).
     */
    public TicketAvailabilityResponse findAvailability(Long eventId, Long ticketCategoryId) {
        Event event = getEventWithDetails(eventId);
        TicketCategory category = event.getTicketCategories().stream()
                .filter(c -> c.getId().equals(ticketCategoryId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de entrada", ticketCategoryId));
        return TicketAvailabilityResponse.from(event, category, LocalDateTime.now());
    }

    @Transactional
    public EventResponse create(EventRequest request) {
        Venue venue = findVenueForEvent(request.venueId());
        validateCapacity(request, venue);

        Event event = new Event(request.name(), request.description(), request.category(),
                venue, request.dateTime());
        toTicketCategories(request).forEach(event::addTicketCategory);

        // cascade = ALL: al guardar el evento también se insertan sus zonas
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse update(Long id, EventRequest request) {
        Event event = getEventWithDetails(id);
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede modificar un evento cancelado");
        }
        Venue venue = findVenueForEvent(request.venueId());
        validateCapacity(request, venue);

        event.setName(request.name());
        event.setDescription(request.description());
        event.setCategory(request.category());
        event.setVenue(venue);
        event.setDateTime(request.dateTime());
        event.replaceTicketCategories(toTicketCategories(request));

        // No hace falta llamar a save(): la entidad está "managed" y Hibernate detecta los
        // cambios al terminar la transacción (dirty checking). Forzamos el flush para que
        // la base de datos asigne los ids de las zonas nuevas antes de armar la respuesta.
        eventRepository.flush();
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse publish(Long id) {
        Event event = getEventWithDetails(id);
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessRuleException("No se puede publicar un evento cancelado");
        }
        if (event.getTicketCategories().isEmpty()) {
            throw new BusinessRuleException(
                    "El evento debe tener al menos una zona (ticketCategory) para publicarse");
        }
        event.setStatus(EventStatus.PUBLISHED); // dirty checking: UPDATE automático al hacer commit
        return EventResponse.from(event);
    }

    @Transactional
    public void delete(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Evento", id);
        }
        eventRepository.deleteById(id);
    }

    private Event getEventWithDetails(Long id) {
        return eventRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
    }

    // Un venueId inexistente en el cuerpo es un error del cliente (400), no un 404 del recurso pedido
    private Venue findVenueForEvent(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new InvalidRequestException("El recinto " + venueId + " no existe"));
    }

    private static void validateCapacity(EventRequest request, Venue venue) {
        if (request.totalCapacity() > venue.getCapacity()) {
            throw new InvalidRequestException("Las zonas suman " + request.totalCapacity()
                    + " entradas y el aforo del recinto es " + venue.getCapacity());
        }
    }

    private static List<TicketCategory> toTicketCategories(EventRequest request) {
        return request.ticketCategories().stream()
                .map(category -> new TicketCategory(category.name(), category.price(), category.capacity()))
                .toList();
    }
}
