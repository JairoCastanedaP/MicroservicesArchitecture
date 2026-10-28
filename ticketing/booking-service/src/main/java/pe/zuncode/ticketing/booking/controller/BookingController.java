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
import pe.zuncode.ticketing.booking.service.BookingPaymentService;
import pe.zuncode.ticketing.booking.service.BookingService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingPaymentService bookingPaymentService;

    public BookingController(BookingService bookingService, BookingPaymentService bookingPaymentService) {
        this.bookingService = bookingService;
        this.bookingPaymentService = bookingPaymentService;
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

    // Sesión 3: reemplaza a PATCH /{id}/confirm. Cobra en payment-service y, si se aprueba, confirma
    @PostMapping("/{id}/pay")
    public BookingResponse pay(@PathVariable Long id) {
        return bookingPaymentService.pay(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
