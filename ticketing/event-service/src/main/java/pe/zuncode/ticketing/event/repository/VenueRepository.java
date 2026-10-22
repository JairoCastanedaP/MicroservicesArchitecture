package pe.zuncode.ticketing.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.zuncode.ticketing.event.model.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {

}
