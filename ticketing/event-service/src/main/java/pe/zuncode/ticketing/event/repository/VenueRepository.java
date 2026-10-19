package pe.zuncode.ticketing.event.repository;

import pe.zuncode.ticketing.event.model.Venue;

import java.util.List;
import java.util.Optional;

public interface VenueRepository {
    List<Venue> findAll  () ;
    Optional<Venue> findById(Long id) ;
    Venue save (Venue venue);
}
