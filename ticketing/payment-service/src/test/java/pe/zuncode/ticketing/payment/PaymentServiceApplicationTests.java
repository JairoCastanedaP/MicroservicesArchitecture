package pe.zuncode.ticketing.payment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pe.zuncode.ticketing.payment.config.FailureSimulator;
import pe.zuncode.ticketing.payment.dto.PaymentRequest;
import pe.zuncode.ticketing.payment.dto.PaymentResponse;
import pe.zuncode.ticketing.payment.dto.SimulationSettings;
import pe.zuncode.ticketing.payment.exception.SimulatedFailureException;
import pe.zuncode.ticketing.payment.model.PaymentStatus;
import pe.zuncode.ticketing.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Usa una base H2 en memoria propia de los tests (no toca data/payment_db).
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:payment_test")
class PaymentServiceApplicationTests {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private FailureSimulator failureSimulator;

    @AfterEach
    void resetSimulation() {
        failureSimulator.update(new SimulationSettings(0.0, Duration.ZERO));
    }

    @Test
    void approvesPaymentWithinLimit() {
        PaymentResponse payment = paymentService.process(
                new PaymentRequest(1L, "ana@mail.com", new BigDecimal("900.00")));

        assertThat(payment.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.rejectionReason()).isNull();
    }

    @Test
    void rejectsPaymentOverLimit() {
        PaymentResponse payment = paymentService.process(
                new PaymentRequest(2L, "ana@mail.com", new BigDecimal("4500.00")));

        assertThat(payment.status()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.rejectionReason()).contains("límite");
    }

    @Test
    void failsWhenSimulationSaysSo() {
        failureSimulator.update(new SimulationSettings(1.0, Duration.ZERO));

        assertThatThrownBy(() -> paymentService.process(
                new PaymentRequest(3L, "ana@mail.com", new BigDecimal("100.00"))))
                .isInstanceOf(SimulatedFailureException.class);
    }
}
