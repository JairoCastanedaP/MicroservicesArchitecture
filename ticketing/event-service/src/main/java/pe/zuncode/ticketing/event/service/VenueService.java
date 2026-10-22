package pe.zuncode.ticketing.event.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.zuncode.ticketing.event.dto.VenueRequest;
import pe.zuncode.ticketing.event.dto.VenueResponse;
import pe.zuncode.ticketing.event.exception.ResourceNotFoundException;
import pe.zuncode.ticketing.event.model.Venue;
import pe.zuncode.ticketing.event.repository.VenueRepository;

import java.util.List;

@Service
@Transactional(readOnly = true) // por defecto todas las operaciones son de solo lectura
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<VenueResponse> findAll() {
        return venueRepository.findAll().stream().map(VenueResponse::from).toList();
    }

    public VenueResponse findById(Long id) {
        return VenueResponse.from(getVenue(id));
    }

    @Transactional // este sí escribe: abre una transacción de lectura/escritura
    public VenueResponse create(VenueRequest request) {
        Venue venue = new Venue(request.name(), request.city(), request.address(), request.capacity());
        return VenueResponse.from(venueRepository.save(venue));
    }

    private Venue getVenue(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recinto", id));
    }

}
