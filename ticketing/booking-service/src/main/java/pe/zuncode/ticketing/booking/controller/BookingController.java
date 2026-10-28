package pe.zuncode.ticketing.booking.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import pe.zuncode.ticketing.booking.service.Requester;

import java.net.URI;
import java.util.List;

/**
 * Sesión 4: cada método recibe el {@link Authentication} (el usuario del token ya validado).
 * Los roles los revisa SecurityConfig; que la reserva sea del usuario lo revisa BookingService.
 */
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingPaymentService bookingPaymentService;

    public BookingController(BookingService bookingService, BookingPaymentService bookingPaymentService) {
        this.bookingService = bookingService;
        this.bookingPaymentService = bookingPaymentService;
    }

    // GET /api/v1/bookings?status=CONFIRMED  (un ADMIN puede filtrar además por ?customerEmail=)
    @GetMapping
    public List<BookingResponse> findAll(@RequestParam(required = false) String customerEmail,
                                         @RequestParam(required = false) BookingStatus status,
                                         Authentication authentication) {
        return bookingService.findAll(Requester.from(authentication), customerEmail, status);
    }

    @GetMapping("/{id}")
    public BookingResponse findById(@PathVariable Long id, Authentication authentication) {
        return bookingService.findById(id, Requester.from(authentication));
    }

    @GetMapping("/{id}/tickets")
    public List<TicketResponse> findTickets(@PathVariable Long id, Authentication authentication) {
        return bookingService.findTickets(id, Requester.from(authentication));
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request,
                                                  Authentication authentication) {
        BookingResponse created = bookingService.create(request, Requester.from(authentication));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // Sesión 3: reemplaza a PATCH /{id}/confirm. Cobra en payment-service y, si se aprueba, confirma
    @PostMapping("/{id}/pay")
    public BookingResponse pay(@PathVariable Long id, Authentication authentication) {
        return bookingPaymentService.pay(id, Requester.from(authentication));
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, Authentication authentication) {
        return bookingService.cancel(id, Requester.from(authentication));
    }
}
