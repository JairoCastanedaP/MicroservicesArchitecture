package pe.zuncode.ticketing.event.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.zuncode.ticketing.event.model.Event;
import pe.zuncode.ticketing.event.model.EventCategory;
import pe.zuncode.ticketing.event.model.EventStatus;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de eventos con Spring Data JPA.
 */
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Búsqueda con filtros opcionales (un filtro en null no se aplica).
     * <p>
     * {@code @EntityGraph} trae el recinto y las zonas en la MISMA consulta (JOIN).
     * Sin él, Hibernate haría 1 consulta para los eventos + N consultas para sus
     * recintos y zonas: el famoso problema N+1.
     * <p>
     * {@code cast(:city as String)}: hace la consulta portable entre bases de datos:
     * PostgreSQL (al que migraremos más adelante) no sabe de qué tipo es un parámetro null
     * dentro de lower() y falla con "function lower(bytea) does not exist".
     */
    @EntityGraph(attributePaths = {"venue", "ticketCategories"})
    @Query("""
            select e from Event e
            where (:city is null or lower(e.venue.city) = lower(cast(:city as String)))
              and (:category is null or e.category = :category)
              and (:status is null or e.status = :status)
            order by e.dateTime
            """)
    List<Event> search(@Param("city") String city,
                       @Param("category") EventCategory category,
                       @Param("status") EventStatus status);

    /** Igual que findById, pero trayendo recinto y zonas en una sola consulta. */
    @EntityGraph(attributePaths = {"venue", "ticketCategories"})
    Optional<Event> findWithDetailsById(Long id);

    /** Query derivada del nombre del método: Spring Data genera el SQL. */
    boolean existsByVenueId(Long venueId);
}
