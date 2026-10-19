package pe.zuncode.ticketing.event.repository;

import org.springframework.stereotype.Repository;
import pe.zuncode.ticketing.event.model.Venue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryVenueRepository implements VenueRepository {

    private final Map<Long, Venue> storage =  new ConcurrentHashMap <>();
    private final AtomicLong sequence = new  AtomicLong ();

    @Override
    public List<Venue> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Venue> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Venue save(Venue venue) {
        if (venue.getId() ==null){
            venue.setId(sequence.incrementAndGet());
        }
        storage.put(venue.getId(), venue);
        return venue;
    }
}
