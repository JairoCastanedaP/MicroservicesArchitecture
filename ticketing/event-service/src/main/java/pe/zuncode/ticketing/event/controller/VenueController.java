package pe.zuncode.ticketing.event.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.zuncode.ticketing.event.dto.VenueRequest;
import pe.zuncode.ticketing.event.dto.VenueResponse;
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
    public List<VenueResponse> findAll() {
        return venueService.findAll();
    }

    @GetMapping("/{id}")
    public VenueResponse findById(@PathVariable Long id) {
        return venueService.findById(id);
    }

    // @Valid dispara Bean Validation; si falla, Spring lanza MethodArgumentNotValidException (400)
    @PostMapping
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody VenueRequest request) {
        VenueResponse created = venueService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
