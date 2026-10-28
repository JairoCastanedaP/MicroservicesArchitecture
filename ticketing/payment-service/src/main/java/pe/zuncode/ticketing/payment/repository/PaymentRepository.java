package pe.zuncode.ticketing.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.zuncode.ticketing.payment.model.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    List<Payment> findAllByOrderByCreatedAtDesc();
}
