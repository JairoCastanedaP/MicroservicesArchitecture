package pe.zuncode.ticketing.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.zuncode.ticketing.event.model.Venue;

/**
 * Acceso a datos de recintos.
 * <p>
 * Sesión 2: ya no escribimos la implementación. Spring Data JPA la genera en tiempo de
 * ejecución y nos da gratis findAll, findById, save, deleteById, existsById, count...
 * El servicio no cambia: sigue dependiendo de esta interfaz.
 */
public interface VenueRepository extends JpaRepository<Venue, Long> {
}
