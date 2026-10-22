package pe.zuncode.ticketing.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pe.zuncode.ticketing.booking.model.Booking;
import pe.zuncode.ticketing.booking.model.BookingStatus;
import pe.zuncode.ticketing.booking.repository.BookingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Demuestra el bloqueo optimista (@Version) contra la base de datos real (booking_db).
 * <p>
 * Simula a dos usuarios que leen la MISMA reserva (versión 0) y la modifican:
 * el primero que guarda gana; el segundo recibe ObjectOptimisticLockingFailureException.
 */
@SpringBootTest
class BookingOptimisticLockingTests {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void secondUpdateWithStaleVersionIsRejected() {
        LocalDateTime now = LocalDateTime.now();
        Booking saved = bookingRepository.save(new Booking(1L, 1L, "test@zuncode.pe", 2,
                new BigDecimal("450.00"), now, now.plusMinutes(10)));

        try {
            // Sin @Transactional en el test, cada llamada al repositorio es su propia transacción:
            // obtenemos dos copias independientes de la misma fila, ambas con version = 0
            Booking userA = bookingRepository.findById(saved.getId()).orElseThrow();
            Booking userB = bookingRepository.findById(saved.getId()).orElseThrow();

            userA.setStatus(BookingStatus.CONFIRMED);
            Booking afterA = bookingRepository.save(userA); // UPDATE ... WHERE id=? AND version=0
            assertThat(afterA.getVersion()).isEqualTo(1L);

            userB.setStatus(BookingStatus.CANCELLED);          // userB todavía cree que la versión es 0
            assertThatThrownBy(() -> bookingRepository.save(userB))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);

            // El cambio de A se conservó: B no lo pisó
            assertThat(bookingRepository.findById(saved.getId()).orElseThrow().getStatus())
                    .isEqualTo(BookingStatus.CONFIRMED);
        } finally {
            bookingRepository.deleteById(saved.getId());
        }
    }
}
