package pe.zuncode.ticketing.event.repository;

import org.springframework.stereotype.Repository;
import pe.zuncode.ticketing.event.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryEventRepository implements EventRepository {

    private final Map<Long, Event> storage = new ConcurrentHashMap<>();
    private final AtomicLong eventSequence = new AtomicLong();
    private final AtomicLong ticketCategorySequence = new AtomicLong();

    @Override
    public List<Event> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Event> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Event save(Event event) {
        if (event.getId() == null) {
            event.setId(eventSequence.incrementAndGet());
        }
        event.getTicketCategories().stream()
                .filter(category -> category.getId() == null)
                .forEach(category -> category.setId(ticketCategorySequence.incrementAndGet()));
        storage.put(event.getId(), event);
        return  event;
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}
