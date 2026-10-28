package pe.zuncode.ticketing.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.zuncode.ticketing.payment.config.FailureSimulator;
import pe.zuncode.ticketing.payment.config.PaymentProperties;
import pe.zuncode.ticketing.payment.dto.PaymentRequest;
import pe.zuncode.ticketing.payment.dto.PaymentResponse;
import pe.zuncode.ticketing.payment.exception.ResourceNotFoundException;
import pe.zuncode.ticketing.payment.model.Payment;
import pe.zuncode.ticketing.payment.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Procesa pagos simulados.
 * <ul>
 *   <li>Si el monto supera {@code ticketing.payment.max-amount}, el pago se rechaza (rechazo de negocio:
 *       responde 201 con status REJECTED, no es un error HTTP).</li>
 *   <li>Si no, se aprueba (APPROVED).</li>
 *   <li>Antes de procesar, {@link FailureSimulator} puede demorar la respuesta o fallar con 503.</li>
 * </ul>
 * ⚠️ No es idempotente: si llega dos veces la misma solicitud, se cobra dos veces.
 * Por eso booking-service NO reintenta los pagos. En la Sesión 8 lo resolvemos con {@code Idempotency-Key}.
 */
@Service
@Transactional(readOnly = true)
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentProperties properties;
    private final FailureSimulator failureSimulator;

    public PaymentService(PaymentRepository paymentRepository, PaymentProperties properties,
                          FailureSimulator failureSimulator) {
        this.paymentRepository = paymentRepository;
        this.properties = properties;
        this.failureSimulator = failureSimulator;
    }

    @Transactional
    public PaymentResponse process(PaymentRequest request) {
        failureSimulator.apply(); // puede demorar o lanzar SimulatedFailureException (503)

        LocalDateTime now = LocalDateTime.now();
        Payment payment = request.amount().compareTo(properties.maxAmount()) > 0
                ? Payment.rejected(request.bookingId(), request.customerEmail(), request.amount(),
                        "El monto supera el límite de la tarjeta (" + properties.maxAmount() + ")", now)
                : Payment.approved(request.bookingId(), request.customerEmail(), request.amount(), now);

        Payment saved = paymentRepository.save(payment);
        log.info("Pago {} de la reserva {}: {} {}", saved.getId(), saved.getBookingId(),
                saved.getStatus(), saved.getAmount());
        return PaymentResponse.from(saved);
    }

    public PaymentResponse findById(Long id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", id));
    }

    public List<PaymentResponse> findAll(Long bookingId) {
        List<Payment> payments = bookingId == null
                ? paymentRepository.findAllByOrderByCreatedAtDesc()
                : paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        return payments.stream().map(PaymentResponse::from).toList();
    }
}
