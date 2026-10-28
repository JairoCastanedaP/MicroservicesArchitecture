package pe.zuncode.ticketing.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.zuncode.ticketing.booking.client.TicketAvailability;
import pe.zuncode.ticketing.booking.config.BookingProperties;
import pe.zuncode.ticketing.booking.dto.BookingRequest;
import pe.zuncode.ticketing.booking.dto.BookingResponse;
import pe.zuncode.ticketing.booking.dto.TicketResponse;
import pe.zuncode.ticketing.booking.exception.BusinessRuleException;
import pe.zuncode.ticketing.booking.exception.ResourceNotFoundException;
import pe.zuncode.ticketing.booking.model.Booking;
import pe.zuncode.ticketing.booking.model.BookingStatus;
import pe.zuncode.ticketing.booking.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio de reservas.
 * <ul>
 *   <li>Toda reserva nace PENDING y retiene las entradas durante {@code ticketing.booking.hold-time}.</li>
 *   <li>Sesión 3: el precio y la disponibilidad los da event-service (vía {@link CatalogService}).</li>
 *   <li>Solo una reserva PENDING y no vencida se puede pagar; al confirmar se emite un ticket por entrada.</li>
 *   <li>Solo una reserva PENDING se puede cancelar.</li>
 *   <li>Si se intenta pagar una reserva vencida, queda EXPIRED.</li>
 * </ul>
 * Sesión 3: ya no hay {@code @Transactional} a nivel de clase. Cada método declara la suya,
 * porque {@link #create} NO debe tener una transacción abierta mientras llama a otro servicio.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingProperties properties;
    private final CatalogService catalogService;

    public BookingService(BookingRepository bookingRepository, BookingProperties properties,
                          CatalogService catalogService) {
        this.bookingRepository = bookingRepository;
        this.properties = properties;
        this.catalogService = catalogService;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAll(String customerEmail, BookingStatus status) {
        return bookingRepository.search(customerEmail, status).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) {
        return BookingResponse.from(getBooking(id));
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findTickets(Long id) {
        Booking booking = bookingRepository.findWithTicketsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
        return booking.getTickets().stream().map(TicketResponse::from).toList();
    }

    /**
     * Sin @Transactional a propósito: la consulta a event-service puede tardar (o fallar tras
     * varios reintentos) y no queremos tener una conexión a la base de datos ocupada mientras
     * esperamos. {@code save()} abre su propia transacción, corta, al final.
     */
    public BookingResponse create(BookingRequest request) {
        TicketAvailability availability =
                catalogService.getAvailability(request.eventId(), request.ticketCategoryId());

        if (!availability.onSale()) {
            throw new BusinessRuleException("El evento " + availability.eventName() + " no está a la venta");
        }
        if (request.quantity() > availability.availableTickets()) {
            throw new BusinessRuleException("Solo quedan " + availability.availableTickets()
                    + " entradas en la zona " + availability.ticketCategoryName());
        }

        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking(request.eventId(), request.ticketCategoryId(),
                request.customerEmail(), request.quantity(), availability.price(),
                now, now.plus(properties.holdTime()));
        return BookingResponse.from(bookingRepository.save(booking));
    }

    /**
     * Paso 1 del pago: comprueba que la reserva se pueda pagar.
     * {@code noRollbackFor}: si la reserva venció la marcamos EXPIRED y lanzamos 409.
     * Por defecto una RuntimeException deshace la transacción (y el cambio a EXPIRED se perdería);
     * aquí queremos que ese cambio sí se guarde.
     */
    @Transactional(noRollbackFor = BusinessRuleException.class)
    public BookingResponse getPayable(Long id) {
        Booking booking = getBooking(id);
        requirePending(booking, "pagar");
        if (booking.isExpired(LocalDateTime.now())) {
            booking.setStatus(BookingStatus.EXPIRED);
            throw new BusinessRuleException("La reserva " + id + " venció el " + booking.getExpiresAt());
        }
        return BookingResponse.from(booking);
    }

    /**
     * Paso 3 del pago: payment-service aprobó el cobro → se confirma la reserva.
     * <p>
     * Volvemos a exigir PENDING: mientras se cobraba, alguien pudo cancelarla. En ese caso
     * el cobro ya se hizo y habría que devolverlo (compensación); lo veremos con Sagas y
     * consistencia eventual en la Sesión 8.
     */
    @Transactional
    public BookingResponse confirm(Long id, Long paymentId) {
        Booking booking = getBooking(id);
        requirePending(booking, "confirmar");
        booking.confirm(paymentId, LocalDateTime.now());
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
