package pe.zuncode.ticketing.payment.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.zuncode.ticketing.payment.dto.PaymentRequest;
import pe.zuncode.ticketing.payment.dto.PaymentResponse;
import pe.zuncode.ticketing.payment.service.PaymentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // GET /api/v1/payments?bookingId=1  (filtro opcional)
    @GetMapping
    public List<PaymentResponse> findAll(@RequestParam(required = false) Long bookingId) {
        return paymentService.findAll(bookingId);
    }

    @GetMapping("/{id}")
    public PaymentResponse findById(@PathVariable Long id) {
        return paymentService.findById(id);
    }

    // 201 tanto si el pago se aprueba como si se rechaza: el rechazo es un resultado de negocio
    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse created = paymentService.process(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
