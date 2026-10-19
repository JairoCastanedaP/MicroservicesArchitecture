package pe.zuncode.ticketing.event.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.zuncode.ticketing.event.model.Event;
import pe.zuncode.ticketing.event.model.EventCategory;
import pe.zuncode.ticketing.event.model.TicketCategory;
import pe.zuncode.ticketing.event.model.Venue;
import pe.zuncode.ticketing.event.service.EventService;
import pe.zuncode.ticketing.event.service.VenueService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Component
@ConditionalOnProperty(name = "ticketing.seed-data.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final VenueService venueService;
    private final EventService eventService;

    public DataSeeder(VenueService venueService, EventService eventService) {
        this.venueService = venueService;
        this.eventService = eventService;
    }

    @Override
    public void run(String... args) {
        Venue estadio = venueService.create(
                new Venue(null, "Estadio Nacional", "Lima", "Av. José Díaz s/n, Cercado de Lima", 45000));
        Venue arena = venueService.create(
                new Venue(null, "Arena Perú", "Lima", "Av. Javier Prado Este 4200, Surco", 12000));
        Venue teatro = venueService.create(
                new Venue(null, "Teatro Municipal de Arequipa", "Arequipa", "Calle Mercaderes 239", 800));

        Event concierto = eventService.create(new Event(1L,
                "Rock en el Nacional", "Gran concierto de rock latino",
                EventCategory.MUSIC, estadio.getId(), inDays(60, 20, 0),
                List.of(new TicketCategory(1L,"Campo VIP", new BigDecimal("450.00"), 5000),
                        new TicketCategory(2L,"Campo General", new BigDecimal("220.00"), 20000),
                        new TicketCategory(3L,"Tribuna Oriente", new BigDecimal("150.00"), 15000))));
        eventService.publish(concierto.getId());

        Event partido = eventService.create(new Event(2L,
                "Final Liga de Vóley", "Final del campeonato nacional de vóley femenino",
                EventCategory.SPORTS, arena.getId(), inDays(30, 18, 30),
                List.of(new TicketCategory(1L,"Preferencial", new BigDecimal("120.00"), 4000),
                        new TicketCategory(2L,"General", new BigDecimal("60.00"), 8000))));
        eventService.publish(partido.getId());

        // Queda en DRAFT a propósito (sin zonas) para practicar la regla de publicación
        eventService.create(new Event(3L,
                "La Casa de Bernarda Alba", "Clásico de García Lorca",
                EventCategory.THEATER, teatro.getId(), inDays(45, 19, 0), List.of()));

        log.info("Datos semilla cargados: {} recintos, {} eventos",
                venueService.findAll().size(), eventService.findAll(null, null, null).size());
    }

    private static LocalDateTime inDays(int days, int hour, int minute) {
        return LocalDate.now().plusDays(days).atTime(LocalTime.of(hour, minute));
    }

}
