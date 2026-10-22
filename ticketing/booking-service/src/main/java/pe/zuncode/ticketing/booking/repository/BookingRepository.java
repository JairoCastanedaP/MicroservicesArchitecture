package pe.zuncode.ticketing.booking.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.zuncode.ticketing.booking.model.Booking;
import pe.zuncode.ticketing.booking.model.BookingStatus;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** Búsqueda con filtros opcionales, las más recientes primero. */
    @Query("""
            select b from Booking b
            where (:customerEmail is null or lower(b.customerEmail) = lower(cast(:customerEmail as String)))
              and (:status is null or b.status = :status)
            order by b.createdAt desc
            """)
    List<Booking> search(@Param("customerEmail") String customerEmail,
                         @Param("status") BookingStatus status);

    /** Reserva con sus tickets en una sola consulta. */
    @EntityGraph(attributePaths = "tickets")
    Optional<Booking> findWithTicketsById(Long id);
}
