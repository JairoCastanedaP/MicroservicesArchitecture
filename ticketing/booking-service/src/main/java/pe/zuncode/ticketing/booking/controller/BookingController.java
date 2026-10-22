package pe.zuncode.ticketing.booking.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.zuncode.ticketing.booking.dto.BookingRequest;
import pe.zuncode.ticketing.booking.dto.BookingResponse;
import pe.zuncode.ticketing.booking.dto.TicketResponse;
import pe.zuncode.ticketing.booking.model.BookingStatus;
import pe.zuncode.ticketing.booking.service.BookingService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // GET /api/v1/bookings?customerEmail=ana@mail.com&status=CONFIRMED  (filtros opcionales)
    @GetMapping
    public List<BookingResponse> findAll(@RequestParam(required = false) String customerEmail,
                                         @RequestParam(required = false) BookingStatus status) {
        return bookingService.findAll(customerEmail, status);
    }

    @GetMapping("/{id}")
    public BookingResponse findById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @GetMapping("/{id}/tickets")
    public List<TicketResponse> findTickets(@PathVariable Long id) {
        return bookingService.findTickets(id);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{id}/confirm")
    public BookingResponse confirm(@PathVariable Long id) {
        return bookingService.confirm(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
