package pe.zuncode.ticketing.event.dto;

import pe.zuncode.ticketing.event.model.TicketCategory;

import java.math.BigDecimal;

public record TicketCategoryResponse(Long id, String name, BigDecimal price, int capacity) {
    public static TicketCategoryResponse from(TicketCategory category) {
        return new TicketCategoryResponse(category.getId(), category.getName(),
                category.getPrice(), category.getCapacity());
    }
}
