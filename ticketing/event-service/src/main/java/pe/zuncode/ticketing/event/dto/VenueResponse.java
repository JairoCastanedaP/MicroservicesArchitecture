package pe.zuncode.ticketing.event.dto;

import pe.zuncode.ticketing.event.model.Venue;

public record VenueResponse(Long id, String name, String city, String address, int capacity) {
    public static VenueResponse from(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getCity(),
                venue.getAddress(), venue.getCapacity());
    }

}
