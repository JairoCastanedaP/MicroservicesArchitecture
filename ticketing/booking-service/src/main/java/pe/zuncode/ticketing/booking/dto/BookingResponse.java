package pe.zuncode.ticketing.booking.dto;

import pe.zuncode.ticketing.booking.model.Booking;
import pe.zuncode.ticketing.booking.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long eventId,
        Long ticketCategoryId,
        String customerEmail,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        BookingStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime confirmedAt,
        Long version
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getEventId(),
                booking.getTicketCategoryId(),
                booking.getCustomerEmail(),
                booking.getQuantity(),
                booking.getUnitPrice(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getExpiresAt(),
                booking.getConfirmedAt(),
                booking.getVersion());
    }
}
