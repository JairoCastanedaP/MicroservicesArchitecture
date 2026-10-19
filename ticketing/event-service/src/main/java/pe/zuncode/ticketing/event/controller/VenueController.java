package pe.zuncode.ticketing.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.zuncode.ticketing.event.model.Venue;
import pe.zuncode.ticketing.event.service.VenueService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public List<Venue> findAll() {
        return venueService.findAll();
    }

    @GetMapping("/{id}")
    public Venue findById(@PathVariable Long id) {
        return venueService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Venue> create(@RequestBody Venue venue) {
        Venue created = venueService.create(venue);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

}
