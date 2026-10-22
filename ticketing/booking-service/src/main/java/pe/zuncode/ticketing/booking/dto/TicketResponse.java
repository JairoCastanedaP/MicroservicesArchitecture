package pe.zuncode.ticketing.booking.dto;

import pe.zuncode.ticketing.booking.model.Ticket;

import java.time.LocalDateTime;

public record TicketResponse(Long id, String code, LocalDateTime issuedAt) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getCode(), ticket.getIssuedAt());
    }
}
