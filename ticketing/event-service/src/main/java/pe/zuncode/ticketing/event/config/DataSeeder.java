package pe.zuncode.ticketing.event.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.zuncode.ticketing.event.dto.*;
import pe.zuncode.ticketing.event.model.Event;
import pe.zuncode.ticketing.event.model.EventCategory;
import pe.zuncode.ticketing.event.model.TicketCategory;
import pe.zuncode.ticketing.event.model.Venue;
import pe.zuncode.ticketing.event.repository.VenueRepository;
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

    private final VenueRepository venueRepository;
    private final VenueService venueService;
    private final EventService eventService;

    public DataSeeder(VenueRepository venueRepository, VenueService venueService, EventService eventService) {
        this.venueRepository = venueRepository;
        this.venueService = venueService;
        this.eventService = eventService;
    }

    @Override
    public void run(String... args) {
        if (venueRepository.count() > 0) {
            log.info("La base de datos ya tiene datos: se omite la carga de datos semilla");
            return;
        }

        VenueResponse estadio = venueService.create(
                new VenueRequest("Estadio Nacional", "Lima", "Av. José Díaz s/n, Cercado de Lima", 45000));
        VenueResponse arena = venueService.create(
                new VenueRequest("Arena Perú", "Lima", "Av. Javier Prado Este 4200, Surco", 12000));
        VenueResponse teatro = venueService.create(
                new VenueRequest("Teatro Municipal de Arequipa", "Arequipa", "Calle Mercaderes 239", 800));

        EventResponse concierto = eventService.create(new EventRequest(
                "Rock en el Nacional", "Gran concierto de rock latino",
                EventCategory.MUSIC, estadio.id(), inDays(60, 20, 0),
                List.of(new TicketCategoryRequest("Campo VIP", new BigDecimal("450.00"), 5000),
                        new TicketCategoryRequest("Campo General", new BigDecimal("220.00"), 20000),
                        new TicketCategoryRequest("Tribuna Oriente", new BigDecimal("150.00"), 15000))));
        eventService.publish(concierto.id());

        EventResponse partido = eventService.create(new EventRequest(
                "Final Liga de Vóley", "Final del campeonato nacional de vóley femenino",
                EventCategory.SPORTS, arena.id(), inDays(30, 18, 30),
                List.of(new TicketCategoryRequest("Preferencial", new BigDecimal("120.00"), 4000),
                        new TicketCategoryRequest("General", new BigDecimal("60.00"), 8000))));
        eventService.publish(partido.id());

        // Queda en DRAFT a propósito (sin zonas) para practicar la regla de publicación
        eventService.create(new EventRequest(
                "La Casa de Bernarda Alba", "Clásico de García Lorca",
                EventCategory.THEATER, teatro.id(), inDays(45, 19, 0), List.of()));

        log.info("Datos semilla cargados: {} recintos, {} eventos",
                venueRepository.count(), eventService.findAll(null, null, null).size());
    }

    private static LocalDateTime inDays(int days, int hour, int minute) {
        return LocalDate.now().plusDays(days).atTime(LocalTime.of(hour, minute));
    }


}
