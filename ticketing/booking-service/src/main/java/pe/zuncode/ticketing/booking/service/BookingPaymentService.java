package pe.zuncode.ticketing.booking.service;

import org.springframework.stereotype.Service;
import pe.zuncode.ticketing.booking.client.PaymentRequest;
import pe.zuncode.ticketing.booking.client.PaymentResult;
import pe.zuncode.ticketing.booking.dto.BookingResponse;
import pe.zuncode.ticketing.booking.exception.PaymentRejectedException;

/**
 * Sesión 3: orquesta el pago de una reserva entre DOS microservicios.
 * <p>
 * Sin @Transactional: una transacción de base de datos no puede abarcar una llamada HTTP a otro
 * servicio (payment-service tiene su propia base de datos). Por eso el flujo son 3 pasos cortos:
 * <ol>
 *   <li>Transacción 1: validar que la reserva se pueda pagar ({@link BookingService#getPayable}).</li>
 *   <li>Llamada HTTP a payment-service, sin transacción abierta ({@link PaymentGateway#pay}).</li>
 *   <li>Transacción 2: confirmar la reserva y emitir los tickets ({@link BookingService#confirm}).</li>
 * </ol>
 * Es una clase aparte (y no un método más de BookingService) para que las llamadas a
 * getPayable y confirm pasen por el proxy de Spring y se apliquen sus @Transactional.
 */
@Service
public class BookingPaymentService {

    private final BookingService bookingService;
    private final PaymentGateway paymentGateway;

    public BookingPaymentService(BookingService bookingService, PaymentGateway paymentGateway) {
        this.bookingService = bookingService;
        this.paymentGateway = paymentGateway;
    }

    public BookingResponse pay(Long bookingId, Requester requester) {
        BookingResponse booking = bookingService.getPayable(bookingId, requester);

        PaymentResult payment = paymentGateway.pay(
                new PaymentRequest(booking.id(), booking.customerEmail(), booking.totalAmount()));

        if (!payment.isApproved()) {
            throw new PaymentRejectedException("El pago de la reserva " + bookingId + " fue rechazado: "
                    + payment.rejectionReason());
        }
        return bookingService.confirm(bookingId, payment.id());
    }
}
