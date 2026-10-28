package pe.zuncode.ticketing.payment.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.zuncode.ticketing.payment.config.FailureSimulator;
import pe.zuncode.ticketing.payment.dto.SimulationSettings;

/**
 * Solo para el curso: cambia en caliente cuán lento o inestable es payment-service.
 * <pre>
 * PUT /api/v1/simulation  { "failureRate": 1.0, "delay": "PT0S" }   → todos los pagos fallan (503)
 * PUT /api/v1/simulation  { "failureRate": 0.0, "delay": "PT5S" }   → todos los pagos tardan 5 s
 * PUT /api/v1/simulation  { "failureRate": 0.0, "delay": "PT0S" }   → vuelve a la normalidad
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/simulation")
public class SimulationController {

    private final FailureSimulator failureSimulator;

    public SimulationController(FailureSimulator failureSimulator) {
        this.failureSimulator = failureSimulator;
    }

    @GetMapping
    public SimulationSettings get() {
        return failureSimulator.getSettings();
    }

    @PutMapping
    public SimulationSettings update(@Valid @RequestBody SimulationSettings settings) {
        return failureSimulator.update(settings);
    }
}
