package pe.zuncode.ticketing.event.repository;

import pe.zuncode.ticketing.event.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    List<Event> findAll();
    Optional<Event> findById(Long id);
    Event save(Event event);
    boolean existsById (Long id);
    void deleteById (Long id);
}
