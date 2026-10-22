package pe.zuncode.ticketing.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.zuncode.ticketing.booking.config.BookingProperties;
import pe.zuncode.ticketing.booking.dto.BookingRequest;
import pe.zuncode.ticketing.booking.dto.BookingResponse;
import pe.zuncode.ticketing.booking.dto.TicketResponse;
import pe.zuncode.ticketing.booking.exception.BusinessRuleException;
import pe.zuncode.ticketing.booking.exception.ResourceNotFoundException;
import pe.zuncode.ticketing.booking.model.Booking;
import pe.zuncode.ticketing.booking.model.BookingStatus;
import pe.zuncode.ticketing.booking.model.Ticket;
import pe.zuncode.ticketing.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio de reservas.
 * <ul>
 *   <li>Toda reserva nace PENDING y retiene las entradas durante {@code ticketing.booking.hold-time}.</li>
 *   <li>Solo una reserva PENDING y no vencida se puede confirmar; al confirmar se emite un ticket por entrada.</li>
 *   <li>Solo una reserva PENDING se puede cancelar.</li>
 *   <li>Si se intenta confirmar una reserva vencida, queda EXPIRED.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingProperties properties;

    public BookingService(BookingRepository bookingRepository, BookingProperties properties) {
        this.bookingRepository = bookingRepository;
        this.properties = properties;
    }

    public List<BookingResponse> findAll(String customerEmail, BookingStatus status) {
        return bookingRepository.search(customerEmail, status).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public BookingResponse findById(Long id) {
        return BookingResponse.from(getBooking(id));
    }

    public List<TicketResponse> findTickets(Long id) {
        Booking booking = bookingRepository.findWithTicketsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        return booking.getTickets().stream().map(TicketResponse::from).toList();
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(request.eventId(), request.ticketCategoryId(),
                request.customerEmail(), request.quantity(), request.unitPrice(),
                now, now.plus(properties.holdTime()));
        return BookingResponse.from(bookingRepository.save(booking));
    }

    /**
     * {@code noRollbackFor}: si la reserva venció la marcamos EXPIRED y lanzamos 409.
     * Por defecto una RuntimeException deshace la transacción (y el cambio a EXPIRED se perdería);
     * aquí queremos que ese cambio sí se guarde.
     */
    @Transactional(noRollbackFor = BusinessRuleException.class)
    public BookingResponse confirm(Long id) {
        Booking booking = getBooking(id);
        requirePending(booking, "confirmar");

        LocalDateTime now = LocalDateTime.now();
        if (booking.isExpired(now)) {
            booking.setStatus(BookingStatus.EXPIRED);
            throw new BusinessRuleException("La reserva " + id + " venció el " + booking.getExpiresAt());
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(now);
        for (int i = 0; i < booking.getQuantity(); i++) {
            booking.addTicket(new Ticket(now)); // cascade: se insertan al hacer commit
        }
        // flush: ejecuta ya el UPDATE (con su control de versión) y los INSERT de los tickets,
        // para devolver la versión actualizada en la respuesta
        bookingRepository.flush();
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = getBooking(id);
        requirePending(booking, "cancelar");
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.flush();
        return BookingResponse.from(booking);
    }

    private Booking getBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }

    private static void requirePending(Booking booking, String action) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessRuleException("No se puede " + action + " la reserva " + booking.getId()
                    + " porque está en estado " + booking.getStatus());
        }
    }
}
