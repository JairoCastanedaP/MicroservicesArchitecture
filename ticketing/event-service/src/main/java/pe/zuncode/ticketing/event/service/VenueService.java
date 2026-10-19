package pe.zuncode.ticketing.event.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pe.zuncode.ticketing.event.model.Venue;
import pe.zuncode.ticketing.event.repository.VenueRepository;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> findAll () {
        return venueRepository.findAll();
    }

    public Venue findById (Long id) {
        return  venueRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException( HttpStatus.NOT_FOUND,
                                        "Recinto no encontrado: " + id)); }

    public Venue create (Venue venue) {
        if (isBlank(venue.getName()) || isBlank(venue.getCity())) {
            throw new ResponseStatusException (HttpStatus.BAD_REQUEST, "name y city son obligatorios"
                            ); }
        if (venue.getCapacity() <= 0) {
            throw new ResponseStatusException (HttpStatus.BAD_REQUEST,
                                    "capacity debe ser mayor a 0"
                            );
        }
        venue.setId(null);
        // el id lo asigna el repositorio
        return venueRepository.save(venue);
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
